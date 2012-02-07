SARC = Simple Arduino Robotic Control.	Version 1.0.0 Alpha			2012-02-06 

This is specialized for a tank (tracked) drive with 2 motors.
Copyright (c) 2011-2012 Leland Green... and Section9

This library is free software; you can redistribute it and/or modify
it under the terms of either the GNU General Public License version 2
or the GNU Lesser General Public License version 2.1, both as
published by the Free Software Foundation.

Written for Section9 (http://section9.choamco.com/) and 
our future robotic overlords (http://singinst.org/)
  By: Leland Green...     Email: aboogieman (_at_) gmail.com
      Odysseus            Email: odysseus@choamco.com

--- Disclaimer ---

The usual stuff. Use this code at your own risk. It is not guaranteed, and we
are not responsible for any damages, injuries or loss of life that results from
it, whether direct or indirect.

This code is currently in beta testing. (I just realized it doesn't even have
a version, yet! Let's call it 0.0.1. There, now it does!) It is not production 
quality, and some of the tools used to build it are very new technologies!

--- Instructions ---

This code is written to be used in Eclipse. To build it, you need the Arduino 
Eclipse plug-in and the STL port for AVR.

To actually *use* this code, you'll need a robot! :) Build instructions are
available here: http://section9.choamco.com/mjolnir/
The short summary of hardware we've tested with is:
  * Arduino (Uno was used for testing)
  * One of: 
  		* Ethernet shield *or* 
  		* 2 XBee devices (with appropriate shield) - one on the robot and
  		  one on a PC. See XBee.txt for information about configuring the
  		  XBee devices.
  * For the client app, an Android device. Note that this does *not* work
    with XBees. 

I cannot support any of the following. If you have troubles with them, please
contact their respective authors. 

1. Get Eclipse installed. Any version that handles C/C++ should work fine. I'm
	using 4.1 (Indigo). Hopefully you already have this installed - it's nice!

2. Follow the instructions for installing Andy Brown's port of STL for AVR. 
	Please read: 
	http://andybrown.me.uk/ws/2011/01/15/the-standard-template-library-stl-for-avr-with-c-streams/
	for usage and information, then download from his downloads page, here:
	http://andybrown.me.uk/ws/downloads/ 
	(At the time of this writing, a direct download link is: 
	http://andybrown.me.uk/ws/files/avr-stl-1.1.zip 
	However, if he has a newer version, please get that.)
	This library is very cool! If you think so, too, please email Andy and
	let him know.
	 
3. Install the Arduino plug-in for Eclipse. I'm using:
	Arduino eclipse extensions	1.1.6	it.baeyens.arduino.feature.feature.group	jan Baeyens
	Another great tool! To install and use it, please see this thread on the 
	Arduino forums: 
	http://arduino.cc/forum/index.php/topic,70547.msg589022.html#msg589022
	
	** Update ** Jantje has this page with easy-to-follow instructions:
	http://www.baeyens.it/eclipse/UsethePlugin.html
	
	Please read ALL of those instructions. If you have questions or problems, 
	please post on that thread so everyone will benefit from it.
	
	However, I will tell you about one glitch I encountered. When you set up a 
	new Arduino project, you choose your target device(s). I found that mine 
	would not build until I selected a different device and did a build-all for
	it. Then I could switch back to the original device and build worked. (I 
	think this may be fixed now... but just in case, keep it in mind.) 
	
	Also, you need to follow those instructions ***for every workspace and 
	project*** that you create. (Some are for the entire workspace, but some
	are for each project.)
	
4. Download the SARC code and add it to an Eclipse workspace. I've not actually 
	done this. I do plan to, and will update this file with additional 
	instructions as needed.

5. After you get things imported, you may see a lot of "Problems" reported by 
	Eclipse. Note that these do *not* get cleared when you build! You can click
	to select them all, then hit delete to get rid of them.
	
	*** Answer: This is, in fact, caused by the indexer. To resolve it, add the
	appropriate header files to C/C++ General -> Indexer -> Files to index up
	front. (I did this in the project settings and will commit this change.)
	You do not need to include the path. For example, I just added:
	", Arduino.h, Ethernet.h, Servo.h" (no quotes). and it cleared up.
	
	These messages do NOT affect the build. Check the Console panel for the true 
	results. If you see "**** Build Finished ****", you have just built SARC 
	and you can upload using the AVR button in Eclipse. (You'll only see it in 
	the C/C++ view.)
	
6. Please support Arduino and open source hardware! See Section9 for links to
	some of our favorite geek-sites.

Another great open source tool is Eclipse EGit (Git for Eclipse):
  Eclipse EGit	1.1.0.201109151100-r	org.eclipse.egit.feature.group	Eclipse EGit
This works great for accessing Git repositories directly from Eclipse.

--- Building ---

There are limited hardware changes supported in 1.0. See *.h for comments and
details. These changes can be made with #define, or project properties -> 
C/C++ Build -> Settings -> AVR C++ Compiler -> Settings -> Symbols.

Most of these are plain enough, but here are some definitions you should know about:

USE_ETHERNET - Sends/Receives control data via Ethernet Shield. (Or compatible.)
USE_XBEE	 - If you use this, control data is sent over Serial. This was tested
				with the SparkFun XBee Shield, but anything connected to the Serial
				RX/TX will work. (You can remove the XBee Shield and control will
				be via USB so you can test with any terminal.)
USE_LCD		 - Prints informational messages to the LCD screen.
LCD_IS_SERIAL - If you're using a serial LCD, you want this defined. If your LCD
				is NOT serial (but you're using one), you'll need to change/refer
				to the #defines in Display.h.
				
*** IMPORTANT NOTE *** Since XBee is only supported via RX/TX (Serial), this means
	*** USE_XBEE and USE_LCD + LCD_IS_SERIAL are mutually exclusive. However, you 
	*can* use both XBee and LCD if the LCD is non-serial (as most cheap ones are).
	I.e., Define USE_XBEE and USE_LCD but *not* LCD_IS_SERIAL for this configuration.
	
	*** Likewise *** USE_ETHERNET and USE_XBEE are mutually exclusive. At this 
	time we only support control via one or the other. (And only one client at 
	a time. We tried connecting with more, but Arduino didn't like that. I 
	don't anticipate much need for controlling via two methods.<g>)

There are a lot of other definitions in the header files. These four are the main 
ones for switching hardware. 

To develop for SARC, the following Eclipse Workspace settings are required:

1. Build Variable STL_INCLUDE_PATH. 
	This variable should reference your local STL include path folder.

2. Path Variable ARDUINO_CORE_PATH. 
	This variable should reference your Arduino Core source folder. (The folder 
	with Arduino.h in it).

3. Path Variable ARDUINO_VARIANT_PATH. 
	This variable should reference the folder with your board specific 
	Arudino_Pins.h file.

4. Path Variable ARDUINO_LIBRARIES_PATH. 
	This variable should reference the folder where the non-core Arduino 
	libraries are stored (Ethernet,Servo, SPI, etc.)

--- Epilog ---

While I cannot support this code, I do welcome questions and feedback. I'll 
reply as I have time. I'm a busy software engineer in my day job, as is 
Odysseus. But we'd like to share this project, so we do plan to be updating 
this project. Stay Tuned! 

Thanks for you interest and I hope you enjoy the project as much as we do!
Leland Green... 						February, 2012	Battlefield, MO USA
