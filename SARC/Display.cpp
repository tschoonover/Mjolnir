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
#include "WString.h"

#ifdef LCD_IS_SERIAL
	#include <SoftwareSerial.h>
	#include <Arduino.h>
#endif

Display::Display()
{
	_currentRow = 0;
	_currentColumn = 0;

#ifdef LCD_IS_SERIAL

	_SerialLCD = new SoftwareSerial(LCD_RX_PIN, LCD_TX_PIN);
	_SerialLCD->begin(9600);

	_blankline = String("");
	for(int i = 0; i < LCD_COLUMN_COUNT; i++)
		_blankline.concat(' ');

	clearBuffer();
	SetCursor(_currentRow, _currentColumn);

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

}

void Display::clearBuffer()
{
	for (int row = 0; row < LCD_ROW_COUNT; row++)
		_buffer[row] = _blankline;
}

void Display::SetCursor(uint8_t row, uint8_t col)
{
	#ifdef LCD_IS_SERIAL

		uint8_t base = 0;
		if (row == 1)
			base = 64;
		else if (row == 2)
			base = 20;
		else if (row == 3)
			base=84;

		_SerialLCD->write(0xFE);
		_SerialLCD->write(0x45);
		_SerialLCD->write(base + col);

	#else
		_lcd->setCursor(row, col);
	#endif
}

void Display::Clear(void)
{
	#ifdef LCD_IS_SERIAL
		clearBuffer();
		_SerialLCD->write(0xFE);
		_SerialLCD->write(0x51);
	#else
		_lcd->clear();
	#endif
}

void Display::Home(void)
{
	#ifdef LCD_IS_SERIAL
		_SerialLCD->write(0xFE);
		_SerialLCD->write(0x46);
	#else
		_lcd->home();
	#endif
}

void Display::On(void)
{
	#ifdef LCD_IS_SERIAL
		_SerialLCD->write(0xFE);
		_SerialLCD->write(0x41);
	#else
		_lcd->display();
	#endif
}

void Display::Off(void)
{
	#ifdef LCD_IS_SERIAL
		_SerialLCD->write(0xFE);
		_SerialLCD->write(0x42);
	#else
		_lcd->noDisplay();
	#endif
}

void Display::ScrollUp(void)
{
	for (int row = 0; row < LCD_ROW_COUNT - 1; row++)
		_buffer[row] = String(_buffer[row + 1]);

	_buffer[LCD_ROW_COUNT - 1] = _blankline;

	Refresh();
}

void Display::Refresh(void)
{
	for (int row = 0; row < LCD_ROW_COUNT; row++)
	{
		SetCursor(row, 0);
		_SerialLCD->print(_buffer[row]);
	}
}

void Display::PrintLine(const char* text)
{
	// Scroll the display if necessary.
	if (_currentRow < LCD_ROW_COUNT - 1)
		_currentRow++;
	else
		ScrollUp();

	SetCursor(_currentRow, _currentColumn);

	// Pad text with spaces.
	String paddedText = String(text);
	paddedText.concat(_blankline);
	paddedText = paddedText.substring(0, 20);

	// Write text to the display.
	Print(paddedText);
}

void Display::Print(const String &text)
{
	// Crop text if necessary.
	String croppedText = String(text);
	if (text.length() + _currentColumn > LCD_COLUMN_COUNT)
		croppedText = text.substring(0, LCD_COLUMN_COUNT - _currentColumn);

	// Update buffer.
	for (unsigned int i = 0; i < croppedText.length(); i++)
		_buffer[_currentRow].setCharAt(_currentColumn + i, croppedText.charAt(i));

	// Output text to device.
	#ifdef LCD_IS_SERIAL
		_SerialLCD->print(croppedText);
	#else
		_lcd->print(croppedText);
	#endif
}

