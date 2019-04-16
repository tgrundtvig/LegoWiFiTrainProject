/*
  Author: Tobias Grundtvig
*/

#ifndef LegoRailroadSwitch_h
#define LegoRailroadSwitch_h

#include <Arduino.h>
#include <AccelStepper.h>
#include <RemoteDeviceBase.h>

class LegoRailroadSwitch : public RemoteDeviceBase
{
public:
    LegoRailroadSwitch(const uint8_t motorPin1,
                       const uint8_t motorPin2,
                       const uint8_t motorPin3,
                       const uint8_t motorPin4,
                       const uint8_t wiFiResetPin,
                       const uint8_t ledPin,
                       const bool isLeftSwitch);

    bool bootLoop(unsigned long curTime);
    bool sendInitializationPackageLoop(unsigned long curTime);
    void mainLoop(unsigned long curTime);
    void onPackageReceived(uint8_t dataBuffer[], uint16_t size);
    
private:
    AccelStepper _stepper;
    const uint16_t _moveRange = 500;
    uint8_t _ledPin;
    uint8_t _isLeftSwitch;
    uint8_t _bootState;
    uint8_t _state;
    uint8_t _nextState;
};

#endif