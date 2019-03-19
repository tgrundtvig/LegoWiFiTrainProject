/*
  Author: Tobias Grundtvig
*/

#include "Arduino.h"
#include <ESP8266WiFi.h> 
#include <WiFiManager.h>
#include "RemoteDevice.h"

#define DEFAULT_SERVER_RESPONSE_TIMEOUT 5000;
#define DEFAULT_RECONNECTION_INTERVAL 5000;

//Default callback functions

void _defaultOnConnectedToWiFi()
{
  Serial.print("Connected to WiFi: ");
  Serial.println(WiFi.SSID());
}

void _defaultOnDisconnectedFromWiFi()
{
  Serial.println("Disconnected from WiFi!");
}

void _defaultOnTryConnectToServer(char* host, uint16_t port)
{
  Serial.print("Trying to connect to ");
  Serial.print(host);
  Serial.print(':');
  Serial.println(port);    
}

void _defaultOnConnectionToServerFailed()
{
  Serial.println("Connection to server failed!");
}

void _defaultOnConnectedToServer()
{
  Serial.println("Connected to the server, waiting for accept!");
}

void _defaultOnDisconnectedFromServer()
{
  Serial.println("Disconnected from server!");
}

void _defaultOnAcceptedByServer()
{
  Serial.println("Accepted by server.");
}

void _defaultOnRejectedByServer(uint8_t errorCode)
{
  Serial.print("Rejected by server. Error code: ");
  Serial.println(errorCode);
}

void _defaultOnServerResponseTimedOut()
{
  Serial.println("Server response timed out!");
}

RemoteDevice::RemoteDevice(	void (*onPackageReceived) (uint8_t data[], uint16_t size),
							bool (*onSendInitializationPackage)(),
							uint32_t deviceType,
							uint32_t deviceVersion,
							char* typeName,
							uint16_t maxPackageSize										)
{
	_onPackageReceived = onPackageReceived;
	_onSendInitializationPackage = onSendInitializationPackage;
	_typeName = typeName;
	_dataBuffer = new uint8_t[maxPackageSize];
	_packageSize = -1;
	_bytesRead = 0;
	_chipId = ESP.getChipId();
	_deviceType = deviceType;
	_deviceVersion = deviceVersion;
	_maxPackageSize = maxPackageSize;
	_lastConnectionAttempt = 0;
	_serverResponseTimeout = DEFAULT_SERVER_RESPONSE_TIMEOUT;
	_reconnectionInterval = DEFAULT_RECONNECTION_INTERVAL;
	_serverTimeout = 0;
  _client.setSync(true);
	// ConnectionState:
  // 0 -> Not on WiFi,
  // 1 -> On WiFi, not connected to server,
  // 2 -> Waiting for server accept,
  // 3 -> Accepted by server, waiting to send init package, 
  // 4 -> Initializationpackage sent, ready to work.
	_connectionState = 0;
	
	_onConnectedToWiFi = &_defaultOnConnectedToWiFi;
	_onDisconnectedFromWiFi = &_defaultOnDisconnectedFromWiFi;
	_onTryConnectToServer = &_defaultOnTryConnectToServer;
	_onConnectionToServerFailed = &_defaultOnConnectionToServerFailed;
	_onConnectedToServer = &_defaultOnConnectedToServer;
	_onDisconnectedFromServer = &_defaultOnDisconnectedFromServer;
	_onAcceptedByServer = &_defaultOnAcceptedByServer;
	_onRejectedByServer = &_defaultOnRejectedByServer;
	_onServerResponseTimedOut = &_defaultOnServerResponseTimedOut;
}

void RemoteDevice::start(char* host, uint16_t port, bool resetWiFi)
{
	_host = host;
	_port = port;
	WiFiManager wifiManager;
	if(resetWiFi)
	{		
		wifiManager.resetSettings();
	}
	String apName = _typeName;
	apName += "_";
	apName += _chipId;
	wifiManager.autoConnect(apName.c_str());
}

bool RemoteDevice::sendPackage(uint8_t data[], uint16_t size)
{
  if(_connectionState >= 3 && _client.connected())
  {
    _client.write((uint8_t) (size >> 8));
    _client.write((uint8_t) (size));
    for(int i = 0; i < size; ++i)
    {
      _client.write(data[i]);
    }
    _client.flush();
    return true;
  }
  return false;
}


int RemoteDevice::getConnectionState()
{
  return _connectionState;
}

// ConnectionState:
// 0 -> Not on WiFi,
// 1 -> On WiFi, not connected to server,
// 2 -> Waiting for server accept,
// 3 -> Accepted by server, waiting to send init package, 
// 4 -> Initializationpackage sent, ready to work.

void RemoteDevice::update()
{
	_updateConnectionState();
  switch(_connectionState)
  {
    case 0:
      // We are not connected to WiFi, so we have to wait
      // Maybe we should restart after some time????
      break;
    case 1:
      //We are on wifi, but not connected to the server
      _tryConnectToServer();
      break;
    case 2:
      //Waiting for server acceptance... 
      _handleWaitingForServerAcceptance();
      break;
    case 3:
      //Wait for the initialization package to be sent
      //This is a call back
      if(_onSendInitializationPackage())
      {
        _connectionState = 4;
      }
      break;
    case 4:
      //Normal operation...
      _handleNormalOperation();
      break;
    default:
      Serial.print("Internal error, _connectionState invalid: ");
      Serial.println(_connectionState);
  }
}

