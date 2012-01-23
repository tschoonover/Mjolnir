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

#include "State.h"
#include "MotorDefs.h"
#include "ArduinoUtils.h"
#include "Arduino.h"

#ifndef MAX_HISTORY
#define MAX_HISTORY 500
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
State::State(int new_direction = 0, unsigned long newDuration = 0, int new_leftSpeed =
		MotorDefs::neutral , int new_rightSpeed = MotorDefs::neutral)
{
	this->_direction = new_direction;
	this->_leftSpeed = new_leftSpeed;
	this->_rightSpeed = new_rightSpeed;
}

int State::getDirection(void)
{
	return this->_direction;
}

bool State::isLeftForward(void) {return this->_leftSpeed > MotorDefs::neutral;}

int State::getLeftSpeed(void) {return this->_leftSpeed;}

bool State::isRightForward(void) {return this->_rightSpeed > MotorDefs::neutral;}

int State::getRightSpeed(void) {return this->_rightSpeed;}

void State::setDuration(unsigned long duration) {this->_duration = duration;}

unsigned long State::getDuration(void) {return this->_duration;}

// For equality, we only compare the _direction and speed(s) - not duration.
bool State::operator==(const State& state)
{
	return state._direction == this->_direction
			&& state._leftSpeed == this->_leftSpeed
			&& state._rightSpeed == this->_rightSpeed;
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
	this->SetHistorySize(historySize);
};

unsigned int StateHistory::SetHistorySize(unsigned int historySize)
{
	this->_vector.resize(historySize);
	return this->_vector.size();
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
int StateHistory::AddState(State* state)
{
	unsigned long tickNow = micros();
	State *prevState = this->_vector[this->_vector.size() - 1];
	if (*prevState == *state)
	{
		prevState->setDuration( timeDifference(prevState->getDuration(), tickNow) );
	}
	else
	{
		if (this->_vector.size() >= MAX_HISTORY)
		{
			this->_vector.erase(this->_vector.begin());
		}
		prevState->setDuration(timeDifference(prevState->getDuration(), timeDifference(this->_previousTick, tickNow)));
		this->_vector.push_back(state);
	}
	this->_previousTick = tickNow;
	return this->_vector.size();
}

state_reverse_iterator StateHistory::BacktrackIterator (unsigned int lastState)
{
	return this->_vector.rbegin();
}


///////////////////////////////////////////////////////////////////////////////

State* StateFactory::CopyState(State& stateSource)
{
	return new State(stateSource.getDirection(), stateSource.getDuration(),
					 stateSource.getLeftSpeed(), stateSource.getRightSpeed());
};

/*
 * Allocates and returns a new State that has the opposite _direction of the ref_state.
 *
 * This assumes:
 * 		neutral - (forward - neutral) = reverse
 * 		neutral + (neutral - reverse) = forward
 *
 * @See: MotorDefs.h
 */
State* StateFactory::OppositeDirection (State& ref_state)
{
	int new_left_speed, new_right_speed;
	new_left_speed =  (ref_state.isLeftForward())?
			MotorDefs::neutral - (ref_state.getLeftSpeed() - MotorDefs::neutral) :
			MotorDefs::neutral + (MotorDefs::neutral - ref_state.getLeftSpeed());

	new_right_speed =  (ref_state.isRightForward())?
			MotorDefs::neutral - (ref_state.getRightSpeed() - MotorDefs::neutral) :
			MotorDefs::neutral + (MotorDefs::neutral - ref_state.getRightSpeed());

	return new State((ref_state.getDirection() + 180) % 360,
					 ref_state.getDuration(),
					 new_left_speed,
					 new_right_speed);
};

} // namespace SARC

