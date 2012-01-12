#ifndef __MOTORDEFS_DEFINED__
#define __MOTORDEFS_DEFINED__

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
// General definitions to aid working with motor movements and deltas.
#define HALF_SPEED_DELTA ((forward - neutral) / 2)
#define FORWARD_HALF_SPEED (neutral + HALF_SPEED_DELTA)
#define REVERSE_HALF_SPEED (neutral - HALF_SPEED_DELTA)
#define FORWARD_SPEED_INCREMENTAL(a) (neutral + ((forward - neutral) / a))
#define REVERSE_SPEED_INCREMENTAL(a) (neutral - ((neutral - reverse) / a))

#endif // __MOTORDEFS_DEFINED__
