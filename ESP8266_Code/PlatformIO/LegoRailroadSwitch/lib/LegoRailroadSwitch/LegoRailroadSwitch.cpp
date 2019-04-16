/*
  Author: Tobias Grundtvig
*/

#include "LegoRailroadSwitch.h"

LegoRailroadSwitch::LegoRailroadSwitch(const uint8_t motorPin1,
                                       const uint8_t motorPin2,
                                       const uint8_t motorPin3,
                                       const uint8_t motorPin4,
                                       const uint8_t wiFiResetPin,
                                       const uint8_t ledPin,
                                       const bool isLeftSwitch) : RemoteDeviceBase("LegoRailroadSwitch", 1, 1, 2, 5000, 10000, wiFiResetPin, ledPin),
                                                                  _stepper(8, motorPin1, motorPin3, motorPin2, motorPin4)
{
    _ledPin = ledPin;
    _isLeftSwitch = isLeftSwitch;
    _bootState = 0;
    _state = 0;
    _nextState = 0;
    _stepper.setMaxSpeed(800.0);
    _stepper.setAcceleration(800.0);
    _stepper.setSpeed(400);
    pinMode(ledPin, OUTPUT);
    digitalWrite(ledPin, HIGH);
};

//Callbacks to be overridden...
bool LegoRailroadSwitch::bootLoop(unsigned long curTime)
{
    _stepper.run();
    switch (_bootState)
    {
    case 0:
        Serial.println();
        Serial.println("Boot sequence startet");
        _stepper.moveTo(-_moveRange);
        _bootState = 1;
        Serial.println("Boot state 1");
        return false;
    case 1:
        if (_stepper.distanceToGo() == 0)
        {
            _stepper.moveTo(_moveRange);
            _bootState = 2;
            Serial.println("Boot state 2");
        }
        return false;
    case 2:
        if (_stepper.distanceToGo() == 0)
        {
            _stepper.moveTo(-_moveRange);
            _bootState = 3;
            Serial.println("Boot state 3");
        }
        return false;
    case 3:
        if (_stepper.distanceToGo() == 0)
        {
            Serial.println("Boot sequence finished.");
            _state = 1;
            _nextState = 1;
            return true;
        }
        return false;
    default:
        Serial.print("Invalid boot state: ");
        Serial.println(_bootState);
        return false;
    }
}

bool LegoRailroadSwitch::sendInitializationPackageLoop(unsigned long curTime)
{
    Serial.println("Sending initialization package.");
    uint8_t data[2];
    data[0] = _isLeftSwitch ? 5 : 6;
    data[1] = _state;
    if (sendPackage(data, 2))
    {
        Serial.println("Initialization package sent");
        return true;
    }
    else
    {
        Serial.println("Could not send initialization package.");
        return false;
    }
}

void LegoRailroadSwitch::mainLoop(unsigned long curTime)
{
    _stepper.run();
    if (_nextState != _state && _stepper.distanceToGo() == 0)
    {
        digitalWrite(_ledPin, LOW);
        _state = _nextState;
        uint8_t data[1];
        data[0] = _state;
        Serial.println("Sending state package.");
        sendPackage(data, 1);
    }
}

void LegoRailroadSwitch::onPackageReceived(uint8_t dataBuffer[], uint16_t size)
{
    switch (dataBuffer[0])
    {
    case 0:
        uint8_t data[1];
        data[0] = _state;
        sendPackage(data, 1);
        break;
    case 1:
        if (_state != 1 && _nextState != 1)
        {
            digitalWrite(_ledPin, HIGH);
            _nextState = 1;
            _state = 3;
            _stepper.moveTo(-_moveRange);
        }
        break;
    case 2:
        if (_state != 2 && _nextState != 2)
        {
            digitalWrite(_ledPin, HIGH);
            _nextState = 2;
            _state = 4;
            _stepper.moveTo(_moveRange);
        }
        break;
    default:
        Serial.print("Unknown command: ");
        Serial.println(dataBuffer[0]);
    }
}
