/* Prototype code for Simple Arduino Robotic Control.
 * Control is via Ethernet and simple character-based commands.
 * This is specialized for a tank (tracked) drive with 2 motors.
 * Copyright (c) 2011-2012 Leland Green... and Section9
 *
 * Adapted from Arduino Ethernet library example sketches
 * for Section9 (http://section9.choamco.com/)
 * By: Leland Green...     Email: aboogieman (_at_) gmail.com
 *     Odysseus            Email: odysseus@choamco.com
 * (At least it was originally adapted from that. Those are only a few
 * lines of what is becoming a complex system.)
 *
 * This file is free software; you can redistribute it and/or modify
 * it under the terms of either the GNU General Public License version 2
 * or the GNU Lesser General Public License version 2.1, both as
 * published by the Free Software Foundation.
 *
 * To use, telnet to the Arduino and send commands. Commands are the
 * same as used for many RPG's, with a few additions. They *are* case
 * sensitive:
 *
 * w = Accelerate forward
 * W = Full speed forward
 * s = Accelerate backward
 * S = Full speed backward
 * a = Steer left
 * d = Steer Right
 * q = Stop (immediate)
 * m = Maintain current speed
 *
 * Note that the PULSE definitions are for Vex Robotics systems.
 * Also note that if this is on a robot, you need either WiFi or
 * a wireless router onboard so you can telnet to it. :)
 *
 *** Please see ReadMe.txt for further information on using and building. ***
 *
 * We (Section9) have built a working prototype. Initial version
 * includes a wireless router. The project is called Mjolnir.
 * See the Section9 URL above for blog updates and other plans.
 */

// Do not remove the include below
#include "SARC.h"
#include "State.h"
#include "ArduinoUtils.h"
#include "MotorDefs.h"
#include "Motor.h"
#include "Display.h"
#include "connection.h"
#include <Arduino.h>

//#define DEBUG

/************ ROBOT COMMAND DEFINITIONS ************/
#define CMAINTAIN		'm'
#define CBRAKE			'b'
#define CSTOP           'q'
#define CFORWARD        'w'
#define CREVERSE        's'
#define CLEFT           'a'
#define CRIGHT          'd'
#define CSTEER_CENTER	'c'
#define CFORWARD_FULL   'W'
#define CREVERSE_FULL   'S'
#define CLEFTFULL       'A'
#define CRIGHTFULL      'D'
#define HELP			'h'

/************ ROBOT MOVEMENT DEFINITIONS ************/
// If motors are moving and this many milliseconds pass, stop motors.
#define MOVEMENT_TIMEOUT 5000		// 5000 = 5 seconds

// If client has been connected for this long (milliseconds), start backtracking to signal.
#define TIME_UNTIL_BACKTRACK 30000	// 30000 = 30 seconds

// TODO: Refactor to get rid of all global variables (or at least global class pointers).
/************ History ************/
SARC::StateHistory *stateHistory = NULL; // This is populated in Motor.cpp
unsigned long lastMoveTime;
SARC::state_reverse_iterator backtrackIterator;
bool haveBacktrackIterator;
bool inAutoMove;				// We're replaying history/moving automatically!
unsigned long autoMoveExpires;	// The milliseconds that the current movement should be stopped.
unsigned long previousTicks;	// The milliseconds for the previous call of loop().

/************ Motors ************/
SARC::Motor* motor = NULL;

/************ Connection ************/
SARC::Connection* connection = NULL;

/************ Display ************/
#ifdef USE_LCD
	Display *display = NULL;
#endif

/************ Misc. global variables ************/
char tempBuf[20];
char *pch;
unsigned int delta = DELTA;

void setup()
{
	#ifdef USE_LCD
		display = new Display();	// See Display.h for important settings!

		display->PrintLine("Init Connection");
	#endif

	connection = new SARC::Connection();
	connection->PrintLine((const char *)"Initializing...");

	// Initialize the history
	stateHistory = new SARC::StateHistory((unsigned int)10);

	#ifdef USE_SERVOS
		motor = new SARC::Motor(PIN_LEFT_SERVO, PIN_RIGHT_SERVO);
	#endif

	#ifdef USE_DC_MOTORS
	#ifdef USE_AF_MOTORS
		motor = new SARC::Motor(AF_MOTOR_LEFT, AF_MOTOR_RIGHT);
	#endif
	#endif

	haveBacktrackIterator = false;

	autoMoveExpires = 0;
	inAutoMove = false;

//	motor->MoveForwardFullSpeed();
//	delay(1000);
//	motor->MoveReverseFullSpeed();

	delay(1000); 			// Give Serial a chance to init.
#ifdef DEBUG
	Serial.println("setup()");
#endif
	motor->StopMovement(); 	// Just in case the Arduino was reset.
}

void showUsage(SARC::Connection* connection)
{
	connection->PrintLine(VERSION);
	// echo valid commands to user
	connection->PrintLine("Movement commands:");
	connection->PrintLine("------------------");
	connection->PrintLine("Maintain - m");
	connection->PrintLine("Brake - b");
	connection->PrintLine("Stop (neutral) - q");
	connection->PrintLine("Forward - w");
	connection->PrintLine("Reverse - s");
	connection->PrintLine("Left - a");
	connection->PrintLine("Right - d");
	connection->PrintLine("Full Forward - W");
	connection->PrintLine("Full Reverse - S");
	connection->PrintLine("Full Left - A");
	connection->PrintLine("Full Right - D");
	connection->PrintLine("Steer Center - c");
}

