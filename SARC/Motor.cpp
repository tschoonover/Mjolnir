/*
 * Motor.cpp
 *
 *  Created on: Jan 22, 2012
 *      Author: Leland Green
 *
 *  See MotorDefs.h
 */

//#include <iterator>
//#include <vector>
//#include <map>
//
//#include <algorithm>

#include "Display.h"
#include "State.h"
#include "Motor.h"
#include <Servo.h>
#include <WString.h>
#include <Arduino.h>

extern Display *display;
extern unsigned long lastMoveTime;
extern SARC::StateHistory *stateHistory;

namespace SARC {

#define min(a,b) ((a)<(b)?(a):(b))
#define max(a,b) ((a)>(b)?(a):(b))

Motor::Motor(unsigned int leftPin, unsigned int rightPin) {
	_isMoving = false;
#ifdef USE_SERVOS
	this->_leftTrackServo = new Servo();
	this->_rightTrackServo = new Servo();
	// init servos
	display->PrintLine("Initializing servos.");
	this->_leftTrackServo->attach((int) leftPin);
	this->_rightTrackServo->attach((int) rightPin);
#endif // USE_SERVOS

	#ifdef USE_DC_MOTORS
		this->_leftMotor = new AF_DCMotor(AF_MOTOR_LEFT, AF_MOTOR_SPEED);
		this->_rightMotor = new AF_DCMotor(AF_MOTOR_RIGHT, AF_MOTOR_SPEED);
	#endif //USE_DC_MOTORS
}

//Motor::~Motor() {
//}

/*
 * This method is the one that actually sends the commands to the motors.
 */
void Motor::Move()
{
	#ifdef USE_SERVOS
		this->_leftTrackServo->writeMicroseconds(this->_leftTrackSpeed);
		this->_rightTrackServo->writeMicroseconds(this->_rightTrackSpeed);
		if ((this->_leftTrackSpeed == MotorDefs::neutral) && (this->_rightTrackSpeed == MotorDefs::neutral))
			_isMoving = false;
		else
			_isMoving = true;
	#endif

	#ifdef USE_DC_MOTORS
		leftMotor->setSpeed((uint8_t)abs(this->_leftTrackSpeed));
		if (this->_leftTrackSpeed < MotorDefs::neutral)
			leftMotor->run(BACKWARD); // Note that BACKWARD & FORWARD are defined in AFMotor.h
		else
			leftMotor->run(FORWARD);

		rightMotor->setSpeed((uint8_t)abs(this->_rightTrackSpeed));
		if (this->_rightTrackSpeed < MotorDefs::neutral)
			rightMotor->run(BACKWARD);
		else
			rightMotor->run(FORWARD);
	#endif

	lastMoveTime = millis();

	// TODO: Update state with optional current heading (using compass or GPS module(s)).
	State* currentState = new SARC::State((int)0, (unsigned long)0, (int)this->_leftTrackSpeed, (int)this->_rightTrackSpeed);
	stateHistory->AddState(currentState);

	#ifdef USE_LCD
		display->PrintLine("Left Track = ");
		display->Print(this->_leftTrackSpeed);
		display->PrintLine("Right Track = ");
		display->Print(this->_rightTrackSpeed);
		display->PrintLine("isMoving = ");
		display->Print(_isMoving, BIN);
	#endif // USE_LCD
};

void Motor::MoveForward()
{
	this->_leftTrackSpeed = min(MotorDefs::forward, this->_leftTrackSpeed + MotorDefs::delta);
	this->_rightTrackSpeed = min(MotorDefs::forward, this->_rightTrackSpeed + MotorDefs::delta);
	Move();
}

void Motor::MoveReverse()
{
	this->_leftTrackSpeed = max(MotorDefs::reverse, this->_leftTrackSpeed - MotorDefs::delta);
	this->_rightTrackSpeed = max(MotorDefs::reverse, this->_rightTrackSpeed - MotorDefs::delta);
	Move();
}

void Motor::MoveForwardFullSpeed(void)
{
	this->_leftTrackSpeed = MotorDefs::forward;
	this->_rightTrackSpeed = MotorDefs::forward;
	Move();
}

void Motor::MoveReverseFullSpeed(void)
{
	this->_leftTrackSpeed = MotorDefs::reverse;
	this->_rightTrackSpeed = MotorDefs::reverse;
	Move();
}

void Motor::TurnLeft()
{
	this->_leftTrackSpeed = max(MotorDefs::reverse, this->_leftTrackSpeed - MotorDefs::delta);
	this->_rightTrackSpeed = min(MotorDefs::forward, this->_rightTrackSpeed + MotorDefs::delta);
	Move();
}

void Motor::TurnRight()
{
	this->_leftTrackSpeed = min(MotorDefs::forward, this->_leftTrackSpeed + MotorDefs::delta);
	this->_rightTrackSpeed = max(MotorDefs::reverse, this->_rightTrackSpeed - MotorDefs::delta);
	Move();
}

void Motor::StopMovement()
{
	this->_leftTrackSpeed = MotorDefs::neutral;
	this->_rightTrackSpeed = MotorDefs::neutral;
	Move();
}

bool Motor::IsMoving()
{
	return _isMoving;
}
} /* namespace SARC */
