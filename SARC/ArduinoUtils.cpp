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
 *     Odysseus            Email: odysseus.section9@gmail.com
 *
 * This file is free software; you can redistribute it and/or modify
 * it under the terms of either the GNU General Public License version 2
 * or the GNU Lesser General Public License version 2.1, both as
 * published by the Free Software Foundation.
 *
 */

#ifndef ARDUINOUTILS_CPP
#define ARDUINOUTILS_CPP
namespace SARC {

#define MAX_UNSIGNED_LONG 0xffffffff // 4,294,967,295

/* It is important to call this function with the *first* time as earlyTime.
 * This is because newestTime may be smaller, and in that case we assume the
 * Arduino tick counter has overflowed and restarted at 0. This happens
 * approximately every 70 minutes.
 * By using this simple requirement, we eliminate more complex logic of
 * dealing with timestamps, mainly because the Arduino does not have
 * timestamps. Although I have seen an OS Arduino project that does, we're
 * being efficient here.
 * Also, by using a different include file, we open the possibility for
 * other platforms to use the same source code. (In other SARC source
 * files.) This would be ideal because the smaller (earlier) timestamp
 * could always be subtracted from the larger one and it would be
 * correct. (I.e., you wouldn't have to worry about calling this with
 * the earlier timestamp first.
 */
unsigned long timeDifference(unsigned long earlyTime, unsigned long newestTime)
{
  if (earlyTime == 0 || newestTime == 0) return 0;
  if (newestTime > earlyTime) return newestTime - earlyTime;
  if (newestTime < earlyTime) return (MAX_UNSIGNED_LONG - earlyTime) + newestTime;
  return 0;
}

} // namespace SARC

#endif
