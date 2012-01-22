/*
 * Display.cpp
 *
 *  Created on: Jan 21, 2012
 *      Author: Leland Green
 *
 *  Encapsulations of various display devices. To change device(s), uncomment and
 *  comment-out the appropriate #defines and change the LCD_PIN_* mnemonics to match the
 *  pins that you want to use.
 *
 *  *** @See: Display.h
 *
 */

#include "Display.h"
//#include <HardwareSerial.h>

Display::Display() {
	_buffer = (char *) calloc(LCD_COLUMN_COUNT, sizeof(char));
	// if (_buffer == NULL) TODO: Disable this instance? How?
	// If you can't allocate 20 bytes, you've got bigger prolems than this! :)

#ifdef LCD_IS_SERIAL

	Serial.begin(9600); // Call this here and not in your code.

#else

	// If anyone wants RW support, we should add it here.
	#ifdef LCD_USE_8_PINS

		*_lcd = LiquidCrystal(LCD_PIN_RS, LCD_PIN_ENABLE,
							  LCD_PIN_D0, LCD_PIN_D1, LCD_PIN_D2, LCD_PIN_D3,
							  LCD_PIN_D4, LCD_PIN_D5, LCD_PIN_D6,LCD_PIN_D7);

	#else // Assume 4 pins

		*_lcd = LiquidCrystal(LCD_PIN_RS, LCD_PIN_ENABLE,
							  LCD_PIN_D4, LCD_PIN_D5, LCD_PIN_D6,LCD_PIN_D7);

	#endif // LCD_USE_8_PINS
#endif // LCD_IS_SERIAL

}	// Display::Display()

void Display::SetCursor(uint8_t row, uint8_t col)
{
	#ifdef LCD_IS_SERIAL
		int base = 0;
		if (row == 2)
			base=64;
		else if (row == 3)
			base=20;
		else if (row == 4)
			base=84;
		Serial.write(base + col - 1;)
	#else
		_lcd->setCursor(row, col);
	#endif	// LCD_IS_SERIAL

} // Display::SetCursor()

void Display::PrintLine(const char* string)
{
	this->SetCursor(_currentRow, 0);
	this->Print(string);
	for (uint8_t n = strlen(string); n < LCD_COLUMN_COUNT; ++n)
	{
		this->Print(' ');
	}
	if (_currentRow < LCD_ROW_COUNT)
		_currentRow += 1;
}

// Print implementations below this point

void Display::Print(const String& string)
{
	#ifdef LCD_IS_SERIAL
		Serial.Print(string);
	#else
		_lcd->print(string);
	#endif
}

void Display::Print(const char* string)
{
	#ifdef LCD_IS_SERIAL
		Serial.Print(string);
	#else
		_lcd->print(string);
	#endif
}

void Display::Print(char ch)
{
	#ifdef LCD_IS_SERIAL
		Serial.Print(ch);
	#else
		_lcd->print(ch);
	#endif
}

void Display::Print(unsigned char ch, int format)
{
	#ifdef LCD_IS_SERIAL
		Serial.Print(ch, format);
	#else
		_lcd->print(ch, format);
	#endif
}

void Display::Print(int num, int format)
{
	#ifdef LCD_IS_SERIAL
		Serial.Print(num, format);
	#else
		_lcd->print(num, format);
	#endif
}

void Display::Print(unsigned int num, int format)
{
	#ifdef LCD_IS_SERIAL
		Serial.Print(num, format);
	#else
		_lcd->print(num, format);
	#endif
}

void Display::Print(long lnum, int format)
{
	#ifdef LCD_IS_SERIAL
		Serial.Print(lnum, format);
	#else
		_lcd->print(lnum, format);
	#endif
}

void Display::Print(unsigned long lnum, int format)
{
	#ifdef LCD_IS_SERIAL
		Serial.Print(lnum, format);
	#else
		_lcd->print(lnum, format);
	#endif
}

void Display::Print(double dnum, int format)
{
	#ifdef LCD_IS_SERIAL
		Serial.Print(dnum, format);
	#else
		_lcd->print(dnum, format);
	#endif
}

void Display::Print(const Printable& printable)
{
	#ifdef LCD_IS_SERIAL
		Serial.Print(printable);
	#else
		_lcd->print(printable);
	#endif
}