void loop()
{
	unsigned long ticksLastConnected = 0L;
	unsigned long millisNow = millis();	// This will be close enough for our purposes.

#ifdef USE_LCD
	// output debug info to LCD screen
	display->PrintLine("Waiting for client");
#endif

	// if client found, begin parsing robot control commands
	if (connection->ClientIsConnected())
	{
		// output debug info
		#ifdef USE_LCD
			display->PrintLine("Net Client acquired");
		#endif

		showUsage(connection);

		// process user input as long as connection persits
		while (connection->ClientIsConnected())
		{
			millisNow = ticksLastConnected = millis();

			// check for user input
			if (connection->ClientDataAvailable())
			{
				// read the next character from the input buffer
				char c = connection->Read();

				#ifdef USE_LCD
					display->Print("Received command: ");
					display->Print(c);
				#endif

				#ifdef DEBUG
					Serial.print("Received command: ");
					Serial.println(c);
				#endif

				if (haveBacktrackIterator)
				{
					stateHistory->SetCurrent(backtrackIterator);
				}

				// Process command
				switch (c)
				{
					case HELP:
						showUsage(connection);
						break;

					case CMAINTAIN:
						connection->PrintLine("Maintaining current speed.");
						break;

					case CSTOP:
						connection->PrintLine("Full Stop.");
						motor->StopMovement();
						break;

					case CFORWARD:
						connection->PrintLine("Accelerating Forward.");
						motor->AccelerateForward(delta);
						break;

					case CREVERSE:
						connection->PrintLine("Accelerating backward.");
						motor->AccelerateReverse(delta);
						break;

					case CLEFT:
						connection->PrintLine("Turning left.");
						motor->TurnLeft(delta);
						break;

					case CRIGHT:
						connection->PrintLine("Turning right.");
						motor->TurnRight(delta);
						break;

					case CFORWARD_FULL:
						connection->PrintLine("Full speed ahead!");
						motor->MoveForwardFullSpeed();
						break;

					case CREVERSE_FULL:
						connection->PrintLine("Full speed reverse!");
						motor->MoveReverseFullSpeed();
						break;

					case CLEFTFULL:
						connection->PrintLine("Turning left.");
						motor->TurnLeftFullSpeed();
						break;

					case CRIGHTFULL:
						connection->PrintLine("Turning right.");
						motor->TurnRightFullSpeed();
						break;

					case CBRAKE:
						connection->PrintLine("Braking.");
						motor->Brake();
						break;

					case CSTEER_CENTER:
						connection->PrintLine("Steer Center");
						motor->SteerCenter();
						break;

					default:
						connection->PrintLine("Unrecognized command: ");
						connection->PrintLine((const char*)&c);
						break;
				}
			}
		 else // no input from user to process
			{
				// stop movement if movement time limit exceeded
				if (motor->IsMoving()) {
					if (millisNow - lastMoveTime >= MOVEMENT_TIMEOUT) {
						#ifdef USE_LCD
							display->PrintLine("Movement timeout.");
						#endif
						#ifdef DEBUG
							Serial.print("millisNow = "); Serial.print(millisNow);
							Serial.print(", lastMoveTime = "); Serial.print(lastMoveTime);
							Serial.print(", MOVEMENT_TIMEOUT = "); Serial.print(MOVEMENT_TIMEOUT);
							Serial.println(" Movement timeout. Stopping.");
						#endif
						motor->StopMovement();
					}
				}
			}
		} // while (connection->ClientIsConnected())

		#ifdef USE_LCD
			display->PrintLine("Connection terminated.");
		#endif
		#ifdef DEBUG
			Serial.println("Connection terminated.");
		#endif
		motor->StopMovement();

	} // if (connection->ClientIsConnected())

	/******* If we're here, we're not connected *******/

	if (SARC::timeDifference(ticksLastConnected, millisNow) >= TIME_UNTIL_BACKTRACK)
	{
		#ifdef DEBUG
			Serial.println("Time until backtrack expired...");
		#endif
		if (inAutoMove == true)
		{
			if (SARC::timeDifference(previousTicks, millisNow) >= autoMoveExpires)
			{
				inAutoMove = false;
				#ifdef DEBUG
					Serial.println("AutoMove expired");
				#endif
			}
		}
		if (inAutoMove == false)
		{
			if (stateHistory->GetHistorySize() > 0)
			{
				/******* Return to sender :) *******/
				if (haveBacktrackIterator == false)
				{
					#ifdef DEBUG
						Serial.println("Initiating return to backtrack!");
					#endif
					backtrackIterator = stateHistory->BacktrackIterator(stateHistory->GetHistorySize());
					haveBacktrackIterator = true;
				}

				backtrackIterator->CopyReverse(*backtrackIterator);

				#ifdef DEBUG
					Serial.println("Calling SetSpeeds() with speeds from history.");
				#endif
				motor->SetSpeeds(backtrackIterator->getLeftSpeed(), backtrackIterator->getRightSpeed());

				if (backtrackIterator != stateHistory->BacktrackIteratorEnd())
				{
					++backtrackIterator; // It's a reverse iterator, so incrementing goes to previous one. ;)
					autoMoveExpires = backtrackIterator->getDuration();
					inAutoMove = true;
				}
				else
				{
					#ifdef DEBUG
						Serial.println("backtrackIterator depleted.");
					#endif
					motor->StopMovement();
				}
			} // if (stateHistory->GetHistorySize() > 0)
			else
			{
				motor->StopMovement();
			}
		} // if (inAutoMove == false)
	} 	// if (timeDifference(ticksLastConnected, millis()) >= TIME_UNTIL_BACKTRACK)
	previousTicks = millisNow;
}	// loop()

