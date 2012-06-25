/*
 * Connection.cpp
 *
 *  Created on: Jan 23, 2012
 *      Author: Leland Green
 */

#define CONNECTION_CPP_

#include "Connection.h"

namespace SARC {

Connection::Connection()
{
	#ifdef USE_ETHERNET
		Ethernet.begin(mac, ip, gateway, subnet);
		_server = new EthernetServer(port);
		_server->begin();
		_client = _server->available();
	#endif // USE_ETHERNET

	#ifdef USE_XBEE
		Serial.begin(9600);
	#endif // USE_XBEE
}

bool Connection::ClientIsConnected(void)
{
	#ifdef USE_ETHERNET
		if (!_client.connected())
		{
			_client = _server->available();
		}
		return _client.connected();
	#endif

	#ifdef USE_XBEE
		return true;
		//return Serial.available(); // For now, assume we're connected unless timeout is reached.
	#endif
}

bool Connection::ClientDataAvailable()
{
	#ifdef USE_ETHERNET
		if (_client) return _client.available();
		return false;
	#endif

	#ifdef USE_XBEE
		return Serial.available(); // For now, assume we're connected unless timeout is reached.
	#endif
}

/*
 * Please note: This will return '\0' if no data is available! Recommended
 * usage is to call this only when ClientDataAvailable() returns true.
 */
char Connection::Read()
{
	#ifdef USE_ETHERNET
		if (ClientDataAvailable()) return _client.read();
		return '\0';
	#endif

	#ifdef USE_XBEE
		return Serial.read();
	#endif
}

size_t Connection::PrintLine(const char* string)
{
	#ifdef USE_ETHERNET
		if (ClientIsConnected())
		{
			return _client.println(string);
		}
		return (size_t)0;
	#endif

	#ifdef USE_XBEE
		return Serial.println((const char *)string);
	#endif
}

Connection::~Connection() {
	delete _server;
}

} /* namespace SARC */

#undef CONNECTION_CPP_
