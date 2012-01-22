################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
CPP_SRCS += \
D:/Program\ Files/arduino-1.0/libraries/Ethernet/Dhcp.cpp \
D:/Program\ Files/arduino-1.0/libraries/Ethernet/Dns.cpp \
D:/Program\ Files/arduino-1.0/libraries/Ethernet/Ethernet.cpp \
D:/Program\ Files/arduino-1.0/libraries/Ethernet/EthernetClient.cpp \
D:/Program\ Files/arduino-1.0/libraries/Ethernet/EthernetServer.cpp \
D:/Program\ Files/arduino-1.0/libraries/Ethernet/EthernetUdp.cpp 

OBJS += \
./Ethernet/Dhcp.o \
./Ethernet/Dns.o \
./Ethernet/Ethernet.o \
./Ethernet/EthernetClient.o \
./Ethernet/EthernetServer.o \
./Ethernet/EthernetUdp.o 

CPP_DEPS += \
./Ethernet/Dhcp.d \
./Ethernet/Dns.d \
./Ethernet/Ethernet.d \
./Ethernet/EthernetClient.d \
./Ethernet/EthernetServer.d \
./Ethernet/EthernetUdp.d 


# Each subdirectory must supply rules for building sources it contributes
Ethernet/Dhcp.o: D:/Program\ Files/arduino-1.0/libraries/Ethernet/Dhcp.cpp
	@echo 'Building file: $<'
	@echo 'Invoking: AVR C++ Compiler'
	avr-g++ -I"D:\Program Files\arduino-1.0\hardware\arduino\cores\arduino" -I"D:\Program Files\arduino-1.0\hardware\arduino\variants\standard" -I"D:\Documents and Settings\Leland\Workspaces\Mjolnir\SARC" -I"D:\Program Files\arduino-1.0\libraries\Ethernet" -I"D:\Program Files\arduino-1.0\libraries\Ethernet\utility" -I"D:\Program Files\arduino-1.0\libraries\Servo" -I"D:\Program Files\arduino-1.0\libraries\SPI" -I"D:\Program Files\arduino-1.0\libraries\Stepper" -I"D:\Program Files\arduino-1.0\libraries\LiquidCrystal" -D__IN_ECLIPSE__=1 -DARDUINO=100 -Wall -g2 -gstabs -ffunction-sections -fdata-sections -Os -ffunction-sections -fdata-sections -fno-exceptions -g -mmcu=atmega328p -DF_CPU=16000000UL -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -felide-constructors -std=c++0x -c -o "$@" -x c++ "$<"
	@echo 'Finished building: $<'
	@echo ' '

Ethernet/Dns.o: D:/Program\ Files/arduino-1.0/libraries/Ethernet/Dns.cpp
	@echo 'Building file: $<'
	@echo 'Invoking: AVR C++ Compiler'
	avr-g++ -I"D:\Program Files\arduino-1.0\hardware\arduino\cores\arduino" -I"D:\Program Files\arduino-1.0\hardware\arduino\variants\standard" -I"D:\Documents and Settings\Leland\Workspaces\Mjolnir\SARC" -I"D:\Program Files\arduino-1.0\libraries\Ethernet" -I"D:\Program Files\arduino-1.0\libraries\Ethernet\utility" -I"D:\Program Files\arduino-1.0\libraries\Servo" -I"D:\Program Files\arduino-1.0\libraries\SPI" -I"D:\Program Files\arduino-1.0\libraries\Stepper" -I"D:\Program Files\arduino-1.0\libraries\LiquidCrystal" -D__IN_ECLIPSE__=1 -DARDUINO=100 -Wall -g2 -gstabs -ffunction-sections -fdata-sections -Os -ffunction-sections -fdata-sections -fno-exceptions -g -mmcu=atmega328p -DF_CPU=16000000UL -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -felide-constructors -std=c++0x -c -o "$@" -x c++ "$<"
	@echo 'Finished building: $<'
	@echo ' '

Ethernet/Ethernet.o: D:/Program\ Files/arduino-1.0/libraries/Ethernet/Ethernet.cpp
	@echo 'Building file: $<'
	@echo 'Invoking: AVR C++ Compiler'
	avr-g++ -I"D:\Program Files\arduino-1.0\hardware\arduino\cores\arduino" -I"D:\Program Files\arduino-1.0\hardware\arduino\variants\standard" -I"D:\Documents and Settings\Leland\Workspaces\Mjolnir\SARC" -I"D:\Program Files\arduino-1.0\libraries\Ethernet" -I"D:\Program Files\arduino-1.0\libraries\Ethernet\utility" -I"D:\Program Files\arduino-1.0\libraries\Servo" -I"D:\Program Files\arduino-1.0\libraries\SPI" -I"D:\Program Files\arduino-1.0\libraries\Stepper" -I"D:\Program Files\arduino-1.0\libraries\LiquidCrystal" -D__IN_ECLIPSE__=1 -DARDUINO=100 -Wall -g2 -gstabs -ffunction-sections -fdata-sections -Os -ffunction-sections -fdata-sections -fno-exceptions -g -mmcu=atmega328p -DF_CPU=16000000UL -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -felide-constructors -std=c++0x -c -o "$@" -x c++ "$<"
	@echo 'Finished building: $<'
	@echo ' '

