/*
  Author: Tobias Grundtvig
*/

#include <Arduino.h>
#include "LegoLocomotive.h"

volatile uint8_t LegoLocomotive::_curDirection;
volatile int LegoLocomotive::_deltaCount;
volatile bool LegoLocomotive::_onMagnet;
volatile unsigned long LegoLocomotive::_enterMagnetTime;
volatile unsigned long LegoLocomotive::_onMagnetTime;

LegoLocomotive::LegoLocomotive(const uint8_t motor_PWM_pin,
                               const uint8_t motor_DIR_pin,
                               const uint8_t hallSensorAPin,
                               const uint8_t hallSensorBPin,
                               const uint8_t wiFiResetPin,
                               const uint8_t ledPin) : RemoteDeviceBase("LegoLocomotive", DEVICE_TYPE, DEVICE_VERSION, MAX_PACKAGE_SIZE, 5000, 10000, wiFiResetPin, ledPin)
{
    _motor_PWM_pin = motor_PWM_pin;
    _motor_DIR_pin = motor_DIR_pin;
    _hallSensorAPin = hallSensorAPin;
    _hallSensorBPin = hallSensorBPin;
    _ledPin = ledPin;

    _curDirection = 0;
    _waitForDirectionChange = false;
    _waitForDirectionTime = 0;

    _absolutePosition = 0;
    _goalPosition = 0;
    _lastPosition = 0;
    _deltaCount = 0;

    _onMagnet = false;
    _enterMagnetTime = 0;
    _onMagnetTime = 0;

    _curSpeed = 0;

    _minSpeed = 512;
    _maxSpeed = 800;

    _lastSpeedChange = 0;
    _lastSpeedUpdate = 0;

    _packageSent = false;

    pinMode(ledPin, OUTPUT);
    pinMode(_motor_PWM_pin, OUTPUT);
    pinMode(_motor_DIR_pin, OUTPUT);
    pinMode(_hallSensorAPin, INPUT_PULLUP);
    pinMode(_hallSensorBPin, INPUT_PULLUP);
    attachInterrupt(digitalPinToInterrupt(_hallSensorAPin), LegoLocomotive::_onHallSensorA, FALLING);
    attachInterrupt(digitalPinToInterrupt(_hallSensorBPin), LegoLocomotive::_onHallSensorB, FALLING);
    digitalWrite(ledPin, HIGH);
};

//Callbacks to be overridden...
bool LegoLocomotive::bootLoop(unsigned long curTime)
{
    //ToDo: Measure start force with current load and battery state.
    return true;
}

bool LegoLocomotive::sendInitializationPackageLoop(unsigned long curTime)
{
    return _sendState();
}

void LegoLocomotive::onPackageReceived(uint8_t dataBuffer[], uint16_t size)
{
    //Serial.println("Package received!");
    long newTargetPosition = dataBuffer[0];
    newTargetPosition <<= 8;
    newTargetPosition += dataBuffer[1];
    newTargetPosition <<= 8;
    newTargetPosition += dataBuffer[2];
    newTargetPosition <<= 8;
    newTargetPosition += dataBuffer[3];
    Serial.print("Got a new target position: ");
    Serial.println(newTargetPosition);
    _goalPosition = newTargetPosition;
}

void LegoLocomotive::onDisconnectedFromServer()
{
    _curSpeed = 0;
    _applySpeed();
}

void LegoLocomotive::mainLoop(unsigned long curTime)
{
    digitalWrite(_ledPin, !_onMagnet);

    //Update position, if it has changed
    if (_deltaCount != 0)
    {
        _updatePosition();
    }

    if (!_packageSent)
    {
        _packageSent = _sendState();
    }

    //Are we changing direction?
    if (_changingDirection(curTime))
    {
        return;
    }
    //Update speed if it has been too long time or if position has changed
    if (curTime - _lastSpeedUpdate > 1000 || _absolutePosition != _lastPosition)
    {
        _updateSpeed(curTime);
    }
    _applySpeed();
}

bool LegoLocomotive::_sendState()
{
    uint8_t data[8];
    data[0] = _absolutePosition >> 24;
    data[1] = _absolutePosition >> 16;
    data[2] = _absolutePosition >> 8;
    data[3] = _absolutePosition;
    data[4] = _onMagnetTime >> 24;
    data[5] = _onMagnetTime >> 16;
    data[6] = _onMagnetTime >> 8;
    data[7] = _onMagnetTime;
    return sendPackage(data, 8);
}

