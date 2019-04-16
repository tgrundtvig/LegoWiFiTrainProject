/*
  Author: Tobias Grundtvig
*/

#ifndef TestDevice_h
#define TestDevice_h

#include <Arduino.h>
#include "RemoteDeviceBase.h"

class TestDevice : public RemoteDeviceBase
{
public:
    TestDevice(const uint8_t wiFiResetPin, const uint8_t ledPin);
    bool bootLoop(unsigned long curTime);
    bool sendInitializationPackageLoop(unsigned long curTime);
    void onTryConnectToServer(const char *host, uint16_t port);
    void mainLoop(unsigned long curTime);
    void onPackageReceived(uint8_t dataBuffer[], uint16_t size);
private:
    uint8_t _ledPin;
    bool _curValue;
};

#endif