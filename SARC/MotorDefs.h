#ifndef __MOTORDEFS_DEFINED__	// Leaving this as non-standard name so you can define in any file.
#define __MOTORDEFS_DEFINED__

/*
 * You should define either USE_AF_MOTORS or USE_VEX_MOTORS, not both.
 */

//#define USE_AF_MOTORS
#define USE_VEX_MOTORS

#ifdef USE_VEX_MOTORS
/************ VEX MOTOR CONTROL DEFINITIONS ************/
#define VEX_BRAKE        200
#define VEX_FULL_FORWARD 2000
#define VEX_NEUTRAL      1500
#define VEX_FULL_REVERSE 1000
#define VEX_SPEED_DELTA  10

#define SPEED_DELTA VEX_SPEED_DELTA

namespace MotorDefs {
/*
 * MotorDefs specify how movement is accomplished. Since we're always interested
 * in a direction for movement, we have this enumeration for natural language
 * concepts, e.g. forward, reverse, neutral and brake.
 *
 * Some *important* assumption are:
 * 		neutral - (forward - neutral) = reverse
 * 		neutral + (neutral - reverse) = forward
 * 	These "laws of speed delta" are used in the algorithms to reverse a course
 * 	stored in the command history.
 *
 * 	If your motor requirements do not meet this requirement, you should
 * 	modify reverse() accordingly.
 */
	enum _MotorDefs
	{
		brake = VEX_BRAKE,
		neutral = VEX_NEUTRAL,
		forward = VEX_FULL_FORWARD,
		reverse = VEX_FULL_REVERSE,
		speed = VEX_SPEED_DELTA,
		delta = VEX_SPEED_DELTA
	} ;

} // namespace MotorDefs

#endif // USE_VEX_MOTORS

#ifdef USE_AF_MOTORS

/*
 * For Adafruit motor shield, I'm only supporting DC motors at this point.
 * (I will be happy to add support for others if you email me.)
 *
 * Adafruit.com has more answers than I ever could. See the FAQ here:
 * 		http://www.ladyada.net/make/mshield/faq.html
 * From that page:
 *  The following pins are in use only if the DC/Stepper noted is in use:
 *   Digital pin 11: DC Motor #1 / Stepper #1 (activation/speed control)
 *   Digital pin  3: DC Motor #2 / Stepper #1 (activation/speed control)
 *   Digital pin  5: DC Motor #3 / Stepper #2 (activation/speed control)
 *   Digital pin  6: DC Motor #4 / Stepper #2 (activation/speed control)
 *  The following pins are in use if any DC/steppers are used
 *   Digital pin  4, 7, 8 and 12 are used to drive the DC/Stepper motors via the 74HC595 serial-to-parallel latch
 *
 *  The following pins are used only if that particular servo is in use:
 *   Digitals pin 9: Servo #1 control
 *   Digital pin 10: Servo #2 control
 */

// The following define which motor you want to use
#define AF_MOTOR_LEFT		1
#define AF_MOTOR_RIGHT		2


#endif // USE_AF_MOTORS

// General definitions to aid working with motor movements and deltas.
// You may need to change, depending on how your motor "speeds" are defined.
#define HALF_SPEED_DELTA ((forward - neutral) / 2)
#define FORWARD_HALF_SPEED (neutral + HALF_SPEED_DELTA)
#define REVERSE_HALF_SPEED (neutral - HALF_SPEED_DELTA)
#define FORWARD_SPEED_INCREMENTAL(a) (neutral + ((forward - neutral) / a))
#define REVERSE_SPEED_INCREMENTAL(a) (neutral - ((neutral - reverse) / a))

#endif // __MOTORDEFS_DEFINED__
