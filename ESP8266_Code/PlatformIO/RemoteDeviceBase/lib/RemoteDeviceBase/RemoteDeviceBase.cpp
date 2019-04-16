/*
  Author: Tobias Grundtvig
*/
#include <FS.h>
#include <Arduino.h>
#include <ESP8266WiFi.h>
#include <WiFiManager.h>
#include <ArduinoJson.h>
#include "RemoteDeviceBase.h"

volatile bool RemoteDeviceBase::_shouldSaveConfig = false;

RemoteDeviceBase::RemoteDeviceBase(const char *deviceTypeName,
                                   const uint16_t deviceType,
                                   const uint16_t deviceVersion,
                                   const uint16_t maxPackageSize,
                                   const uint16_t pingInterval,
                                   const uint16_t reconnectInterval,
                                   const uint8_t wiFiResetPin,
                                   const uint8_t ledPin)
{
  _deviceId = ESP.getChipId();
  _deviceTypeName = deviceTypeName;
  _deviceType = deviceType;
  _deviceVersion = deviceVersion;
  _maxPackageSize = maxPackageSize;
  _ledPin = ledPin;
  _pingInterval = pingInterval;
  _reconnectInterval = reconnectInterval;
  _readBuffer = new uint8_t[_maxPackageSize];
  _connectionState = 0;
  _client.setNoDelay(true);
  _lastPingReceived = 0;

  //Read the WiFi reset pin
  pinMode(wiFiResetPin, INPUT_PULLUP);
  delay(10);
  _resetWiFi = !digitalRead(wiFiResetPin);
}

// ConnectionState:
// 0 -> Boot loop,
// 1 -> Not on WiFi,
// 2 -> On WiFi, not connected to server,
// 3 -> Waiting for server accept,
// 4 -> Accepted by server, waiting to send init package,
// 5 -> Initializationpackage sent, ready to work.
void RemoteDeviceBase::update(unsigned long curTime)
{
  if (_connectionState != 255)
  {
    _updateConnectionState();
  }
  //Serial.print("Update. _connectionState: ");
  //Serial.println(_connectionState);
  switch (_connectionState)
  {
  case 0: //Boot loop
    //Serial.println("Boot loop");
    if (bootLoop(curTime))
    {
      ++_connectionState;
      _startWiFi();
    }
    break;
  case 1: //Not on WiFi
    //We just wait for WiFi to be ready...
    noWiFiLoop(curTime);
    break;
  case 2: //On WiFi, not connected to server
    tryingToConnectLoop(curTime);
    if (_tryConnectToServer(curTime))
    {
      _connectionState = 3;
      _stateStartTime = curTime;
    }
    break;
  case 3: //Waiting for server accept
    if (curTime - _stateStartTime > 10000)
    {
      //Server did not accept in time
      onServerResponseTimeout("Server accept.");
      _client.stop();
      _connectionState = 2;
    }
    if (_readPackage(curTime))
    {
      if (_packageSize != 1)
      {
        Serial.print("Serious error: Size of response package from server not equal to 1. It was ");
        Serial.println(_packageSize);
        _connectionState = 255;
      }
      if (_readBuffer[0] == 0)
      {
        //Server accepted
        onAcceptedByServer();
        _stateStartTime = curTime;
        _connectionState = 4;
      }
      else
      {
        onRejectedByServer();
        _client.stop();
        _connectionState = 2;
      }
    }
    break;
  case 4: //Accepted by server, waiting to send initialization package
    if (curTime - _stateStartTime > 10000)
    {
      //Server did not accept in time
      onClientResponseTimeout("Sending initialization package.");
      _client.stop();
      _connectionState = 2;
    }
    if (sendInitializationPackageLoop(curTime))
    {
      _stateStartTime = curTime;
      _lastPingReceived = curTime;
      _connectionState = 5;
    }
    break;
  case 5: //Initialization package sent, ready to work
    if (_handlePingPong(curTime))
    {
      mainLoop(curTime);
      if (_readPackage(curTime))
      {
        //We have received a package
        Serial.println("Package received!");
        onPackageReceived(_readBuffer, _packageSize);
      }
    }
    break;
  case 255: //Serious error
    if (curTime - _lastConnectionAttempt > 20000)
    {
      Serial.println("Serious error!");
      _lastConnectionAttempt = curTime;
    }
    break;
  default:
    Serial.print("Serious error: Unknown connection state: ");
    Serial.print(_connectionState);
    _connectionState = 255;
  }
}

