/*
  Author: Tobias Grundtvig
*/

#ifndef LegoLocomotive_h
#define LegoLocomotive_h


#include <Arduino.h>
#include <RemoteDeviceBase.h>

#define DEVICE_TYPE 2
#define DEVICE_VERSION 1
#define MAX_PACKAGE_SIZE 4

class LegoLocomotive : public RemoteDeviceBase
{
public:
  LegoLocomotive(const uint8_t motor_PWM_pin,
                 const uint8_t motor_DIR_pin,
                 const uint8_t hallSensorAPin,
                 const uint8_t hallSensorBPin,
                 const uint8_t wiFiResetPin,
                 const uint8_t ledPin);

  //Callbacks to be overridden...
  bool bootLoop(unsigned long curTime);

  bool sendInitializationPackageLoop(unsigned long curTime);

  void onPackageReceived(uint8_t dataBuffer[], uint16_t size);

  void onDisconnectedFromServer();

  void mainLoop(unsigned long curTime);

private:
  bool _sendState();
  void _updatePosition();
  bool _changingDirection(long curTime);
  void _updateSpeed(long curTime);
  void _applySpeed();

  //Hallsensor interrupt callbacks
  static void _onHallSensorA();
  static void _onHallSensorB();

  uint8_t _ledPin;
  uint8_t _motor_PWM_pin;
  uint8_t _motor_DIR_pin;
  uint8_t _hallSensorAPin;
  uint8_t _hallSensorBPin;

  //uint8_t _dataBuffer[MAX_PACKAGE_SIZE];

  unsigned long _lastSpeedUpdate;

  // keeping track of direction
  static volatile uint8_t _curDirection;
  bool _waitForDirectionChange;
  unsigned long _waitForDirectionTime;

  //Keeping track position
  long _absolutePosition;
  long _goalPosition;
  long _lastPosition;
  static volatile int _deltaCount;

  //Keeping track of magnets
  static volatile bool _onMagnet;
  static volatile unsigned long _enterMagnetTime;
  static volatile unsigned long _onMagnetTime;

  uint16_t _curSpeed;

  int _minSpeed;
  int _maxSpeed;
  long _lastSpeedChange;

  bool _packageSent;
};

#endif