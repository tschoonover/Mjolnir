/* Prototype code for Simple Arduino Robotic Control. 
 * Control is via Ethernet and simple character-based commands.
 * This is specialized for a tank (tracked) drive with 2 motors.
 * Copyright (c) 2011 Leland Green... and Section9
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
 * We (Section9) have great plans for an actual prototype. Initial
 * plans are to use a wireless router. The project is called Mjolnir.
 * See the Section9 URL above for blog updates and other plans.
 * 
 */
 
#include <SPI.h>
#include "Ethernet.h"
#include <Servo.h>

/************ VEX MOTOR CONTROL DEFINITIONS ************/
#define VEX_BRAKE        200
#define VEX_FULL_FORWARD 2000
#define VEX_NEUTRAL      1500
#define VEX_FULL_REVERSE 1000
#define VEX_SPEED_DELTA  10
#define MOVEMENT_TIMEOUT 1000

/************ ROBOT COMMAND DEFINITIONS ************/
#define CSTOP           'q'
#define CFORWARD        'w'
#define CREVERSE        's'
#define CLEFT           'a'
#define CRIGHT          'd'
#define CFORWARD_FULL   'W'
#define CREVERSE_FULL   'S'

/************ PIN DEFINITIONS ************/
#define PIN_LEFT_SERVO  2
#define PIN_RIGHT_SERVO 3

/************ ETHERNET SHIELD CONFIG ************/
byte mac[] = { 0x90, 0xA2, 0xDA, 0x00, 0x3B, 0xB2 }; // MAC address
byte ip[] = { 192, 168, 1, 99 };                     // IP address
byte gateway[] = { 192, 168, 1, 1 };                 // Gateway address
byte subnet[] = { 255, 255, 255, 0 };                // Subnet mask
unsigned int port = 23;                              // Port number (Telnet)
Server server = Server(port);

/************ SERVO CONFIG ************/
Servo leftTrackServo;
Servo rightTrackServo;
int leftTrackSpeed = VEX_NEUTRAL;
int rightTrackSpeed = VEX_NEUTRAL;
unsigned long lastMoveTime;
boolean isMoving = false;

void setup()
{
  // init serial communication for debugging
  Serial.begin(9600);
  
  // init servos
  Serial.println("Initializing servos.");
  leftTrackServo.attach(PIN_LEFT_SERVO);
  rightTrackServo.attach(PIN_RIGHT_SERVO);

  // init ethernet shield
  Serial.println("Initializing ethernet.");
  Ethernet.begin(mac, ip, gateway, subnet);
  server = Server(port);
  server.begin();
}

void loop()
{
  // output debug info
  Serial.println("Waiting for client...");
  
  // check for an incoming client
  Client client = server.available();

  // if client found, begin parsing robot control commands
  if (client) {
    
    // output debug info
    Serial.println("Client acquired...");
    
    // echo valid commands to user
    client.println("Movement commands:");
    client.println("------------------");
    client.println("Stop - q");
    client.println("Forward - w");
    client.println("Reverse - s");
    client.println("Left - a");
    client.println("Right - d");
    client.println("Full forward - W");
    client.println("Full Reverse - S");

    // process user input as long as connection persits
    while (client.connected()) {
      
      // check for user input
      if (client.available()) {
        
        // read the next character from the input buffer
        char c = client.read();
        
        // output debug info
        Serial.print("Received command: ");
        Serial.println(c);
        
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
            leftTrackSpeed = VEX_FULL_FORWARD;
            rightTrackSpeed = VEX_FULL_FORWARD;
            updateServos();
            break;
            
          case CREVERSE_FULL:
            client.println("Full speed reverse!");
            leftTrackSpeed = VEX_FULL_REVERSE;
            rightTrackSpeed = VEX_FULL_REVERSE;
            updateServos();
            break;
            
          default:
            client.print("Unrecognized command: ");
            client.println(c);
        }
      } else  // no input from user to process
      {
        // stop movement if movement time limit exceeded
        if (isMoving) {
          if (millis() - lastMoveTime >= MOVEMENT_TIMEOUT) {
            Serial.println("Movement timeout.");
            stopMovement();
          }
        }
      }
    }
    
    Serial.println("Connection terminated.");
    stopMovement();
  }
}

void updateServos()
{ 
  leftTrackServo.writeMicroseconds(leftTrackSpeed);
  rightTrackServo.writeMicroseconds(rightTrackSpeed);
  lastMoveTime = millis();

  if ((leftTrackSpeed == VEX_NEUTRAL) && (rightTrackSpeed == VEX_NEUTRAL))
    isMoving = false;
  else
    isMoving = true;
  
  Serial.print("Left Track = ");
  Serial.println(leftTrackSpeed);
  Serial.print("Right Track = ");
  Serial.println(rightTrackSpeed);
  Serial.print("isMoving = ");
  Serial.println(isMoving, BIN);
}

void moveForward() 
{
  leftTrackSpeed = min(VEX_FULL_FORWARD, leftTrackSpeed + VEX_SPEED_DELTA);
  rightTrackSpeed = min(VEX_FULL_FORWARD, rightTrackSpeed + VEX_SPEED_DELTA);
  updateServos();
}

void moveBackward()
{
  leftTrackSpeed = max(VEX_FULL_REVERSE, leftTrackSpeed - VEX_SPEED_DELTA);
  rightTrackSpeed = max(VEX_FULL_REVERSE, rightTrackSpeed - VEX_SPEED_DELTA);
  updateServos();
}

void turnLeft() 
{
  leftTrackSpeed = max(VEX_FULL_REVERSE, leftTrackSpeed - VEX_SPEED_DELTA);
  rightTrackSpeed = min(VEX_FULL_FORWARD, rightTrackSpeed + VEX_SPEED_DELTA);
  updateServos();
}

void turnRight() 
{
  leftTrackSpeed = min(VEX_FULL_FORWARD, leftTrackSpeed + VEX_SPEED_DELTA);
  rightTrackSpeed = max(VEX_FULL_REVERSE, rightTrackSpeed - VEX_SPEED_DELTA);
  updateServos();
}

void stopMovement() 
{
  leftTrackSpeed = VEX_NEUTRAL;
  rightTrackSpeed = VEX_NEUTRAL;
  updateServos();
}