// ConnectionState:
// 0 -> Boot loop,
// 1 -> Not on WiFi,
// 2 -> On WiFi, not connected to server,
// 3 -> Waiting for server accept,
// 4 -> Accepted by server, waiting to send init package,
// 5 -> Initializationpackage sent, ready to work.
void RemoteDeviceBase::_updateConnectionState()
{
  if (_connectionState == 0)
  {
    //We are still in the boot loop...
    return;
  }
  if (!_client.connected())
  {
    if (_connectionState > 2)
    {
      Serial.println("Client disconnected!");
      _connectionState = 2;
      onDisconnectedFromServer();
    }
  }
  if (WiFi.isConnected())
  {
    if (_connectionState == 1)
    {
      _connectionState = 2;
      onConnectedToWiFi();
    }
  }
  else
  {
    if (_connectionState > 1)
    {
      _connectionState = 1;
      onDisconnectedFromWiFi();
    }
  }
}

bool RemoteDeviceBase::_tryConnectToServer(unsigned long curTime)
{
  if (curTime - _lastConnectionAttempt < _reconnectInterval)
  {
    //Not time to try to connect yet.
    return false;
  }
  onTryConnectToServer(_config.hostname, _config.port);
  if (!_client.connect(_config.hostname, _config.port))
  {
    _lastConnectionAttempt = curTime;
    onConnectionToServerFailed();
    return false;
  }
  _sendHandshakePackage();
  onConnectedToServer();
  return true;
}

bool RemoteDeviceBase::sendPackage(uint8_t dataBuffer[], uint16_t index, uint16_t size)
{
  if (!_client.connected())
  {
    return false;
  }
  _client.write((uint8_t)(size >> 8));
  _client.write((uint8_t)(size));
  for (int i = 0; i < size; ++i)
  {
    _client.write(dataBuffer[i + index]);
  }
  _client.flush();
  return true;
}

bool RemoteDeviceBase::sendPackage(uint8_t dataBuffer[], uint16_t size)
{
  return sendPackage(dataBuffer, 0, size);
}

void RemoteDeviceBase::_writeIntegerToBuffer(uint8_t *buffer, uint64_t data, uint16_t index, uint8_t size)
{
  for (uint8_t i = 0; i < size; ++i)
  {
    uint8_t shift = size - i - 1;
    buffer[i + index] = (uint8_t)(data >> (shift * 8));
  }
}

void RemoteDeviceBase::_sendHandshakePackage()
{
  uint8_t tmp[16];
  //deviceId
  _writeIntegerToBuffer(tmp, _deviceId, 0, 8);
  //deviceType
  _writeIntegerToBuffer(tmp, _deviceType, 8, 2);
  //deviceVersion
  _writeIntegerToBuffer(tmp, _deviceVersion, 10, 2);
  //max package size
  _writeIntegerToBuffer(tmp, _maxPackageSize, 12, 2);
  //ping interval
  _writeIntegerToBuffer(tmp, _pingInterval, 14, 2);
  //Send the package
  sendPackage(tmp, 16);
}

bool RemoteDeviceBase::_handlePingPong(unsigned long curTime)
{
  //uint16_t pingTimeout = _connectionState == 5 ? _pingInterval*2 : 10000;
  uint16_t pingTimeout = _pingInterval * 2;
  if (curTime - _lastPingReceived > pingTimeout)
  {
    //Ping not received in due time...
    Serial.println("Ping not received in due time!");
    _client.stop();
    _connectionState = 2;
    onServerResponseTimeout("Ping not recieved in due time.");
    onDisconnectedFromServer();
    return false;
  }
  return true;
}

bool RemoteDeviceBase::_readPackage(unsigned long curTime)
{
  while (_client.available())
  {
    switch (_bytesRead)
    {
    case 0:
      //Read first byte in the size...
      _packageSize = 0;
      _packageSize += _client.read();
      _packageSize <<= 8;
      ++_bytesRead;
      break;
    case 1:
      //read second byte in the size...
      _packageSize += _client.read();
      ++_bytesRead;
      if (_packageSize == 0)
      {
        //This is an empty package which is a ping..
        //So we just send a pong...
        _lastPingReceived = curTime;
        Serial.print("Got ping at ");
        Serial.println(curTime);
        Serial.println("Sending pong!");
        //Send pong
        _client.write((uint8_t)0);
        _client.write((uint8_t)0);
        _client.flush();
        _bytesRead = 0;
      }
      else if (_packageSize > _maxPackageSize)
      {
        _client.stop();
        //This is a serious error, so we print out some debug info...
        Serial.print("Serious error: Package size too big!!! Was: ");
        Serial.print(_packageSize);
        Serial.print(" Maximum is: ");
        Serial.println(_maxPackageSize);
        _bytesRead = 0;
        _connectionState = 255; //Serious error
        return false;
      }
      break;
    default:
      //Read next Databyte
      _readBuffer[_bytesRead - 2] = _client.read();
      ++_bytesRead;
      if (_bytesRead == _packageSize + 2)
      {
        //All bytes have been read.
        _bytesRead = 0;
        return true;
      }
      break;
    }
  }
  return false;
}

