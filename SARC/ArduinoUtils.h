/* Collection of Arduino-specific utility functions. If you're running on a
 * different platform, you may (likely will) need to adapt the items in this
 * file. The idea is that you can leave Utilities.h as-is, and also the
 * other code that calls these functions/classes.
 *
 * Written for the Mjolnir project.
 * Copyright (c) 2011-2012 Leland Green... and Section9
 *
 * Web site: Section9 (http://section9.choamco.com/)
 * By: Leland Green...     Email: aboogieman (_at_) gmail.com
 *     Odysseus            Email: odysseus@choamco.com
 *
 * This file is free software; you can redistribute it and/or modify
 * it under the terms of either the GNU General Public License version 2
 * or the GNU Lesser General Public License version 2.1, both as
 * published by the Free Software Foundation.
 *
 */

/* It is important to call this function with the *first* time as time1.
 * This is because time2 may be smaller, and in that case we assume the
 * Arduino tick counter has overflowed and restarted at 0. This happens
 * approximately every 70 minutes.
 */
unsigned long timeDifference(unsigned long time1, unsigned long time2)
{
  if (time1 == 0 || time2 == 0) return 0;
  if (time2 > time1) return time2 - time1;
  if (time2 < time1) return (MAX_UNSIGNED_LONG - time1) + time2;
  return 0;
}

