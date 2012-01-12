/* State and related classes originally written for Mjolnir project
 * by Leland Green in January 2012.
 *
 * The overall goal of this portion is to encapsulate state for a
 * moment in time. Mjolnir uses a track-based (tank) robot. At first
 * glance, this may seem simpler than a wheel-based device, but in
 * some ways, it's more complicated. For example, to turn, the left
 * and right tracks go in different directions. This would also
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

//#include <iterator>
//#include <vector>
//#include <map>
//#include <pnew.cpp>

#include "MotorDefs.h"

using namespace MotorDefs;

/*
 * State class is a immutable encapsulation of the current direction and speed
 * for a simple agent. Since it is immutable, a class factory is provided
 * (below) that copies a state and can also return an "opposite direction"
 * state, which is useful for backtracking.
 */
class State {

public:
	State(int newDirection = 0, unsigned long newDuration = 0, int newLeftSpeed =
			neutral , int newRightSpeed = neutral) {
		this->direction = newDirection;
		this->leftSpeed = newLeftSpeed;
		this->rightSpeed = newRightSpeed;
		this->duration = newDuration;
	}

	int getDirection(void) {
		return this->direction;
	}

	bool isLeftForward(void) {return this->leftSpeed > neutral;}

	int getLeftSpeed(void)
	{
		return this->leftSpeed;
	}

	bool isRightForward(void) {return this->rightSpeed > neutral;}

	int getRightSpeed(void) {
		return this->rightSpeed;
	}

	void setDuration(unsigned long duration)
	{
		this->duration = duration;
	}

	int getDuration(void)
	{
		return this->duration;
	}

	// For equality, we only compare the direction and speed(s) - not duration.
	bool operator==(State* state) {
		return state->direction == this->direction
				&& state->leftSpeed == this->leftSpeed
				&& state->rightSpeed == this->rightSpeed;
	}

private:
	int direction;				// Assumed to be 0 - 360 (degrees)
	int leftSpeed;
	int rightSpeed;
	unsigned long duration;		// Probably length in ticks, but can be anything.
};



class StateFactory
{
 public:
	State* CopyState(State& stateSource)
	{
		return new State(stateSource.getDirection(), stateSource.getDuration(),
						 stateSource.getLeftSpeed(), stateSource.getRightSpeed());
	};
	/*
	 * Allocates and returns a new State that has the opposite direction of the ref_state.
	 *
	 * This assumes:
	 * 		neutral - (forward - neutral) = reverse
	 * 		neutral + (neutral - reverse) = forward
	 *
	 * @See: MotorDefs.h
	 */
	State* OppositeDirection (State& ref_state)
	{
		int new_left_speed, new_right_speed;
		new_left_speed =  (ref_state.isLeftForward())?
				neutral - (ref_state.getLeftSpeed() - neutral) :
				neutral + (neutral - ref_state.getLeftSpeed());

		new_right_speed =  (ref_state.isRightForward())?
				neutral - (ref_state.getRightSpeed() - neutral) :
				neutral + (neutral - ref_state.getRightSpeed());

		return new State((ref_state.getDirection() + 180) % 360,
						 ref_state.getDuration(),
						 new_left_speed,
						 new_right_speed);
	};
};
