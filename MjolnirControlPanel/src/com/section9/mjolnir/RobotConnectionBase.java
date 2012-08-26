/*  
 *  This file is part of the Mjolnir Control Panel (MCP) program,
 *  a client application for controlling the Mjolnir robot.
 *  Copyright (C) 2012 Section9
 *
 *  MCP is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  MCP is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.section9.mjolnir;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Base class for establishing connections to various IP based robot subsystems. Encapsulates the process of connecting
 * and disconnecting, transmitting data and monitoring for responses. Subclasses must implement the onDataReceived method
 * in order to process incoming data in whatever way is appropriate for the connection type.
 * s
 * @author Odysseus
 */
public abstract class RobotConnectionBase
{
	private Socket mRemoteSocket;
	private InputStream mInStream;
	private OutputStream mOutStream;
	private Thread mListenerThread;
	
	/**
	 * Spawns a worker thread that opens a connection to the specified address and port, transmits the specified data
	 * and then closes the connection. Does not monitor connection for response. Use for fire and forget type messages.
	 * @param address - The IP address or DNS name of the connection target.
	 * @param port - The port address to use for the connection.
	 * @param data - The data to be transmitted.
	 */
	public static void sendAsyncAndClose(final String address, final int port, final byte[] data)
	{
		Thread t = new Thread(new Runnable()
		{
			public void run()
			{
				try
				{
					Socket s = new Socket(address, port);
					s.getOutputStream().write(data);
					s.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		t.start();
	}

	/**
	 * Invoked by the connection listener each time data is read. The specific implementation should process the
	 * incoming data in whatever way is appropriate for the connection type. Must be implemented by all subclasses.
	 * 
	 * @param data - The data read from the connection.
	 */
	protected abstract void onDataReceived(byte[] data, int byteCount);
	
	/**
	 * Establishes a persistent connection to the specified address and port. Spawns a listener thread to monitor
	 * the connection for incoming data. All incoming data is passed to the onDataReceived method for processing.
	 * @param address - The IP address or DNS name of the connection target.
	 * @param port - The port addresss to use for the connection.
	 * @throws Exception Unable to establish connection.
	 */
	public void connect(String address, int port) throws Exception
	{
		// Attempt to open connection.
		mRemoteSocket = new Socket(address, port);
		
		// Init input/output streams for use by threads.
		mInStream = mRemoteSocket.getInputStream();
		mOutStream = mRemoteSocket.getOutputStream();
		
		// Spawn worker thread to listen for incoming data.
		mListenerThread = new Thread(new Runnable()
		{
			public void run()
			{
				int byteCount = -1;
				byte[] buffer = new byte[8192];
				do
				{
					try
					{
						byteCount = mInStream.read(buffer);
						if (byteCount != -1)
							onDataReceived(buffer, byteCount);
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
				} while (byteCount != -1);
			};
		});
		mListenerThread.start();
	}
	
	/**
	 * Synchronously transmits the specified data. Use when blocking will not produce adverse behavior.
	 * @param data - The data to transmit.
	 * @throws Exception Not connected or unabled to transmit data.
	 */
	public void send(byte[] data) throws Exception
	{
		if (!mRemoteSocket.isConnected())
			throw new Exception("Not connected.");
		
		synchronized (mOutStream)
		{
			mOutStream.write(data);
		}
	}
	
	/**
	 * Asynchronously transmits the specified data. Use when blocking will produce adverse behavior.
	 * @param data - The data to transmit.
	 * @throws Exception Not connected.
	 */
	public void sendAsync(final byte[] data) throws Exception
	{
		if (!mRemoteSocket.isConnected())
			throw new Exception("Not connected.");
		
		// Spawn worker thread to send the data.
		Thread t = new Thread(new Runnable()
		{
			public void run()
			{
				try
				{
					synchronized (mOutStream)
					{
						mOutStream.write(data);
					}
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		});
		t.start();
	}
	
	/**
	 * Terminates the connection listener thread and closes the connection.
	 */
	public void disconnect()
	{
		// Attempt to close the socket.
		try
		{
			if (mRemoteSocket != null)
			{
				mRemoteSocket.shutdownInput();
				mRemoteSocket.shutdownOutput();
				mRemoteSocket.close();
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		// Wait for the listener thread to terminate.
		try
		{
			if (mListenerThread != null)
				mListenerThread.join();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}
}