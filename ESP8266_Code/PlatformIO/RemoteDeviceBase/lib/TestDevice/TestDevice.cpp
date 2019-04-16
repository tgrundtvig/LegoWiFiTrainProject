/*
  Author: Tobias Grundtvig
*/

#include "TestDevice.h"

TestDevice::TestDevice(const uint8_t wiFiResetPin, const uint8_t ledPin) : RemoteDeviceBase("TestDevice", 0, 1, 1, 500, 10000, wiFiResetPin, ledPin)
{
    _ledPin = ledPin;
    pinMode(ledPin, OUTPUT);
    _curValue = false;
    digitalWrite(ledPin, _curValue);
}

bool TestDevice::bootLoop(unsigned long curTime)
{
    Serial.println("Override bootLoop to do one-time initialization here.");
    return true;
}

bool TestDevice::sendInitializationPackageLoop(unsigned long curTime)
{
    Serial.println("Sending initialization package.");
    uint8_t tmp[1];
    tmp[0] = _curValue;
    return sendPackage(tmp, 1);
}

void TestDevice::onTryConnectToServer(const char *host, uint16_t port)
{
    digitalWrite(_ledPin, HIGH);
    Serial.print("Trying to connect to ");
    Serial.print(host);
    Serial.print(':');
    Serial.println(port);
}

void TestDevice::mainLoop(unsigned long curTime)
{
}

void TestDevice::onPackageReceived(uint8_t dataBuffer[], uint16_t size)
{
    //Serial.print("Package received. Size: ");
    //Serial.println(size);
    _curValue = dataBuffer[0];
    digitalWrite(_ledPin, _curValue);
    uint8_t tmp[1];
    tmp[0] = _curValue;
    sendPackage(tmp, 1);
}