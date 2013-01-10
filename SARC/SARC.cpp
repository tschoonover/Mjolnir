/* Prototype code for Simple Arduino Robotic Control.
 * Control is via Ethernet and simple character-based commands.
 * This is specialized for a tank (tracked) drive with 2 motors.
 * Copyright (c) 2011-2012 Leland Green... and Section9
 *
 * Adapted from Arduino Ethernet library example sketches
 * for Section9 (http://section9.choamco.com/)
 * By: Leland Green...     Email: aboogieman (_at_) gmail.com
 *     Odysseus            Email: odysseus.section9@gmail.com
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
//#include "State.h"
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

/************ ROBOT MOVEMENT DEFINITIONS ************/
// If motors are moving and this many milliseconds pass, stop motors.
#define MOVEMENT_TIMEOUT 5000		// 5000 = 5 seconds

// If client has been connected for this long (milliseconds), start backtracking to signal.
//#define TIME_UNTIL_BACKTRACK 3000	// 30000 = 30 seconds

// TODO: Refactor to get rid of all global variables (or at least global class pointers).
/************ History ************/
//SARC::StateHistory *stateHistory = NULL; // This is populated in Motor.cpp
unsigned long lastMoveTime;
//SARC::state_reverse_iterator backtrackIterator;
//bool haveBacktrackIterator;
//bool inAutoMove;				// We're replaying history/moving automatically!
//unsigned long autoMoveExpires;	// The milliseconds that the current movement should be stopped.
//unsigned long previousTicks;	// The milliseconds for the previous call of loop().
bool displayedWaitingMessage = false;
//unsigned long ticksLastConnected = 0L;

/************ Motors ************/
SARC::Motor* motor = NULL;

/************ Connection ************/
SARC::Connection* connection = NULL;

/************ Display ************/
#ifdef USE_LCD
	Display *display = NULL;
#endif

/************ Misc. global variables ************/
unsigned int delta = DELTA;

void setup()
{
	#ifdef DEBUG
		// Initialize serial communication for debug output.
		Serial.begin(9600);
		Serial.println("Entering setup().");
	#endif

	#ifdef USE_LCD
		// Initialize LCD.
		display = new Display();
		delay(1000); // Pause to allow device to initialize.
		display->Clear();
		display->Home();
		#ifdef DEBUG
			Serial.println("LCD initialized.");
		#endif
	#endif

	// Initialize connection.
	connection = new SARC::Connection();
	delay(1000); // Pause to allow device to initialize.
	#ifdef DEBUG
		Serial.println("Communication initialized.");
	#endif
	#ifdef USE_LCD
		display->PrintLine("Comm init'd.");
	#endif

	// Initialize state history.
//	stateHistory = new SARC::StateHistory((unsigned int)MAX_HISTORY);
//	#ifdef DEBUG
//		Serial.println("History initialized.");
//	#endif
//	#ifdef USE_LCD
//		display->PrintLine("History init'd.");
//	#endif

	#ifdef USE_SERVOS
		// Initialize VEX motors.
		motor = new SARC::Motor(PIN_LEFT_SERVO, PIN_RIGHT_SERVO);
		#ifdef DEBUG
			Serial.println("Servos initialized.");
		#endif
		#ifdef USE_LCD
			display->PrintLine("Servos init'd.");
		#endif
	#endif

	#ifdef USE_DC_MOTORS
	#ifdef USE_AF_MOTORS
		// Initialize adafruit motors.
		motor = new SARC::Motor(AF_MOTOR_LEFT, AF_MOTOR_RIGHT);
	#endif
	#endif

	// Initialize backtrack control variables.
//	haveBacktrackIterator = false;
//	autoMoveExpires = 0;
//	inAutoMove = false;

#ifdef DEBUG
	Serial.println("Entering loop().");
#endif
}

