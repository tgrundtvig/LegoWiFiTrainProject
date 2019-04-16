/*
  Author: Tobias Grundtvig
*/

#ifndef RemoteDeviceBase_h
#define RemoteDeviceBase_h

#include <Arduino.h>
#include <ESP8266WiFi.h>

class RemoteDeviceBase
{
public:
    RemoteDeviceBase(const char *deviceTypeName,
                     const uint16_t deviceType,
                     const uint16_t deviceVersion,
                     const uint16_t maxPackageSize,
                     const uint16_t pingInterval,
                     const uint16_t reconnectInterval,
                     const uint8_t wiFiResetPin,
                     const uint8_t ledPin);

    void update(unsigned long curTime);

    bool sendPackage(uint8_t dataBuffer[], uint16_t index, uint16_t size);
    bool sendPackage(uint8_t dataBuffer[], uint16_t size);

    //Callbacks to be overridden...
    virtual bool bootLoop(unsigned long curTime)
    {
        Serial.println("Override bootLoop to do one-time initialization here.");
        return true;
    }

    virtual bool sendInitializationPackageLoop(unsigned long curTime)
    {
        if (curTime - _lastChange > 5000)
        {
            Serial.println("Override sendInitializationPackage to send the initialization package!");
            _ledValue = !_ledValue;
            digitalWrite(_ledPin, _ledValue);
            _lastChange = curTime;
        }
        return false;
    }

    virtual void noWiFiLoop(unsigned long curTime)
    {
        if (curTime - _lastChange > 50)
        {
            _ledValue = !_ledValue;
            digitalWrite(_ledPin, _ledValue);
            _lastChange = curTime;
        }
    }

    virtual void tryingToConnectLoop(unsigned long curTime)
    {
        if (curTime - _lastChange > 500)
        {
            _ledValue = !_ledValue;
            digitalWrite(_ledPin, _ledValue);
            _lastChange = curTime;
        }
    }

    virtual void mainLoop(unsigned long curTime)
    {
        Serial.print("Main loop called at time: ");
        Serial.println(curTime);
    }

    virtual void onPackageReceived(uint8_t dataBuffer[], uint16_t size)
    {
        Serial.print("Package received. Size: ");
        Serial.println(size);
    }

    virtual void onConnectedToWiFi()
    {
        Serial.print("Connected to WiFi: ");
        Serial.println(WiFi.SSID());
    }

    virtual void onDisconnectedFromWiFi()
    {
        Serial.print("Disconnected from WiFi.");
    }

    virtual void onTryConnectToServer(const char *host, uint16_t port)
    {
        digitalWrite(_ledPin, HIGH);
        Serial.print("Trying to connect to ");
        Serial.print(host);
        Serial.print(':');
        Serial.println(port);
    }

    virtual void onConnectionToServerFailed()
    {
        Serial.println("Connection to server failed.");
    }

    virtual void onConnectedToServer()
    {
        Serial.println("Connected to server.");
    }

    virtual void onDisconnectedFromServer()
    {
        Serial.println("Diconnected from server.");
    }

    virtual void onServerResponseTimeout(const char* waitingFor)
    {
        Serial.print("Server response timed out waiting for: ");
        Serial.println(waitingFor);
    }

    virtual void onClientResponseTimeout(const char* waitingFor)
    {
        Serial.print("Client response timed out waiting for: ");
        Serial.println(waitingFor);
    }

    virtual void onAcceptedByServer()
    {
        Serial.println("Accepted by server.");
    }

    virtual void onRejectedByServer()
    {
        Serial.println("Rejected by server.");
    }

private:
    struct Config
    {
        char hostname[64];
        int port;
    };
    void _startWiFi();
    void _readServerParameters();
    bool _readPackage(unsigned long curTime);
    bool _handlePingPong(unsigned long curTime);
    void _updateConnectionState();
    bool _tryConnectToServer(unsigned long curTime);
    void _sendHandshakePackage();

    bool _loadConfiguration();
    void _saveConfiguration();
    static void _onSaveConfigCallback();

    //Utility function
    static void _writeIntegerToBuffer(uint8_t *buffer, uint64_t data, uint16_t index, uint8_t size);

    // ConnectionState:
    // 0 -> Boot loop,
    // 1 -> Not on WiFi,
    // 2 -> On WiFi, not connected to server,
    // 3 -> Waiting for server accept,
    // 4 -> Accepted by server, waiting to send init package,
    // 5 -> Initializationpackage sent, ready to work.
    // 255 -> Serious error, cannot recover
    uint8_t _connectionState;

    //WiFi connection
    WiFiClient _client;
    Config _config;
    static volatile bool _shouldSaveConfig;
    bool _resetWiFi;

    //Device id and type
    uint32_t _deviceId;
    const char *_deviceTypeName;
    uint16_t _deviceType;
    uint16_t _deviceVersion;

    //Maximum incoming package size
    uint16_t _maxPackageSize;

    //Server reconnect
    unsigned long _lastConnectionAttempt;
    uint16_t _reconnectInterval;

    //State timing
    unsigned long _stateStartTime;

    //PingPong variables
    unsigned long _lastPingReceived;
    uint16_t _pingInterval;

    //Package reading variables.
    uint8_t *_readBuffer;
    int _packageSize;
    int _bytesRead;

    //LED blinking
    unsigned long _lastChange;
    uint8_t _ledPin;
    bool _ledValue;
};

#endif