################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
CPP_SRCS += \
D:/Program\ Files/arduino-1.0/libraries/LiquidCrystal/LiquidCrystal.cpp 

OBJS += \
./LiquidCrystal/LiquidCrystal.o 

CPP_DEPS += \
./LiquidCrystal/LiquidCrystal.d 


# Each subdirectory must supply rules for building sources it contributes
LiquidCrystal/LiquidCrystal.o: D:/Program\ Files/arduino-1.0/libraries/LiquidCrystal/LiquidCrystal.cpp
	@echo 'Building file: $<'
	@echo 'Invoking: AVR C++ Compiler'
	avr-g++ -I"D:\Program Files\arduino-1.0\hardware\arduino\cores\arduino" -I"D:\Program Files\arduino-1.0\hardware\arduino\variants\standard" -I"D:\Documents and Settings\Leland\Workspaces\Mjolnir\SARC" -I"D:\Program Files\arduino-1.0\libraries\Ethernet" -I"D:\Program Files\arduino-1.0\libraries\Ethernet\utility" -I"D:\Program Files\arduino-1.0\libraries\Servo" -I"D:\Program Files\arduino-1.0\libraries\SPI" -I"D:\Program Files\arduino-1.0\libraries\Stepper" -I"D:\Program Files\arduino-1.0\libraries\LiquidCrystal" -I"D:\Program Files\arduino-1.0\libraries\AFMotor" -D__IN_ECLIPSE__=1 -DARDUINO=100 -Wall -g2 -gstabs -ffunction-sections -fdata-sections -Os -ffunction-sections -fdata-sections -fno-exceptions -g -mmcu=atmega328p -DF_CPU=16000000UL -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -felide-constructors -std=c++0x -c -o "$@" -x c++ "$<"
	@echo 'Finished building: $<'
	@echo ' '

