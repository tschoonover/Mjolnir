/*
 * State.h
 *
 *  Created on: Jan 14, 2012
 *      Author: Leland
 */

#ifndef STATE_H_
#define STATE_H_

#include <vector>
#include "MotorDefs.h"
#ifndef MAX_HISTORY
#define MAX_HISTORY 500
#endif

#ifndef STATE_C_

using namespace MotorDefs;

namespace SARC {


class State {

public:
	State(int newDirection, unsigned long newDuration, int newLeftSpeed, int newRightSpeed);

	int getDirection(void);
	bool isLeftForward(void);
	int getLeftSpeed(void);
	bool isRightForward(void);
	int getRightSpeed(void);
	void setDuration(unsigned long duration);
	int getDuration(void);

	// Returns the difference between left and right speeds as a signed long.
	// We don't care what this value is, we only want the magnitude. This
	// is used by the StateHistory class (below).
	long getYaw(void);

	// For equality, we only compare the direction and speed(s) - not duration.
	bool operator==(State* state);

private:
	std::vector<State> _vector;
	unsigned long _previousTick;

//private:
//	int direction;				// Assumed to be 0 - 360 (degrees)
//	int leftSpeed;
//	int rightSpeed;
//	unsigned long duration;		// Probably length in ticks, but can be anything.
};


///////////////////////////////////////////////////////////////////////////////

typedef std::vector<State>::reverse_iterator state_reverse_iterator;

class StateHistory
{
 public:
	StateHistory(unsigned int);
	unsigned int setHistorySize(unsigned int);

 private:
	std::vector<State> _vector;

 public:
	// Adds a State.
	// @return: size_t The number of States in this history.
	int AddState(const State& state);
	state_reverse_iterator BacktrackIterator (unsigned int lastState=0);
};

///////////////////////////////////////////////////////////////////////////////

class StateFactory
{
 public:
	State* CopyState(State& stateSource);

	/*
	 * Allocates and returns a new State that has the opposite direction of the ref_state.
	 *
	 * This assumes:
	 * 		neutral - (forward - neutral) = reverse
	 * 		neutral + (neutral - reverse) = forward
	 *
	 * @See: MotorDefs.h
	 */
	State* OppositeDirection (State& ref_state);
};

} // namespace SARC

#endif // STATE_C_

#endif // STATE_H_
