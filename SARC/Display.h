/*
 * Display.h
 *
 *  Created on: Jan 21, 2012
 *      Author: Leland Green
 */

#ifndef DISPLAY_H_
#define DISPLAY_H_

#include "WString.h"
#include "Print.h"

//#define USE_LCD

/* Use LCD_IS_SERIAL if you have a serial LCD. Right now, we only support this model:
 * 	http://www.jameco.com/webapp/wcs/stores/servlet/Product_10001_10001_2118686_-1
 * (Although, in theory, any model by that manufacturer will work.)
 * Just connect voltage, ground and the Arduino TX to your LCD RX.
 */
//#define LCD_IS_SERIAL

//#define LCD_USE_8_PINS	// Not sure why you'd want to do this, but we support it.
//#define LCD_USE_RW		// RW mode is not supported at this time.

#ifdef LCD_IS_SERIAL

//#include <SoftwareSerial.h>

#define LCD_RX_PIN 			4
#define LCD_TX_PIN 			5		// You only need to set this, baud rate and row count.
#define LCD_BAUD_RATE		9600
#define LCD_ROW_COUNT		4		// TODO: Implement a better way to handle this.
#define LCD_COLUMN_COUNT	20

#else // Not LCD_IS_SERIAL

#include <LiquidCrystal.h>

#define LCD_ROW_COUNT		2		// TODO: Implement merge of this with same mnemonic above.
#define LCD_COLUMN_COUNT	20		// TODO: And this one.
/*
 * For details on hooking up your LCD, see the LiquidCrystal documentation at:
 * 			http://arduino.cc/en/Reference/LiquidCrystalConstructor
 * From that documentation:
 * LiquidCrystal(rs, enable, d4, d5, d6, d7)
 * LiquidCrystal(rs, rw, enable, d4, d5, d6, d7)
 * LiquidCrystal(rs, enable, d0, d1, d2, d3, d4, d5, d6, d7)
 * LiquidCrystal(rs, rw, enable, d0, d1, d2, d3, d4, d5, d6, d7)
 *
 * The following mnemonics are somewhat randomly chosen. The numbers are the
 * Arduino pin numbers. See data sheet of your LCD for instructions on which
 * connection is which.
 */

#define LCD_PIN_RS 			7
//#define LCD_PIN_RW		-1
#define LCD_PIN_ENABLE		8

// Note that D0 through D3 are only needed if you're
//#define LCD_PIN_D0		-1
//#define LCD_PIN_D1		-1
//#define LCD_PIN_D2		-1
//#define LCD_PIN_D3		-1
#define LCD_PIN_D4		9
#define LCD_PIN_D5		10
#define LCD_PIN_D6		11
#define LCD_PIN_D7		12

#endif // LCD_IS_SERIAL


/*
 * Encapsulation of display, for connecting an LCD.
 */
class Display
{

public:

	Display();

	void Clear(void);
	void Home(void);
	void On(void);
	void Off(void);
	void SetCursor(uint8_t row, uint8_t col);
    void Print(const String &text);
    void PrintLine(const char *);
    void PrintLine(const String &);
    void ScrollUp(void);
    void Refresh(void);

private:

    uint8_t _currentRow;
    uint8_t _currentColumn;

	#ifdef LCD_IS_SERIAL
//	SoftwareSerial* _SerialLCD;
	String _buffer[LCD_ROW_COUNT];
	String _blankline;
	void clearBuffer();

	#else

	LiquidCrystal* _lcd;

	#endif
};

#endif /* DISPLAY_H_ */
