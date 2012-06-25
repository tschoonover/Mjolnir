/*
 * Motor.cpp
 *
 *  Created on: Jan 22, 2012
 *      Author: Leland Green
 *
 *  See MotorDefs.h
 */

#include "Display.h"
#include "State.h"
#include "MotorDefs.h"
#include <Servo.h>
#include <WString.h>
#include <Arduino.h>
#include "Motor.h"

#ifdef DEBUG
#include "HardwareSerial.h"
	extern HardwareSerial Serial;
#endif // DEBUG

extern unsigned long lastMoveTime;
extern SARC::StateHistory *stateHistory;

namespace SARC {

#define min(a,b) ((a)<(b)?(a):(b))
#define max(a,b) ((a)>(b)?(a):(b))

/*
 * @param leftPin The pin number connected to the speed control of your left servo. If using AFMotors, this is ignored.
 *
 * @param rightPin The pin number connected to the speed control of your right servo. If using AFMotors, this is ignored.
 */
Motor::Motor(unsigned int leftPin, unsigned int rightPin) {
	_isMoving = false;
	_delta = DELTA;
#ifdef USE_SERVOS
	_leftTrackServo = new Servo();
	_rightTrackServo = new Servo();
	// init servos
#ifdef USE_LCD
//	display->PrintLine("Init servos.");
#endif
	_leftTrackServo->attach((int) leftPin);
	_rightTrackServo->attach((int) rightPin);
#endif // USE_SERVOS

	#ifdef USE_DC_MOTORS
		_leftMotor = new AF_DCMotor(AF_MOTOR_LEFT, AF_MOTOR_SPEED);
		_rightMotor = new AF_DCMotor(AF_MOTOR_RIGHT, AF_MOTOR_SPEED);
	#endif //USE_DC_MOTORS
}

/*
 * Forces speeds to be within forward and reverse range. This is *without* the relative,
 * which is applied in _one_place,_only_ - the MoveRelative(), just before the motors are
 * actually moved. (Unless you call SetSpeed() and Move() directly, in which case the
 * relative values are *not* applied.)
 */
void Motor::ValidateSpeeds()
{
	if (_leftSpeed > (unsigned int)forward) _leftSpeed = forward;
	if (_leftSpeed < (unsigned int)minimum) _leftSpeed = minimum;

	if (_rightSpeed > (unsigned int)forward) _rightSpeed = forward;
	if (_rightSpeed < (unsigned int)minimum) _rightSpeed = minimum;
}

/*
 * MoveRelative() should always be called unless you're setting the absolute motor speed.
 * Basically, if you're controlling motors with MotorDefs forward or reverse, call MoveRelative.
 * If you're setting the speeds from StateHistory (or otherwise setting a known value), call Move.
 */
void Motor::MoveRelative()
{
#ifdef DEBUG
	Serial.print("MoveRelative() ");
#endif
	ValidateSpeeds();

#ifdef USE_DC_MOTORS // This is only tested with USE_AF_MOTORS.
	if (_leftSpeed <= reverse)
		_leftActualSpeed = (unsigned int) map(_leftSpeed, reverse, reverse - relative, minimum, maximum);
	else
		_leftActualSpeed = (unsigned int) map(_leftSpeed, forward - relative, forward, minimum, maximum);
	if (_rightSpeed <= reverse)
		_rightActualSpeed = (unsigned int) map(_rightSpeed, reverse, reverse - relative, minimum, maximum);
	else
		_rightActualSpeed = (unsigned int) map(_rightSpeed, forward - relative, forward, minimum, maximum);
#else
	_leftActualSpeed = _leftSpeed;
	_rightActualSpeed = _rightSpeed;
#endif
	Move();

#ifdef DEBUG
	Serial.print("MoveRelative() (After Move) ");
#endif
}

/*
 * This method sets the relative speed of the left and right motors.
 * This method could be used for moving forward or reverse, if the speeds are the same.
 */
void Motor::Turn(unsigned int newLeftSpeed, unsigned int newRightSpeed)
{
#ifdef DEBUG
	Serial.print("Turn() - newLeftSpeed = "); Serial.print(newLeftSpeed);
	Serial.print(", newRightSpeed = "); Serial.println(newRightSpeed);
#endif
	_leftSpeed = newLeftSpeed;
	_rightSpeed = newRightSpeed;
	Move();

}

/*
 * "Accelerates" to the left by specified _delta amount. Note that the left motor
 * speed is *decreased* by this amount and the right motor speed is *increased*
 * by it.
 */
void Motor::TurnLeft(unsigned int delta)
{
	_rightSpeed += delta;
	if (delta >= _leftSpeed) _leftSpeed = reverse - relative;
	else _leftSpeed -= delta;
	ValidateSpeeds();
#ifdef DEBUG
	Serial.print("TurnLeft "); Serial.println(delta);
#endif
	MoveRelative();
}

/*
 * "Accelerates" to the right by specified delta amount. Note that the right motor
 * speed is *decreased* by this amount and the left motor speed is *increased*
 * by it.
 */
void Motor::TurnRight(unsigned int delta)
{
	_leftSpeed += delta;
	if (delta >= _rightSpeed) _rightSpeed = reverse - relative;
	else _rightSpeed -= delta;
	ValidateSpeeds();
#ifdef DEBUG
	Serial.print("TurnRight "); Serial.println(delta);
#endif
	MoveRelative();
}


// TODO: Add support for direction (probably with another overload of Move())
// Note that I wanted to have an overloaded Move() here, but there is apparently
// a bug in avr-gcc that says Move() is ambiguous.
/*
 * SetSpeeds() sets the absolute motion. This does NOT affect the tracked speeds stored internally
 * (in private variables).
 *
 * This is implemented primarily to allow the history module to backtrack by setting absolute speeds.
 * You can call it if you like, but be aware that calling the "MoveForward" and other methods will
 * override these values the first time they're called.
 */
void Motor::SetSpeeds(unsigned int newLeftSpeed =neutral,
		unsigned int newRightSpeed = neutral)
{
//#ifdef DEBUG
//	Serial.print("SetSpeeds() ");
//	ShowSpeeds();
//#endif
	_leftActualSpeed = newLeftSpeed;
	_rightActualSpeed = newRightSpeed;
	Move();
}

void Motor::AccelerateForward(unsigned int delta)
{
#ifdef DEBUG
	Serial.print("AccelerateForward () - delta = "); Serial.println(delta);
#endif
	_delta = delta;
	MoveForward();
}

void Motor::AccelerateReverse(unsigned int delta)
{
#ifdef DEBUG
	Serial.print("AccelerateReverse () - delta = "); Serial.println(delta);
#endif
	_delta = delta;
	MoveReverse();
}

void Motor::MoveForwardFullSpeed(void)
{
#ifdef DEBUG
	Serial.println("MoveForwardFullSpeed ()");
#endif
	_leftSpeed = forward;
	_rightSpeed = forward;
	MoveRelative();
}

void Motor::MoveReverseFullSpeed(void)
{
#ifdef DEBUG
	Serial.println("MoveReverse ()");
#endif
	_leftSpeed = reverse - relative;
	_rightSpeed = reverse - relative;
	MoveRelative();
}

void Motor::TurnRightFullSpeed(void)
{
//#ifdef DEBUG
//	Serial.println("TurnRightFullSpeed ()");
//#endif
	_leftSpeed = forward;
	_rightSpeed = reverse - relative;
	MoveRelative();
}

void Motor::TurnLeftFullSpeed(void)
{
//#ifdef DEBUG
//	Serial.println("TurnLeftFullSpeed ()");
//#endif
	_leftSpeed = reverse - relative;
	_rightSpeed = forward;
	MoveRelative();
}

/*
 * Sets both left and right speed to neutral, as defined in MotorDefs.h.
 * Note that if the motor has a "braking" mode (as some servos and steppers do),
 * you should call Brake() instead.
 */
void Motor::StopMovement(void)
{
//	#ifdef DEBUG
//		Serial.println("StopMovement ()");
//	#endif
	_leftSpeed = neutral;
	_rightSpeed = neutral;
	MoveRelative();
}

/*
 * Sets both speeds to brake, as defined in Motordefs.h. For DC motors, this will
 * have the same effect as StopMovement(). However, for some servos - e.g.
 * Vex Robotics servos - this will actively engage the motors to hold them still. This
 * may be desired if you have a manipulator, or are positioned on a steep incline.
 *
 * *** NOTE *** This sets the _leftSpeed and _rightSpeed members to neutral. This is
 * so that the next call to one of the movement methods will accelerate from "stopped",
 * not from "brake" because brake is outside the normal operating range.
 */
void Motor::Brake(void)
{
	_leftSpeed = neutral;
	_rightSpeed = neutral;
	SetSpeeds(brake, brake);
}

/*
 * Sets both left and right speeds to be the same. The speed will be whichever is
 * currently the highest, but in a forward direction. This is to "center the wheel"
 * while in the middle of a turn.
 */
void Motor::SteerCenter(void)
{
	if (_leftSpeed > _rightSpeed) _rightSpeed = _leftSpeed;
	else _leftSpeed = _rightSpeed;
	MoveRelative();
}

bool Motor::IsMoving(void)
{
	return _isMoving;
}

///////////////////////////////////////////////// Protected methods:

void Motor::MoveForward(void)
{
//#ifdef DEBUG
//	Serial.println("MoveForward()");
//#endif
	_leftSpeed = min(forward, _leftSpeed + _delta);
	_rightSpeed = min(forward, _rightSpeed + _delta);
	MoveRelative();
}

void Motor::MoveReverse(void)
{
//#ifdef DEBUG
//	Serial.println("MoveReverse()");
//#endif
	if (_delta > _leftSpeed)
		_leftSpeed = 0;
	else
		_leftSpeed = max(minimum, _leftSpeed - _delta);
	if (_delta > _rightSpeed)
		_rightSpeed = 0;
	else
		_rightSpeed = max(minimum, _rightSpeed - _delta);

	MoveRelative();
}

/*
 * This method is the one that actually sends the commands to the motors.
 * It is protected, and you shouldn't need to call it, unless you create a subclass.
 */
void Motor::Move(void)
{
	#ifdef DEBUG
		Serial.print("Move() ");
	#endif

	#ifdef USE_SERVOS
		_leftTrackServo->writeMicroseconds(_leftActualSpeed);
		_rightTrackServo->writeMicroseconds(_rightActualSpeed);
	#endif

	#ifdef USE_DC_MOTORS
		_leftMotor->setSpeed(_leftActualSpeed);
		if (_leftSpeed < neutral)
			_leftMotor->run(BACKWARD); // Note that BACKWARD & FORWARD are defined in AFMotor.h
		else
			_leftMotor->run(FORWARD);

		_rightMotor->setSpeed(_rightActualSpeed);
		if (_rightSpeed < neutral)
			_rightMotor->run(BACKWARD);
		else
			_rightMotor->run(FORWARD);
	#endif

	if (_leftSpeed == neutral && _rightSpeed == neutral)
		_isMoving = false;
	else
		_isMoving = true;

	lastMoveTime = millis();

	// TODO: Update state with optional current heading (using compass &/or GPS module(s)).
	State* currentState = new SARC::State((unsigned int)0, (unsigned long)0, (unsigned int)_leftActualSpeed, (unsigned int)_rightActualSpeed);
	stateHistory->AddState(*currentState);

	#ifdef USE_LCD
//		display->PrintLine("Left Track = "); display->Print(String(_leftActualSpeed, DEC));
//		display->PrintLine("Right Track = "); display->Print(String(_rightActualSpeed, DEC));
//		display->PrintLine("isMoving = "); display->Print(String(_isMoving, BIN));
	#endif // USE_LCD
};

} /* namespace SARC */
