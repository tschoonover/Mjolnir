################################################################################
# Automatically-generated file. Do not edit!
################################################################################

-include ../makefile.init

RM := rm -rf

# All of the sources participating in the build are defined here
-include sources.mk
-include Stepper/subdir.mk
-include Servo/subdir.mk
-include SPI/subdir.mk
-include LiquidCrystal/subdir.mk
-include Ethernet/utility/subdir.mk
-include Ethernet/subdir.mk
-include ArduinoCore/subdir.mk
-include AFMotor/subdir.mk
-include subdir.mk
-include objects.mk

ifneq ($(MAKECMDGOALS),clean)
ifneq ($(strip $(C++_DEPS)),)
-include $(C++_DEPS)
endif
ifneq ($(strip $(C_DEPS)),)
-include $(C_DEPS)
endif
ifneq ($(strip $(ASM_DEPS)),)
-include $(ASM_DEPS)
endif
ifneq ($(strip $(CC_DEPS)),)
-include $(CC_DEPS)
endif
ifneq ($(strip $(CPP_DEPS)),)
-include $(CPP_DEPS)
endif
ifneq ($(strip $(S_DEPS)),)
-include $(S_DEPS)
endif
ifneq ($(strip $(CXX_DEPS)),)
-include $(CXX_DEPS)
endif
ifneq ($(strip $(C_UPPER_DEPS)),)
-include $(C_UPPER_DEPS)
endif
ifneq ($(strip $(S_UPPER_DEPS)),)
-include $(S_UPPER_DEPS)
endif
endif

-include ../makefile.defs

# Add inputs and outputs from these tool invocations to the build variables 
LSS += \
SARC.lss \

FLASH_IMAGE += \
SARC.hex \

EEPROM_IMAGE += \
SARC.eep \

SIZEDUMMY += \
sizedummy \


# All Target
all: SARC.elf secondary-outputs

# Tool invocations
SARC.elf: $(OBJS) $(USER_OBJS)
	@echo 'Building target: $@'
	@echo 'Invoking: AVR C++ Linker'
	avr-gcc  -L"D:\Documents and Settings\Leland\Workspaces\Mjolnir\Arduino_Uno/Release" -mmcu=atmega328p --cref -s -Os -Wl,--gc-sections  -o"SARC.elf" $(OBJS) $(USER_OBJS) $(LIBS) -lm
	@echo 'Finished building target: $@'
	@echo ' '

SARC.lss: SARC.elf
	@echo 'Invoking: AVR Create Extended Listing'
	-avr-objdump -h -S SARC.elf  >"SARC.lss"
	@echo 'Finished building: $@'
	@echo ' '

SARC.hex: SARC.elf
	@echo 'Create Flash image (ihex format)'
	-avr-objcopy -R .eeprom -O ihex SARC.elf  "SARC.hex"
	@echo 'Finished building: $@'
	@echo ' '

SARC.eep: SARC.elf
	@echo 'Create eeprom image (ihex format)'
	-avr-objcopy -j .eeprom --no-change-warnings --change-section-lma .eeprom=0 -O ihex SARC.elf  "SARC.eep"
	@echo 'Finished building: $@'
	@echo ' '

sizedummy: SARC.elf
	@echo 'Invoking: Print Size'
	-avr-size --format=avr --mcu=atmega328p SARC.elf
	@echo 'Finished building: $@'
	@echo ' '

# Other Targets
clean:
	-$(RM) $(OBJS)$(C_DEPS)$(EEPROM_IMAGE)$(ELFS)$(FLASH_IMAGE)$(LSS)$(S_DEPS)$(CXX_DEPS)$(S_UPPER_DEPS)$(C++_DEPS)$(ASM_DEPS)$(CC_DEPS)$(CPP_DEPS)$(C_UPPER_DEPS)$(SIZEDUMMY) SARC.elf
	-@echo ' '

secondary-outputs: $(LSS) $(FLASH_IMAGE) $(EEPROM_IMAGE) $(SIZEDUMMY)

.PHONY: all clean dependents
.SECONDARY:

-include ../makefile.targets
