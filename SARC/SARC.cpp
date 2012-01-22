/* Prototype code for Simple Arduino Robotic Control.
 * Control is via Ethernet and simple character-based commands.
 * This is specialized for a tank (tracked) drive with 2 motors.
 * Copyright (c) 2011-2012 Leland Green... and Section9
 *
 * Adapted from Arduino Ethernet library example sketches
 * for Section9 (http://section9.choamco.com/)
 * By: Leland Green...     Email: aboogieman (_at_) gmail.com
 *     Odysseus            Email: odysseus@choamco.com
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
 *
 * Note that the PULSE definitions are for Vex Robotics systems.
 * Also note that if this is on a robot, you need either WiFi or
 * a wireless router onboard so you can telnet to it. :)
 *
 * We (Section9) have built a working prototype. Initial version
 * includes a wireless router. The project is called Mjolnir.
 * See the Section9 URL above for blog updates and other plans.
 *
 */

//#include <iterator>
//#include <vector>
//#include <map>
//#include <pnew.h>
//
//#include <algorithm>
//
// Do not remove the include below
#include "SARC.h"
#include "State.h"
#include "ArduinoUtils.h"

#include "MotorDefs.h"
#include "Display.h"
#include <Arduino.h>

/************ ROBOT COMMAND DEFINITIONS ************/
#define CSTOP           'q'
#define CFORWARD        'w'
#define CREVERSE        's'
#define CLEFT           'a'
#define CRIGHT          'd'
#define CFORWARD_FULL   'W'
#define CREVERSE_FULL   'S'

/************ ROBOT MOVEMENT DEFINITIONS ************/
#define MOVEMENT_TIMEOUT 1000

/************ PIN DEFINITIONS ************/
#define PIN_LEFT_SERVO  2
#define PIN_RIGHT_SERVO 3

/************ ETHERNET SHIELD CONFIG ************/
byte mac[] = { 0x90, 0xA2, 0xDA, 0x00, 0x3B, 0xB2 }; // MAC address
byte ip[] = { 192, 168, 1, 99 }; // IP address
byte gateway[] = { 192, 168, 1, 1 }; // Gateway address
byte subnet[] = { 255, 255, 255, 0 }; // Subnet mask
unsigned int port = 23; // Port number (Telnet)
EthernetServer *server;

/************ SERVO CONFIG ************/
Servo *leftTrackServo;
Servo *rightTrackServo;
int leftTrackSpeed = MotorDefs::neutral;
int rightTrackSpeed = MotorDefs::neutral;
unsigned long lastMoveTime;
boolean isMoving = false;

/************ History ************/
SARC::StateHistory *stateHistory = NULL;

Display *display = NULL;

void setup()
{
	display = new Display();	// See Display.h for important settings!

	*leftTrackServo = Servo();
	*rightTrackServo = Servo();
	// init servos
	display->PrintLine("Initializing servos.");
	leftTrackServo->attach((int) PIN_LEFT_SERVO);
	rightTrackServo->attach((int) PIN_RIGHT_SERVO);

	// init ethernet shield
	display->PrintLine("Init ethernet.");
	Ethernet.begin(mac, ip, gateway, subnet);
	*server = EthernetServer(port);
	server->begin();

	// Initialize the hsistory
	stateHistory = new SARC::StateHistory((unsigned int)MAX_HISTORY);
}

void updateServos()
{
	leftTrackServo->writeMicroseconds(leftTrackSpeed);
	rightTrackServo->writeMicroseconds(rightTrackSpeed);
	lastMoveTime = millis();

	if ((leftTrackSpeed == MotorDefs::neutral) && (rightTrackSpeed == MotorDefs::neutral))
		isMoving = false;
	else
		isMoving = true;

	// TODO: Update state with optional current heading (using compass or GPS module(s)).
	SARC::State* currentState = new SARC::State((int)0, (unsigned long)0, (int)leftTrackSpeed, (int)rightTrackSpeed);
	stateHistory->AddState((const SARC::State&)*currentState);

	display->Print("Left Track = ");
	display->Print(leftTrackSpeed);
	display->Print("Right Track = ");
	display->Print(rightTrackSpeed);
	display->Print("isMoving = ");
	display->Print(isMoving, BIN);
};

void moveForward()
{
	leftTrackSpeed = min(MotorDefs::forward,
			leftTrackSpeed + MotorDefs::delta);
	rightTrackSpeed = min(MotorDefs::forward,
			rightTrackSpeed + MotorDefs::delta);
	updateServos();
}

void moveBackward()
{
	leftTrackSpeed = max(MotorDefs::reverse,
			leftTrackSpeed - MotorDefs::delta);
	rightTrackSpeed = max(MotorDefs::reverse,
			rightTrackSpeed - MotorDefs::delta);
	updateServos();
}

void turnLeft()
{
	leftTrackSpeed = max(MotorDefs::reverse,
			leftTrackSpeed - MotorDefs::delta);
	rightTrackSpeed = min(MotorDefs::forward,
			rightTrackSpeed + MotorDefs::delta);
	updateServos();
}

void turnRight()
{
	leftTrackSpeed = min(MotorDefs::forward,
			leftTrackSpeed + MotorDefs::delta);
	rightTrackSpeed = max(MotorDefs::reverse,
			rightTrackSpeed - MotorDefs::delta);
	updateServos();
}

void stopMovement()
{
	leftTrackSpeed = MotorDefs::neutral;
	rightTrackSpeed = MotorDefs::neutral;
	updateServos();
}

void loop()
{
	// output debug info
	display->PrintLine("Waiting for client");

	// check for an incoming client
	EthernetClient client = server->available();

	// if client found, begin parsing robot control commands
	if (client) {

		// output debug info
		display->PrintLine("Net Client acquired");

		// echo valid commands to user
		client.println("Movement commands:");
		client.println("------------------");
		client.println("Stop - q");
		client.println("Forward - w");
		client.println("Reverse - s");
		client.println("Left - a");
		client.println("Right - d");
		client.println("Full MotorDefs::forward - W");
		client.println("Full Reverse - S");

		// process user input as long as connection persits
		while (client.connected())
		{
			// check for user input
			if (client.available())
			{
				// read the next character from the input buffer
				char c = client.read();

				// output debug info
				display->Print("Received command: ");
				display->Print(c);

				// process command
				switch (c)
				{
					case CSTOP:
						client.println("Full Stop");
						stopMovement();
						break;

					case CFORWARD:
						client.println("Accelerating Forward.");
						moveForward();
						break;

					case CREVERSE:
						client.println("Accelerating backward.");
						moveBackward();
						break;

					case CLEFT:
						client.println("Turning left.");
						turnLeft();
						break;

					case CRIGHT:
						client.println("Turning right.");
						turnRight();
						break;

					case CFORWARD_FULL:
						client.println("Full speed ahead!");
						leftTrackSpeed = MotorDefs::forward;
						rightTrackSpeed = MotorDefs::forward;
						updateServos();
						break;

					case CREVERSE_FULL:
						client.println("Full speed reverse!");
						leftTrackSpeed = MotorDefs::reverse;
						rightTrackSpeed = MotorDefs::reverse;
						updateServos();
						break;

					default:
						client.print("Unrecognized command: ");
						client.println(c);
						break;
				}
			}
		 else // no input from user to process
			{
				// stop movement if movement time limit exceeded
				if (isMoving) {
					if (millis() - lastMoveTime >= MOVEMENT_TIMEOUT) {
						display->PrintLine("Movement timeout.");
						stopMovement();
					}
				}
			}
		}

		display->PrintLine("Connection terminated.");
		stopMovement();
	}
}

