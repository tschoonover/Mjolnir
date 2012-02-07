/* State and related classes originally written for Mjolnir project
 * by Leland Green in January 2012.
 *
 * The overall goal of this portion is to encapsulate state for a
 * moment in time. Mjolnir uses a track-based (tank) robot. At first
 * glance, this may seem simpler than a wheel-based device, but in
 * some ways, it's more complicated. For example, to turn, the left
 * and right tracks go in different _directions. This would also
 * work with a four-wheel robot, but is not required.
 *
 * Because of this, there is no concept of "steering".
 *
 * This is also written to be "motor agnostic". The definitions for
 * speed values are in MotorDefs.h.
 *
 * This file is free software; you can redistribute it and/or modify
 * it under the terms of either the GNU General Public License version 2
 * or the GNU Lesser General Public License version 2.1, both as
 * published by the Free Software Foundation.
 *
 *
 */

#include <iterator>
#include <vector>
#include <map>
//#include "pnew.cpp"
#include "pnew.h"
#include <algorithm>

#include "State.h"
#include "MotorDefs.h"
#include "ArduinoUtils.h"
#include "Arduino.h"

#ifndef MAX_HISTORY
#define MAX_HISTORY 100
#endif


namespace SARC {

/*
 * State class is an encapsulation of the current _direction and speed
 * for a simple agent. Since it is immutable, a class factory is provided
 * (below) that copies a state and can also return an "opposite _direction"
 * state, which is useful for backtracking.
 * This class is immutable except for the duration. This was done to allow
 * updating the duration when two states are presented in the history that
 * have the same direction and speed.
 */
State::State(const State& state)
{
	_direction = state._direction;
	_duration = state._duration;
	_leftSpeed = state._leftSpeed;
	_rightSpeed = state._rightSpeed;
	_previousTick = state._previousTick;
}

State::State(unsigned int newDirection = 0, unsigned long newDuration = 0,
		unsigned int newLeftSpeed = MotorDefs::neutral , unsigned int newRightSpeed = MotorDefs::neutral)
{
	_direction = newDirection;
	_leftSpeed = newLeftSpeed;
	_rightSpeed = newRightSpeed;
	_previousTick = micros();
}

int State::getDirection(void)
{
	return _direction;
}

bool State::isLeftForward(void) {return _leftSpeed > MotorDefs::neutral;}

int State::getLeftSpeed(void) {return _leftSpeed;}

bool State::isRightForward(void) {return _rightSpeed > MotorDefs::neutral;}

int State::getRightSpeed(void) {return _rightSpeed;}

void State::setDuration(unsigned long duration) {_duration = duration;}

unsigned long State::getDuration(void) {return _duration;}

// For equality, we only compare the _direction and speed(s) - not duration.
bool State::operator==(const State& state)
{
	return state._direction == _direction
			&& state._leftSpeed == _leftSpeed
			&& state._rightSpeed == _rightSpeed;
}


void State::CopyReverse(State& souceState)
{
	souceState._leftSpeed = (isLeftForward())?
			MotorDefs::neutral - (getLeftSpeed() - MotorDefs::neutral) :
			MotorDefs::neutral + (MotorDefs::neutral - getLeftSpeed());

	souceState._rightSpeed =  (isRightForward())?
			MotorDefs::neutral - (getRightSpeed() - MotorDefs::neutral) :
			MotorDefs::neutral + (MotorDefs::neutral - getRightSpeed());
}

//const State& State::operator *(const State* state)
//{
//	return *state;
//}

///////////////////////////////////////////////////////////////////////////////

/*
 * @Class
 */


StateHistory::StateHistory(unsigned int historySize)
{
	SetHistorySize(historySize);
	_previousTick = micros();
};

unsigned int StateHistory::SetHistorySize(unsigned int historySize)
{
	_vector.resize(historySize);
	return _vector.size();
}

/*
 * Adds a State. This is done in an intelligent way by comparing the previous
 * state to the new one. If the States are different only by time, the
 * previous State is updated by adding the time (duration) of the two States.
 * The size of the wrapped vector is also managed. If the current size is >=
 * MAX_HISTORY, the oldest element is removed.
 * @param: state A reference to a state object.
 * @return: size_t The number of States in this history.
 */
int StateHistory::AddState(State state)
{
	unsigned long tickNow = micros();
	State& prevState = _vector[_vector.size() - 1];
	if (prevState == state)
	{
		prevState.setDuration( timeDifference(prevState.getDuration(), tickNow) );
	}
	else
	{
		if (_vector.size() >= MAX_HISTORY)
		{
			_vector.erase(_vector.begin());
		}
		prevState.setDuration(timeDifference(prevState.getDuration(), timeDifference(_previousTick, tickNow)));
		_vector.push_back(state);
	}
	_previousTick = tickNow;
	return _vector.size();
}

std::vector<State>::reverse_iterator StateHistory::BacktrackIterator (unsigned int lastState)
{
	return _vector.rbegin();
}

std::vector<State>::reverse_iterator StateHistory::BacktrackIteratorEnd ()
{
	return _vector.rend();
}

void StateHistory::SetCurrent(std::vector<State>::reverse_iterator reverseIterator)
{
	if (reverseIterator != _vector.rbegin() && reverseIterator != _vector.rend())
	{
		std::vector<State>::reverse_iterator riter = _vector.rbegin();
		_vector.erase((State *)&(*riter), (State *)&(*reverseIterator));
	}
}

unsigned int StateHistory::GetHistorySize(void)
{
	return _vector.size();
}

///////////////////////////////////////////////////////////////////////////////

//State* StateFactory::CopyState(State* stateSource)
//{
//	return new SARC::State(stateSource->getDirection(), stateSource->getDuration(),
//					 stateSource->getLeftSpeed(), stateSource->getRightSpeed());
//};
//
///*
// * Allocates and returns a new State that has the opposite _direction of the ref_state->
// *
// * This assumes:
// * 		neutral - (forward - neutral) = reverse
// * 		neutral + (neutral - reverse) = forward
// *
// * @See: MotorDefs.h
// */
//State* StateFactory::OppositeDirection (State* ref_state)
//{
//	int new_left_speed, new_right_speed;
//	new_left_speed =  (ref_state->isLeftForward())?
//			MotorDefs::neutral - (ref_state->getLeftSpeed() - MotorDefs::neutral) :
//			MotorDefs::neutral + (MotorDefs::neutral - ref_state->getLeftSpeed());
//
//	new_right_speed =  (ref_state->isRightForward())?
//			MotorDefs::neutral - (ref_state->getRightSpeed() - MotorDefs::neutral) :
//			MotorDefs::neutral + (MotorDefs::neutral - ref_state->getRightSpeed());
//
//	return new State((ref_state->getDirection() + 180) % 360,
//					 ref_state->getDuration(),
//					 new_left_speed,
//					 new_right_speed);
//};

} // namespace SARC

