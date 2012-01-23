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

/************ ETHERNET SHIELD CONFIG ************/
byte mac[] = { 0x90, 0xA2, 0xDA, 0x00, 0x3B, 0xB2 }; // MAC address
byte ip[] = { 192, 168, 1, 99 }; // IP address
byte gateway[] = { 192, 168, 1, 1 }; // Gateway address
byte subnet[] = { 255, 255, 255, 0 }; // Subnet mask
unsigned int port = 23; // Port number (Telnet)
EthernetServer *server;

/************ History ************/
SARC::StateHistory *stateHistory = NULL;
unsigned long lastMoveTime;

/************ Motors ************/
SARC::Motor* motor = NULL;

/************ Display ************/
Display *display = NULL;

void setup()
{
	display = new Display();	// See Display.h for important settings!

	// init ethernet shield
	display->PrintLine("Init ethernet.");
	Ethernet.begin(mac, ip, gateway, subnet);
	*server = EthernetServer(port);
	server->begin();

	// Initialize the history
	stateHistory = new SARC::StateHistory((unsigned int)MAX_HISTORY);

#ifdef USE_SERVOS
	motor = new SARC::Motor(PIN_LEFT_SERVO, PIN_RIGHT_SERVO);
#endif

#ifdef USE_DC_MOTORS
	motor = new SARC::Motor(AF_MOTOR_LEFT, AF_MOTOR_RIGHT);
#endif
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
						motor->StopMovement();
						break;

					case CFORWARD:
						client.println("Accelerating Forward.");
						motor->MoveForward();
						break;

					case CREVERSE:
						client.println("Accelerating backward.");
						motor->MoveReverse();
						break;

					case CLEFT:
						client.println("Turning left.");
						motor->TurnLeft();
						break;

					case CRIGHT:
						client.println("Turning right.");
						motor->TurnRight();
						break;

					case CFORWARD_FULL:
						client.println("Full speed ahead!");
						motor->MoveForwardFullSpeed();
						break;

					case CREVERSE_FULL:
						client.println("Full speed reverse!");
						motor->MoveReverseFullSpeed();
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
				if (motor->IsMoving()) {
					if (millis() - lastMoveTime >= MOVEMENT_TIMEOUT) {
						display->PrintLine("Movement timeout.");
						motor->StopMovement();
					}
				}
			}
		}

		display->PrintLine("Connection terminated.");
		motor->StopMovement();
	}
}