void LegoLocomotive::_updatePosition()
{
    _absolutePosition += _deltaCount;
    _deltaCount = 0;
    _packageSent = false; //Sent new package
}

bool LegoLocomotive::_changingDirection(long curTime)
{
    if (!_waitForDirectionChange)
    {
        return false;
    }
    if (curTime - _waitForDirectionTime < 2000)
    {
        return true;
    }
    _waitForDirectionChange = false;
    if (_curDirection == 0)
    {
        _curDirection = 1;
        if (_onMagnet)
        {
            if (!digitalRead(_hallSensorAPin))
            {
                _onMagnet = false;
            }
            else
            {
                ++_deltaCount;
            }
        }
        else // !onMagnet
        {
            if (!digitalRead(_hallSensorAPin))
            {
                if (!digitalRead(_hallSensorBPin))
                {
                    --_deltaCount;
                }
                else
                {
                    _onMagnet = true;
                }
            }
        }
    }
    else //curDirection == 1
    {
        _curDirection = 0;
        if (_onMagnet)
        {
            if (!digitalRead(_hallSensorBPin))
            {
                _onMagnet = false;
            }
            else
            {
                --_deltaCount;
            }
        }
        else // !onMagnet
        {
            if (!digitalRead(_hallSensorAPin))
            {
                if (!digitalRead(_hallSensorBPin))
                {
                    ++_deltaCount;
                }
                else
                {
                    _onMagnet = true;
                }
            }
        }
    }
    return false;
}

void LegoLocomotive::_updateSpeed(long curTime)
{
    //Check if we need to change direction...
    if (_curSpeed == 0 && _goalPosition > _absolutePosition && _curDirection == 1)
    {
        _waitForDirectionChange = true;
        _waitForDirectionTime = curTime;
        return;
    }

    if (_curSpeed == 0 && _goalPosition < _absolutePosition && _curDirection == 0)
    {
        _waitForDirectionChange = true;
        _waitForDirectionTime = curTime;
        return;
    }

    //Are we at the position
    if (_goalPosition == _absolutePosition)
    {
        _curSpeed = 0;
        _lastPosition = _absolutePosition;
        return;
    }

    //Should we update the speedsetting?
    if (_absolutePosition != _lastPosition || curTime - _lastSpeedUpdate > 1000)
    {
        _lastSpeedUpdate = curTime;
        if (_absolutePosition == _lastPosition) //We seem to be stopped
        {
            ++_curSpeed;
            if (_curSpeed > 10)
                _curSpeed = 10;
            return;
        }
        if (_curDirection == 0)
        {
            if (_goalPosition - _absolutePosition > _curSpeed)
            {
                ++_curSpeed;
                if (_curSpeed > 10)
                    _curSpeed = 10;
            }
            else
            {
                --_curSpeed;
                if (_curSpeed < 1)
                    _curSpeed = 1;
            }
        }
        else if (_curDirection == 1)
        {
            if (_absolutePosition - _goalPosition > _curSpeed)
            {
                ++_curSpeed;
                if (_curSpeed > 10)
                    _curSpeed = 10;
            }
            else
            {
                --_curSpeed;
                if (_curSpeed < 1)
                    _curSpeed = 1;
            }
        }
        _lastPosition = _absolutePosition;
    }
}

void LegoLocomotive::_applySpeed()
{
    int speedValue = 0;
    if (_curSpeed > 0)
    {
        if (_curSpeed == 10)
        {
            speedValue = _maxSpeed;
        }
        else
        {
            int d = (_maxSpeed - _minSpeed) / 9;
            speedValue = _minSpeed + (_curSpeed - 1) * d;
        }
    }
    digitalWrite(_motor_DIR_pin, _curDirection);
    analogWrite(_motor_PWM_pin, speedValue);
}

//Hallsensor interrupt callbacks
void LegoLocomotive::_onHallSensorA()
{
    if (_curDirection == 0 && !_onMagnet)
    {
        _onMagnet = true;
        _enterMagnetTime = millis();
    }
    else if (_curDirection == 1 && _onMagnet)
    {
        _onMagnet = false;
        _onMagnetTime = millis() - _enterMagnetTime;
        --_deltaCount;
    }
}

void LegoLocomotive::_onHallSensorB()
{
    if (_curDirection == 1 && !_onMagnet)
    {
        _onMagnet = true;
        _enterMagnetTime = millis();
    }
    else if (_curDirection == 0 && _onMagnet)
    {
        _onMagnet = false;
        _onMagnetTime = millis() - _enterMagnetTime;
        ++_deltaCount;
    }
}