void RemoteDeviceBase::_startWiFi()
{
  Serial.println("Loading configuration file.");
  Serial.print("Mounting filesystem : ");
  //Mount filesystem
  if (SPIFFS.begin())
  {
    Serial.println("Succes.");
  }
  else
  {
    Serial.println("Failed.");
  }
  Serial.print("Reset WiFi button pushed? : ");
  Serial.println(_resetWiFi);
  _shouldSaveConfig = false;
  //Setting default values
  strcpy(_config.hostname, "");
  _config.port = 3377;
  //Loading from file if exists
  bool configLoaded = _loadConfiguration();
  Serial.print("Configuration loaded : ");
  Serial.println(configLoaded);
  char strPort[8];
  itoa(_config.port, strPort, 10);

  Serial.println("Starting WiFi...");
  WiFiManagerParameter custom_host("host", "device server ip", _config.hostname, 64);
  WiFiManagerParameter custom_port("port", "device server port", strPort, 8);
  WiFiManager wifiManager;
  wifiManager.setSaveConfigCallback(RemoteDeviceBase::_onSaveConfigCallback);
  wifiManager.addParameter(&custom_host);
  wifiManager.addParameter(&custom_port);
  if (_resetWiFi || !configLoaded)
  {
    wifiManager.resetSettings();
  }
  String apName = _deviceTypeName;
  apName += "_";
  apName += _deviceId;
  wifiManager.autoConnect(apName.c_str());
  //Copy values
  strcpy(_config.hostname, custom_host.getValue());
  String tmp(custom_port.getValue());
  _config.port = tmp.toInt();
  if (_shouldSaveConfig)
  {
    _saveConfiguration();
  }
}

// Loads the configuration from a file
bool RemoteDeviceBase::_loadConfiguration()
{
  if (SPIFFS.exists("/config.json"))
  {
    //file exists, reading and loading
    Serial.println("reading config file");
    File configFile = SPIFFS.open("/config.json", "r");
    if (configFile)
    {
      Serial.println("opened config file");
      // Allocate a temporary JsonDocument
      // Don't forget to change the capacity to match your requirements.
      // Use arduinojson.org/v6/assistant to compute the capacity.
      StaticJsonDocument<512> doc;
      // Deserialize the JSON document
      DeserializationError error = deserializeJson(doc, configFile);
      if (error)
      {
        Serial.println(F("Failed to read file, using default configuration"));
        return false;
      }
      // Copy values from the JsonDocument to the Config
      _config.port = doc["port"];
      strlcpy(_config.hostname,          // <- destination
              doc["hostname"],           // <- source
              sizeof(_config.hostname)); // <- destination's capacity

      // Close the file (Curiously, File's destructor doesn't close the file)
      configFile.close();
      return true;
    }
    else
    {
      Serial.println("Could not open config file.");
      return false;
    }
  }
  else
  {
    Serial.println("Config file does not exist.");
    return false;
  }
 return false;
}

// Saves the configuration to a file
void RemoteDeviceBase::_saveConfiguration()
{
  // Delete existing file, otherwise the configuration is appended to the file
  SPIFFS.remove("/config.json");

  // Open file for writing
  File configFile = SPIFFS.open("/config.json", "w");
  if (!configFile)
  {
    Serial.println(F("Failed to create file"));
    return;
  }

  // Allocate a temporary JsonDocument
  // Don't forget to change the capacity to match your requirements.
  // Use arduinojson.org/assistant to compute the capacity.
  StaticJsonDocument<512> doc;

  // Set the values in the document
  doc["hostname"] = _config.hostname;
  doc["port"] = _config.port;

  // Serialize JSON to file
  if (serializeJson(doc, configFile) == 0)
  {
    Serial.println(F("Failed to write to file"));
  }

  // Close the file
  configFile.close();
}

void RemoteDeviceBase::_onSaveConfigCallback()
{
  Serial.println("Config should be saved.");
  _shouldSaveConfig = true;
}