void loop()
{
	unsigned long millisNow = millis();	// This will be close enough for our purposes.

	if (!displayedWaitingMessage)
	{
		#ifdef DEBUG
			Serial.println("Waiting for client.");
		#endif
		#ifdef USE_LCD
				display->PrintLine("Waiting for client.");
		#endif
		displayedWaitingMessage = true;
	}

	// if client found, begin parsing robot control commands
	if (connection->ClientIsConnected())
	{
		#ifdef DEBUG
			Serial.println("Client acquired.");
		#endif
		#ifdef USE_LCD
			display->PrintLine("Client acquired.");
		#endif

		// process user input as long as connection persists
		while (connection->ClientIsConnected())
		{
			millisNow = millis();
//			ticksLastConnected = millisNow;

			// check for user input
			if (connection->ClientDataAvailable())
			{
				// read the next character from the input buffer
				char c = connection->Read();

				#ifdef DEBUG
					Serial.print("Received command: ");
					Serial.println(c);
				#endif

//				if (haveBacktrackIterator)
//				{
//					stateHistory->SetCurrent(backtrackIterator);
//				}

				// Process command
				switch (c)
				{
					case CMAINTAIN:
						lastMoveTime = millis();
						connection->PrintLine("Maintaining current speed.");
						break;

					case CSTOP:
						connection->PrintLine("Full Stop.");
						motor->StopMovement();
						#ifdef USE_LCD
							display->PrintLine("Full Stop.");
						#endif
						break;

					case CFORWARD:
						connection->PrintLine("Accelerating.");
						motor->AccelerateForward(delta);
						#ifdef USE_LCD
							display->PrintLine("Accelerating.");
						#endif
						break;

					case CREVERSE:
						connection->PrintLine("Decelerating.");
						motor->AccelerateReverse(delta);
						#ifdef USE_LCD
							display->PrintLine("Decelerating.");
						#endif
						break;

					case CLEFT:
						connection->PrintLine("Turning left.");
						motor->TurnLeft(delta);
						#ifdef USE_LCD
							display->PrintLine("Turning left.");
						#endif
						break;

					case CRIGHT:
						connection->PrintLine("Turning right.");
						motor->TurnRight(delta);
						#ifdef USE_LCD
							display->PrintLine("Turning right.");
						#endif
						break;

					case CFORWARD_FULL:
						connection->PrintLine("Full forward.");
						motor->MoveForwardFullSpeed();
						#ifdef USE_LCD
							display->PrintLine("Full forward.");
						#endif
						break;

					case CREVERSE_FULL:
						connection->PrintLine("Full reverse.");
						motor->MoveReverseFullSpeed();
						#ifdef USE_LCD
							display->PrintLine("Full reverse.");
						#endif
						break;

					case CLEFTFULL:
						connection->PrintLine("Full left.");
						motor->TurnLeftFullSpeed();
						#ifdef USE_LCD
							display->PrintLine("Full left.");
						#endif
						break;

					case CRIGHTFULL:
						connection->PrintLine("Full right.");
						motor->TurnRightFullSpeed();
						#ifdef USE_LCD
							display->PrintLine("Full right.");
						#endif
						break;

					case CBRAKE:
						connection->PrintLine("Braking.");
						motor->Brake();
						#ifdef USE_LCD
							display->PrintLine("Braking.");
						#endif
						break;

					case CSTEER_CENTER:
						connection->PrintLine("Centering.");
						motor->SteerCenter();
						#ifdef USE_LCD
							display->PrintLine("Centering.");
						#endif
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
		}

		#ifdef DEBUG
			Serial.println("Connection terminated.");
		#endif
		#ifdef USE_LCD
			display->PrintLine("Conn terminated.");
		#endif
		motor->StopMovement();
	}

	/******* If we're here, we're not connected *******/
//	if (SARC::timeDifference(ticksLastConnected, millisNow) >= TIME_UNTIL_BACKTRACK)
//	{
//		if (inAutoMove == true)
//		{
//			Serial.print("automove time="); Serial.println(SARC::timeDifference(previousTicks, millisNow));
//			if (SARC::timeDifference(previousTicks, millisNow) >= autoMoveExpires)
//			{
//				inAutoMove = false;
//				#ifdef DEBUG
//					Serial.println("AutoMove expired");
//				#endif
//			}
//		}
//		if (inAutoMove == false)
//		{
//			if (stateHistory->GetHistorySize() > 0)
//			{
//				/******* Return to sender :) *******/
//				if (haveBacktrackIterator == false)
//				{
//					#ifdef DEBUG
//						Serial.println("Initiating return to backtrack!");
//					#endif
//					backtrackIterator = stateHistory->BacktrackIterator(stateHistory->GetHistorySize());
//					haveBacktrackIterator = true;
//				}
//
//				backtrackIterator->CopyReverse(*backtrackIterator);
//
//				#ifdef DEBUG
//					Serial.println("Calling SetSpeeds() with speeds from history.");
//				#endif
//				motor->SetSpeeds(backtrackIterator->getLeftSpeed(), backtrackIterator->getRightSpeed());
//
//				if (backtrackIterator != stateHistory->BacktrackIteratorEnd())
//				{
//					++backtrackIterator; // It's a reverse iterator, so incrementing goes to previous one. ;)
//					autoMoveExpires = backtrackIterator->getDuration();
//					inAutoMove = true;
//					Serial.print("beginning automove for duration="); Serial.println(autoMoveExpires);
//				}
//				else
//				{
//					#ifdef DEBUG
//						Serial.println("backtrackIterator depleted.");
//					#endif
//					motor->StopMovement();
//				}
//			} // if (stateHistory->GetHistorySize() > 0)
//			else
//			{
//				motor->StopMovement();
//			}
//		} // if (inAutoMove == false)
//	} 	// if (timeDifference(ticksLastConnected, millis()) >= TIME_UNTIL_BACKTRACK)
//	previousTicks = millisNow;
}
