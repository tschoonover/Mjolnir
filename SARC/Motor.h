/*
 * Motor.h
 *
 *  Created on: Jan 22, 2012
 *      Author: Leland Green
 */

#ifndef MOTOR_H_
#define MOTOR_H_

#include "MotorDefs.h"

namespace SARC {

/*
 *
 */
class Motor {
public:
	Motor(unsigned int, unsigned int);
//	virtual ~Motor();
	void Move();
	void MoveForward();
	void MoveReverse();
	void TurnLeft();
	void TurnRight();
	void MoveForwardFullSpeed();
	void MoveReverseFullSpeed();
	void StopMovement();

	bool IsMoving();

private:
	bool _isMoving;
	unsigned int _leftTrackSpeed;
	unsigned int _rightTrackSpeed;

#ifdef USE_SERVOS
	Servo *_leftTrackServo;
	Servo *_rightTrackServo;
#endif // USE_SERVOS

	#ifdef USE_DC_MOTORS
		AF_DCMotor* _leftMotor;
		AF_DCMotor* _rightMotor;
	#endif // USE_DC_MOTORS

};

} /* namespace SARC */
#endif /* MOTOR_H_ */