void RemoteDevice::_updateConnectionState()
{
  if(!_client.connected())
  {
    if(_connectionState > 1)
    {
      _connectionState = 1;
      _onDisconnectedFromServer(); 
    }
  }
  if(!WiFi.isConnected())
  {
    if(_connectionState > 0)
    {
      _connectionState = 0;
      _onDisconnectedFromWiFi();      
    }
  }
  else
  {
    if(_connectionState == 0)
    {
      _connectionState = 1;
      _onConnectedToWiFi();
    }
  }
}

void RemoteDevice::_tryConnectToServer()
{
  if(millis() - _lastConnectionAttempt < _reconnectionInterval)
  {
    //Not time to try to connect yet.
    return;
  }
	_onTryConnectToServer(_host, _port);
	if(!_client.connect(_host, _port))
	{
    _lastConnectionAttempt = millis();
    _onConnectionToServerFailed();
    return;
	}
	_sendHandshakeData();
  _connectionState = 2;
  _onConnectedToServer();
  _serverTimeout = millis();
}

void RemoteDevice::_sendHandshakeData()
{
	//Send deviceId
	uint64_t deviceId = (uint64_t) _chipId;
	for(int i = 7; i >= 0; --i)
	{
		uint8_t data =(uint8_t) (deviceId >> (i*8));
		_client.write(data);
	}
	//Send deviceType
	for(int i = 3; i >= 0; --i)
	{
		uint8_t data = (uint8_t) (_deviceType >> (i*8));
		_client.write(data);
	}

	//Send deviceVersion
	for(int i = 3; i >= 0; --i)
	{
		uint8_t data = (uint8_t) (_deviceVersion >> (i*8));
		_client.write(data);
	}

	//Send max package size
	for(int i = 1; i >= 0; --i)
	{
		uint8_t data = (uint8_t) (_maxPackageSize >> (i*8));
		_client.write(data);
	}
	//Done writing, flush client.
	_client.flush();
}

void RemoteDevice::_handleWaitingForServerAcceptance()
{
  //Waiting for server acceptance... 
  if(_client.available())
  {
    int response = _client.read();
    if(response == 0)
    {
      //Server has accepted and we move to next state.
      _connectionState = 3;
      _onAcceptedByServer();
    }
    else
    {
      //Server has rejected.
      //Empty buffer
      _client.stop();
      _connectionState = 1;
      _lastConnectionAttempt = millis();
      _onRejectedByServer(response);
    }
  }
  else if(millis() - _serverTimeout > _serverResponseTimeout)
  {
    //Server has not accepted in due time
    _client.stop();
    _connectionState = 1;
    _lastConnectionAttempt = millis();
    _onServerResponseTimedOut();
  }
}

void RemoteDevice::_handleNormalOperation()
{
  //Normal operation...
  if(_client.available())
  {
    if(_bytesRead == 0)
    {
      //Read first byte in the size...
      _packageSize = 0;
      _packageSize += _client.read();
      _packageSize <<= 8;
      ++_bytesRead;
    }
    else if(_bytesRead == 1)
    {
      //read second byte in the size...
      _packageSize += _client.read();
      ++_bytesRead;
      if(_packageSize == 0)
      {
        //This is an empty package...
        _onPackageReceived(_dataBuffer, _packageSize);
        _bytesRead = 0;
        _packageSize = -1;
      }
    } 
    else
    {
      //Read next Databyte
      _dataBuffer[_bytesRead-2] = _client.read();
      ++_bytesRead;
      if(_bytesRead == _packageSize + 2)
      {
        //All bytes have been read.
        _onPackageReceived(_dataBuffer, _packageSize);
        _bytesRead = 0;
        _packageSize = -1;
      }
    }
    _serverTimeout = millis();
  }
  else if(_bytesRead > 0 && millis() - _serverTimeout > _serverResponseTimeout)
  {
    //Server stopped in the middle of sending a package...
    _client.stop();
    _connectionState = 1;
    _onServerResponseTimedOut();
    _onDisconnectedFromServer();
  }
}

void RemoteDevice::_onDisconnected()
{
}

	
void RemoteDevice::setOnConnectedToWiFi(void (*onConnectedToWiFi)())
{
	_onConnectedToWiFi = onConnectedToWiFi;
}

void RemoteDevice::setOnDisconnectedFromWiFi(void (*onDisconnectedFromWiFi)())
{
	_onDisconnectedFromWiFi = onDisconnectedFromWiFi;
}

void RemoteDevice::setOnTryConnectToServer(void (*onTryConnectToServer)(char* host, uint16_t port))
{
	_onTryConnectToServer = onTryConnectToServer;
}

void RemoteDevice::setOnConnectionToServerFailed(void (*onConnectionToServerFailed)())
{
	_onConnectionToServerFailed = onConnectionToServerFailed;
}

void RemoteDevice::setOnConnectedToServer(void (*onConnectedToServer)())
{
	_onConnectedToServer = onConnectedToServer;
}

void RemoteDevice::setOnDisconnectedFromServer(void (*onDisconnectedFromServer)())
{
	_onDisconnectedFromServer = onDisconnectedFromServer;
}

void RemoteDevice::setOnAcceptedByServer(void (*onAcceptedByServer)())
{
	_onAcceptedByServer = onAcceptedByServer;
}

void RemoteDevice::setOnRejectedByServer(void (*onRejectedByServer)(uint8_t errorCode))
{
	_onRejectedByServer = onRejectedByServer;
}

void RemoteDevice::setOnServerResponseTimedOut(void (*onServerResponseTimedOut)())
{
	_onServerResponseTimedOut = onServerResponseTimedOut;
}


