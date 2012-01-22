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
#include <HardwareSerial.h>

extern HardwareSerial Serial;

Display::Display() {

	// Allocate String objects to help us keep track of displayed text, allow scrolling, etc.
	for (int j=0; j<LCD_ROW_COUNT; j++)
	{
		this->BlankRow(j);
	}

	_currentRow = 0;
	_currentColumn = 0;

#ifdef LCD_IS_SERIAL

	Serial.begin(9600); // Call this here and not in your code.

#else // Not LCD_IS_SERIAL

	// If anyone wants RW support, we should add it here.
	#ifdef LCD_USE_8_PINS

		*_lcd = LiquidCrystal(LCD_PIN_RS, LCD_PIN_ENABLE,
							  LCD_PIN_D0, LCD_PIN_D1, LCD_PIN_D2, LCD_PIN_D3,
							  LCD_PIN_D4, LCD_PIN_D5, LCD_PIN_D6,LCD_PIN_D7);

	#else // Assume 4 pins

		*_lcd = LiquidCrystal(LCD_PIN_RS, LCD_PIN_ENABLE,
							  LCD_PIN_D4, LCD_PIN_D5, LCD_PIN_D6,LCD_PIN_D7);

	#endif // LCD_USE_8_PINS

#endif // (else not) LCD_IS_SERIAL

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
		Serial.write(base + col - 1);
	#else
		_lcd->setCursor(row, col);
	#endif	// LCD_IS_SERIAL

} // Display::SetCursor()

void Display::Clear(void)
{
	#ifdef LCD_IS_SERIAL
		Serial.write(0xFE);
		Serial.write(0x51);
	#else
		_lcd->clear();
	#endif
}

void Display::Home(void)
{
	#ifdef LCD_IS_SERIAL
		Serial.write(0xFE);
		Serial.write(0x46);
	#else
		_lcd->home();
	#endif
}

void Display::On(void)
{
	#ifdef LCD_IS_SERIAL
		Serial.write(0xFE);
		Serial.write(0x41);
	#else
		_lcd->display();
	#endif
}

void Display::Off(void)
{
	#ifdef LCD_IS_SERIAL
		Serial.write(0xFE);
		Serial.write(0x42);
	#else
		_lcd->noDisplay();
	#endif
}

// TODO:
//void Display::AutoScroll(void)
//{
//	#ifdef LCD_IS_SERIAL
//	#else
//	#endif
//}
//

/*
 * Writes the entire buffer, one row at a time.
 */
void Display::WriteBuffer(void)
{
	//this->Clear(); // Would this cause flicker? (If called too often?)
	for (int j=0; j<LCD_ROW_COUNT; j++)
	{
		for (unsigned int k=0; k<_buffer[j].length(); k++)
		{
			_buffer[j].concat(' ');
		}
		this->SetCursor(j, 0);
		this->Print(_buffer[j]);
	}
}

void Display::Refresh(void)
{
	this->WriteBuffer();
}

/*
 * Modifies _tempString. Returns a string of spaces LCD_COLUMN_COUNT long.
 *
 * @param: rowNumber The number of the row. 1 = first row. Max = LCD_COLUMN_COUNT.
 */
void Display::BlankRow(uint8_t rowNumber)
{
	if (rowNumber < 1 || rowNumber > LCD_ROW_COUNT) return; // just forget it
	for (int j=0; j<LCD_COLUMN_COUNT; j++)
	{
		_buffer[rowNumber - 1][j] = ' ';
	}
}

void Display::ScrollUp(void)
{
	for (int j=1; j<LCD_ROW_COUNT; j++)
	{
		_buffer[j-1] = _buffer[j];
	}
	_buffer[LCD_ROW_COUNT-1] = "";
	for (int j=0; j<LCD_ROW_COUNT; j++)
	{
		_buffer[LCD_ROW_COUNT-1].concat(" ");
	}

}

void Display::PrintLine(const char* string)
{
	if (_currentRow < LCD_ROW_COUNT)
		_currentRow += 1;
	else
		this->ScrollUp();

	this->SetCursor(_currentRow, 0);
	this->Print(string);
	for (uint8_t n = strlen(string); n < LCD_COLUMN_COUNT; ++n)
	{
		this->Print(' ');
	}
}

// Print implementations below this point

/*
 * Other overloads call this method.
 */
void Display::Print(const String& string)
{
	// First update our buffer, just for scrolling.
	for (unsigned int j=0; j<string.length() && _currentColumn + j<LCD_COLUMN_COUNT; j++)
		_buffer[_currentRow][_currentColumn+j] = string[j];
	_currentColumn += string.length();

	// Then output the string to the device.
	#ifdef LCD_IS_SERIAL
		char tempString[LCD_COLUMN_COUNT+1]="";
		string.toCharArray(tempString, LCD_COLUMN_COUNT, 0);
		Serial.print(tempString);
	#else
		_lcd->print(string);
	#endif
}

void Display::Print(const char* string)
{
	String tempString(string);
	this->Print(tempString);
}

void Display::Print(char ch)
{
	String tempString(ch);
	this->Print(tempString);
}

void Display::Print(unsigned char ch, int format)
{
	String tempString(ch, format);
	this->Print(tempString);
}

void Display::Print(int num, int format)
{
	String tempString(num, format);
	this->Print(tempString);
}

void Display::Print(unsigned int num, int format)
{
	String tempString(num, format);
	this->Print(tempString);
}

void Display::Print(long lnum, int format)
{
	String tempString(lnum, format);
	this->Print(tempString);
}

void Display::Print(unsigned long lnum, int format)
{
	String tempString(lnum, format);
	this->Print(tempString);
}

//void Display::Print(unsigned double dnum, unsigned int format)
//{
//	String tempString(dnum, format);
//	this->Print(tempString);
//}

// Warning: This is not implemented because it won't update the buffer. You're welcome
// to use it, just so you're aware of that.
//void Display::Print(const Printable& printable)
//{
//	#ifdef LCD_IS_SERIAL
//		Serial.Print(printable);
//	#else
//		_lcd->Print(printable);
//	#endif
//}
//
