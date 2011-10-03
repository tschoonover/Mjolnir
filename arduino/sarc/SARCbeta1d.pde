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
 * To use, telnet to the Arduino and send commands.
 */
 
#include <SPI.h>
#include "Ethernet.h"
#include <Servo.h>

// The MAC address for the shield:
byte mac[] = { 0x90, 0xA2, 0xDA, 0x00, 0x3B, 0xB2 };  

// The IP address for the shield:
byte ip[] = { 192, 168, 1, 99 };

// The router's gateway address:
byte gateway[] = { 192, 168, 1, 1 };

// The default subnet:
byte subnet[] = { 255, 255, 255, 0 };

// Telnet defaults to port 23
Server server = Server(23);

// Global definitions for robot motion
#define STOP       0
#define FORWARD    1
#define REVERSE   2
#define LEFT       3
#define RIGHT      4

// All PULSE defines are in microseconds (us) for Servo.writeMicroseconds()
#define PULSE_BRAKE 200
#define PULSE_FULL_FORWARD 2000
#define PULSE_NEUTRAL 1500
#define PULSE_FULL_REVERSE 1000

// Movement character definitions
#define CSTOP       'q'
#define CFORWARD    'w'
#define CREVERSE   's'
#define CLEFT       'a'
#define CRIGHT      'd'
#define CDECELERATE    'b'
#define CFORWARD_FULL  'W'
#define CREVERSE_FULL 'S'

// Define the pins to use on motors.
// TODO: This will require more logic to actually control motors.
static int leftMotorPin  = 2;
static int rightMotorPin = 3;
static int leftFrontLED  = 4; // LED's to indicate motion
static int leftRearLED   = 5;
static int rightFrontLED = 6;
static int rightRearLED  = 7;

// Use two ints to track the speed of two tracks. 
// These are always in-sync except when turning.
unsigned int currentLeftSpeed = PULSE_NEUTRAL;
unsigned int currentRightSpeed = PULSE_NEUTRAL;
/* Experiment with this value. This will give 50 forward and 50 reverse "speeds". 
 * I.e., PULSE_FULL_FORWARD - PULSE_NEUTRAL = 500, and then 500 / 10 = 50.
 */
signed int speedDelta = 10; 

// Number of miliseconds to maintain current direction and speed. 
#define MILLIS_TO_CONTINUE  1000
unsigned long moveStartTime = 0;
Servo servoLeft;
Servo servoRight;

void setup()
{
  // Set up LED pins
  pinMode(leftFrontLED, OUTPUT);
  pinMode(leftRearLED, OUTPUT);
  pinMode(rightFrontLED, OUTPUT);
  pinMode(rightRearLED, OUTPUT);

  // Initialize serial communication for debugging.
  Serial.begin(9600);
  
  // Initialize the ethernet device
  Ethernet.begin(mac, ip, gateway, subnet);

  // start listening for clients
  server.begin();
  
  // Attach the servos
  servoLeft.attach(leftMotorPin);
  servoRight.attach(rightMotorPin);
  // TODO: Should we set speed to neutral on startup? Probably.
}

// This is done ONLY for debugging. It is very questionable, and may not work.
// (It is not normally valid C++ code unless Server.available() is implemented
// as a macro - which I doubt. LG...)
//Client client = server.available();