Ethernet/EthernetClient.o: D:/Program\ Files/arduino-1.0/libraries/Ethernet/EthernetClient.cpp
	@echo 'Building file: $<'
	@echo 'Invoking: AVR C++ Compiler'
	avr-g++ -I"D:\Program Files\arduino-1.0\hardware\arduino\cores\arduino" -I"D:\Program Files\arduino-1.0\hardware\arduino\variants\standard" -I"D:\Documents and Settings\Leland\Workspaces\Mjolnir\SARC" -I"D:\Program Files\arduino-1.0\libraries\Ethernet" -I"D:\Program Files\arduino-1.0\libraries\Ethernet\utility" -I"D:\Program Files\arduino-1.0\libraries\Servo" -I"D:\Program Files\arduino-1.0\libraries\SPI" -I"D:\Program Files\arduino-1.0\libraries\Stepper" -I"D:\Program Files\arduino-1.0\libraries\LiquidCrystal" -D__IN_ECLIPSE__=1 -DARDUINO=100 -Wall -g2 -gstabs -ffunction-sections -fdata-sections -Os -ffunction-sections -fdata-sections -fno-exceptions -g -mmcu=atmega328p -DF_CPU=16000000UL -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -felide-constructors -std=c++0x -c -o "$@" -x c++ "$<"
	@echo 'Finished building: $<'
	@echo ' '

Ethernet/EthernetServer.o: D:/Program\ Files/arduino-1.0/libraries/Ethernet/EthernetServer.cpp
	@echo 'Building file: $<'
	@echo 'Invoking: AVR C++ Compiler'
	avr-g++ -I"D:\Program Files\arduino-1.0\hardware\arduino\cores\arduino" -I"D:\Program Files\arduino-1.0\hardware\arduino\variants\standard" -I"D:\Documents and Settings\Leland\Workspaces\Mjolnir\SARC" -I"D:\Program Files\arduino-1.0\libraries\Ethernet" -I"D:\Program Files\arduino-1.0\libraries\Ethernet\utility" -I"D:\Program Files\arduino-1.0\libraries\Servo" -I"D:\Program Files\arduino-1.0\libraries\SPI" -I"D:\Program Files\arduino-1.0\libraries\Stepper" -I"D:\Program Files\arduino-1.0\libraries\LiquidCrystal" -D__IN_ECLIPSE__=1 -DARDUINO=100 -Wall -g2 -gstabs -ffunction-sections -fdata-sections -Os -ffunction-sections -fdata-sections -fno-exceptions -g -mmcu=atmega328p -DF_CPU=16000000UL -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -felide-constructors -std=c++0x -c -o "$@" -x c++ "$<"
	@echo 'Finished building: $<'
	@echo ' '

Ethernet/EthernetUdp.o: D:/Program\ Files/arduino-1.0/libraries/Ethernet/EthernetUdp.cpp
	@echo 'Building file: $<'
	@echo 'Invoking: AVR C++ Compiler'
	avr-g++ -I"D:\Program Files\arduino-1.0\hardware\arduino\cores\arduino" -I"D:\Program Files\arduino-1.0\hardware\arduino\variants\standard" -I"D:\Documents and Settings\Leland\Workspaces\Mjolnir\SARC" -I"D:\Program Files\arduino-1.0\libraries\Ethernet" -I"D:\Program Files\arduino-1.0\libraries\Ethernet\utility" -I"D:\Program Files\arduino-1.0\libraries\Servo" -I"D:\Program Files\arduino-1.0\libraries\SPI" -I"D:\Program Files\arduino-1.0\libraries\Stepper" -I"D:\Program Files\arduino-1.0\libraries\LiquidCrystal" -D__IN_ECLIPSE__=1 -DARDUINO=100 -Wall -g2 -gstabs -ffunction-sections -fdata-sections -Os -ffunction-sections -fdata-sections -fno-exceptions -g -mmcu=atmega328p -DF_CPU=16000000UL -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -felide-constructors -std=c++0x -c -o "$@" -x c++ "$<"
	@echo 'Finished building: $<'
	@echo ' '


