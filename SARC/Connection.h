/*
 * Connection.h
 *
 *  Created on: Jan 23, 2012
 *      Author: Leland Green
 *
 *  The Connection class encapsulates the server and client. It presents a
 *  Server and a Client that wrap and abstract out the actual objects.
 *  For example, if you're using Ethernet, the Server and Client will be the
 *  standard EthernetServer and EthernetClient, respectively.
 *
 *  Note that the robot is the server and whatever you're connecting with
 *  is the client. That's because the port is opened on the robot and it
 *  listens for incoming connections.
 *
 */

#ifndef CONNECTION_H_
#define CONNECTION_H_

//#define USE_ETHERNET	// You should define either USE_ETHERNET or USE_XBEE.
//#define USE_XBEE

#ifdef USE_ETHERNET
#include <Ethernet.h>

#ifdef CONNECTION_CPP_
/************ ETHERNET SHIELD CONFIG ************/
byte mac[] = { 0x90, 0xA2, 0xDA, 0x00, 0x3B, 0xB2 }; // MAC address
byte ip[] = { 192, 168, 1, 99 }; // IP address
byte gateway[] = { 192, 168, 1, 1 }; // Gateway address
byte subnet[] = { 255, 255, 255, 0 }; // Subnet mask
unsigned int port = 23; // Port number (Telnet)

#endif //CONNECTION_CPP_

#endif // USE_ETHERNET

#ifdef USE_XBEE
#include <HardwareSerial.h>
extern HardwareSerial Serial;

#ifdef WARN_USING_MESSAGES
#warning "Using XBee"
#endif

#endif // USE_XBEE

namespace SARC {

/*
 *
 */
class Connection {
public:
	Connection();
	virtual ~Connection();

	bool ClientIsConnected(void);
	bool ClientDataAvailable(void);
	char Read(void);

	size_t PrintLine(const char*);

private:
	#ifdef USE_ETHERNET
		// TODO: Wrap in better abstraction so all clients have same capabilities/properties.
		EthernetServer* _server;
		EthernetClient _client;
	#endif // USE_ETHERNET

	#ifdef USE_XBEE
		// Simply using XBee in UART mode, which is the simplest and saves pins.
		// This should work with anything connected to Arduino Rx/Tx pins.
	#endif // USE_XBEE

};	// class Connection

} /* namespace SARC */
#endif /* CONNECTION_H_ */
