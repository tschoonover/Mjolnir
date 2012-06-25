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
	void MoveRelative(void);
	void SetSpeeds(unsigned int, unsigned int);
	void ValidateSpeeds(void);
	void Turn(unsigned int, unsigned int);
	void TurnLeftFullSpeed(void);
	void TurnRightFullSpeed(void);
	void TurnLeft(unsigned int);
	void TurnRight(unsigned int);
	void AccelerateForward(unsigned int);
	void AccelerateReverse(unsigned int);
	void MoveForwardFullSpeed(void);
	void MoveReverseFullSpeed(void);
	void StopMovement(void);
	void Brake(void);
	void SteerCenter(void);
	bool IsMoving(void);

protected:
	void Move(void);
	void MoveForward(void);
	void MoveReverse(void);

private:
	bool _isMoving;
	unsigned int _delta;
	unsigned int _leftActualSpeed;
	unsigned int _rightActualSpeed;
	unsigned int _leftSpeed;
	unsigned int _rightSpeed;

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