void loop()
{
  // Check for incoming clients.
  Client client = server.available();

  debug(client, "Main loop...");
  
  // If client found, begin parsing robot control commands.
  if (client) {
    
    // Output debug info.
    debug(client, "Client acquired...");
    
    // Echo valid commands to user.
    debug(client, "Movement keys: w, s, a, d Stop: q");
    
    // Process user input as long as connection persits.
    while (client.connected()) {
      
      // Stop robot if movement time limit exceeded.
        if (moveStartTime) {
          unsigned long now = millis();
          if (now - moveStartTime >= MILLIS_TO_CONTINUE) {
            stop_movement();
          }
        }
      
      // Check for user input.
      if (client.available()) {
        
        // Read the next character and echo it to client.
        char c = client.read();
        // Output debug info.
        debug(client, "Received input: ");
        debug(client, (char *)&c);
        
        // Perform movement.
        switch (c) {
          case CSTOP:
            debug(client, "Full Stop");
            stop_movement();
            break;
            
          case CFORWARD:
            debug(client, "Accelerating Forward");
            move_forward();
            break;
            
          case CREVERSE:
            debug(client, "Accelerating backward");
            move_backward();
            break;
            
          case CLEFT:
            debug(client, "Turning Left");
            turn_left();
            break;
            
          case CRIGHT:
            debug(client, "Turning Right");
            turn_right();
            break;
            
          case CFORWARD_FULL:
            debug(client, "Full Speed Ahead!");
            currentLeftSpeed = PULSE_FULL_FORWARD;
            currentRightSpeed = PULSE_FULL_FORWARD;
            update_servo_speed();
            break;
            
          case CREVERSE_FULL:
            debug(client, "Full Speed Reverse!");
            currentLeftSpeed = PULSE_FULL_REVERSE;
            currentRightSpeed = PULSE_FULL_REVERSE;
            update_servo_speed();
            break;
            
          default:
            debug(client, "Unrecognized command.");
        }
      }
    }
  }
  else { // No client connected - eventually "timeout" and stop.
    if (moveStartTime) {
      unsigned long now = millis();
      if (now - moveStartTime >= MILLIS_TO_CONTINUE) {
        stop_movement();
      }
    }
  }
}

void update_servo_speed() {
  int leftFrontSpeed = map(currentLeftSpeed, PULSE_NEUTRAL, PULSE_FULL_FORWARD, 0, 255);
  int rightFrontSpeed = map(currentRightSpeed, PULSE_NEUTRAL, PULSE_FULL_FORWARD, 0, 255);
  int leftBackSpeed = map(currentLeftSpeed, PULSE_FULL_REVERSE, PULSE_NEUTRAL, 0, 255);
  int rightBackSpeed = map(currentRightSpeed, PULSE_FULL_REVERSE, PULSE_NEUTRAL, 0, 255);
  analogWrite(leftFrontLED, leftFrontSpeed);
  analogWrite(rightFrontLED, rightFrontSpeed);
  analogWrite(leftRearLED, leftBackSpeed);
  analogWrite(rightRearLED, rightBackSpeed);
  servoLeft.writeMicroseconds(currentLeftSpeed);
  servoRight.writeMicroseconds(currentRightSpeed);
  moveStartTime = millis();
}

void debug(Client client, char *str) {
  client.println(str);
  Serial.println(str);
}

// Accelerate forward
void move_forward() 
{ if (currentLeftSpeed < PULSE_FULL_FORWARD)
    currentLeftSpeed += speedDelta;
  if (currentRightSpeed < PULSE_FULL_FORWARD)
    currentRightSpeed += speedDelta;
  update_servo_speed();
}

void move_backward() {
  if (currentLeftSpeed > PULSE_FULL_REVERSE)
    currentLeftSpeed -= speedDelta;
  if (currentRightSpeed > PULSE_FULL_REVERSE)
    currentRightSpeed -= speedDelta;
  update_servo_speed();
}

/* Turn commands assume we use tracks rather than wheels.
 * To turn left, we just stop the left track(s) and 
 * move the right track(s) forward.
 * Also note that this "turn" is relative. We probably want a way to
 * say "turn full left immediately" to allow tighter turns.
 */
void turn_left() 
{
  if (currentLeftSpeed > PULSE_FULL_REVERSE)
    currentLeftSpeed -= speedDelta;
  if (currentRightSpeed > PULSE_FULL_FORWARD)
    currentRightSpeed += speedDelta;
  update_servo_speed();
}

void turn_right() 
{
  if (currentLeftSpeed < PULSE_FULL_FORWARD)
    currentLeftSpeed += speedDelta;
  if (currentRightSpeed > PULSE_FULL_REVERSE)
    currentRightSpeed -= speedDelta;
  update_servo_speed();
}

void stop_movement() 
{
  currentLeftSpeed = PULSE_BRAKE;
  currentRightSpeed = PULSE_BRAKE;
  update_servo_speed();
  moveStartTime = 0;
}
