//============================================================================
// Name        : UnitTests.cpp
// Author      : Leland Green...
// Version     :
// Copyright   : Copyright (c) 2012 Leland Green... and Section9
// Description : Unit tests for Mjolnir and related code.
//============================================================================

//#include <iostream>
//using namespace std;
#include <stdio.h>

#include "MotorDefs.h"
#include "State.h"

int test_State(void)
{
	int error = 0;
	State *state1 = new State(0, 0, MotorDefs::forward, MotorDefs::forward);
	StateFactory *stateFactory = new StateFactory();
	State *state2 = stateFactory->CopyState(*state1);
	if (state2->getLeftSpeed() != MotorDefs::forward || state2->getRightSpeed() != MotorDefs::forward)
	{
		//std::cout << "Error on assignment!";
		printf("Error on assignment!");
		error = 1;
	}
	delete state2;

	State *state3 = stateFactory->OppositeDirection(*state1);
	if (state3->getLeftSpeed() != MotorDefs::reverse || state3->getRightSpeed() != MotorDefs::reverse)
	{
		//std::cout << "Error: OppositeDirection() did not reverse speed(s)!" << std::endl;
		printf("Error: OppositeDirection() did not reverse speed(s)!");
		//std::cout << "state3->getLeftSpeed() = " << state3->getLeftSpeed() << ", state3->getRightSpeed() = " << state3->getRightSpeed() << std::endl;
		printf("state3->getLeftSpeed() = %d, state3->getRightSpeed() = %d", state3->getLeftSpeed(), state3->getRightSpeed());
		error = 1;
	}
	delete state3;
	return error;
}

int main()
{
	//std::cout << "Testing... please do not turn off your computer or blow it up...." << std::endl;
	printf("Testing... please do not turn off your computer or blow it up....");
	if (test_State() != 0)
		//std::cout << "Errors!";
		printf("Errors!");
	else
		//std::cout << "Success!";
		printf("Success!");
	return 0;
}
