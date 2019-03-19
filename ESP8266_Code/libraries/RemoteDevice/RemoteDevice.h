/*
  RemoteDevice.h - TODO: Document...
*/
#ifndef RemoteDevice_h
#define RemoteDevice_h

#include "Arduino.h"
#include <ESP8266WiFi.h>
#include <WiFiManager.h>

class RemoteDevice
{
	public:
		RemoteDevice(	void (*onPackageReceived) (uint8_t data[], uint16_t size),
						bool (*onSendInitializationPackage)(),
						uint32_t deviceType,
						uint32_t deviceVersion,
						char* typeName,
						uint16_t maxPackageSize										);
		void start(char* host, uint16_t port, bool resetWiFi);
		bool sendPackage(uint8_t data[], uint16_t size);
		void update();
    // ConnectionState:
    // 0 -> Not on WiFi,
    // 1 -> On WiFi, not connected to server,
    // 2 -> Waiting for server accept,
    // 3 -> Accepted by server, waiting to send init package, 
    // 4 -> Initializationpackage sent, ready to work.
    int getConnectionState();
    
		void setOnConnectedToWiFi(void (*onConnectedToWiFi)());
		void setOnDisconnectedFromWiFi(void (*onDisconnectedFromWiFi)());
		void setOnTryConnectToServer(void (*onTryConnectToServer)(char* host, uint16_t port));
		void setOnConnectionToServerFailed(void (*onConnectionToServerFailed)());
		void setOnConnectedToServer(void (*onConnectedToServer)());
		void setOnDisconnectedFromServer(void (*onDisconnectedFromServer)());
		void setOnAcceptedByServer(void (*onAcceptedByServer)());
		void setOnRejectedByServer(void (*onRejectedByServer)(uint8_t errorCode));
		void setOnServerResponseTimedOut(void (*onServerResponseTimedOut)());
		
	private:
	
		void (*_onPackageReceived) (uint8_t data[], uint16_t size);
		bool (*_onSendInitializationPackage)();
		
		void (*_onConnectedToWiFi)();
		void (*_onDisconnectedFromWiFi)();
		void (*_onTryConnectToServer)(char* host, uint16_t port);
		void (*_onConnectionToServerFailed)();
		void (*_onConnectedToServer)();
		void (*_onDisconnectedFromServer)();
		void (*_onAcceptedByServer)();
		void (*_onRejectedByServer)(uint8_t errorCode);
		void (*_onServerResponseTimedOut)();
		
    
    WiFiClient _client;
		char* _host;
		uint16_t _port;	
		char* _typeName;
		uint32_t _chipId;
		uint32_t _deviceType;
		uint32_t _deviceVersion;
		uint16_t _maxPackageSize;
		
		long _serverResponseTimeout;
		uint8_t* _dataBuffer;
		int _packageSize;
		int _bytesRead;
		unsigned long _lastConnectionAttempt;
		unsigned long _serverTimeout;
		unsigned long _reconnectionInterval;
		// ConnectionState:
    // 0 -> Not on WiFi,
    // 1 -> On WiFi, not connected to server,
    // 2 -> Waiting for server accept,
    // 3 -> Accepted by server, waiting to send init package, 
    // 4 -> Initializationpackage sent, ready to work.
		int _connectionState = 0;
		
		void _updateConnectionState();
    void _tryConnectToServer();
    void _sendHandshakeData();
    void _handleWaitingForServerAcceptance();
    void _handleNormalOperation();
    void _onDisconnected();
};

#endif