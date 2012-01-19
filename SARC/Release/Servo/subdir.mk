################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
CPP_SRCS += \
D:/Program\ Files/arduino-1.0/libraries/Servo/Servo.cpp 

OBJS += \
./Servo/Servo.o 

CPP_DEPS += \
./Servo/Servo.d 


# Each subdirectory must supply rules for building sources it contributes
Servo/Servo.o: D:/Program\ Files/arduino-1.0/libraries/Servo/Servo.cpp
	@echo 'Building file: $<'
	@echo 'Invoking: AVR C++ Compiler'
	avr-g++ -I"D:\Program Files\arduino-1.0\hardware\tools\avr\avr\include" -I"D:\Program Files\arduino-1.0\hardware\arduino\cores\arduino" -I"D:\Program Files\arduino-1.0\hardware\arduino\variants\standard" -I"D:\Documents and Settings\Leland\Workspaces\Mjolnir\SARC" -I"D:\Program Files\arduino-1.0\libraries\Ethernet" -I"D:\Program Files\arduino-1.0\libraries\Ethernet\utility" -I"D:\Program Files\arduino-1.0\libraries\Servo" -I"D:\Program Files\arduino-1.0\libraries\SPI" -I"D:\Program Files\arduino-1.0\libraries\Stepper" -D__IN_ECLIPSE__=1 -DARDUINO=100 -Wall -Os -ffunction-sections -fdata-sections -fno-exceptions -g -mmcu=atmega328p -DF_CPU=16000000UL -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -c -o "$@" -x c++ "$<"
	@echo 'Finished building: $<'
	@echo ' '


