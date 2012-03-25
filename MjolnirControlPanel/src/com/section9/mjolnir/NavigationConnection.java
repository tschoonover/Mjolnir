package com.section9.mjolnir;

/**
 * Used to establish a socket connection to the Mjolnir robot navigation subsystem. Once a
 * connection has been established, outgoing messages to the subsystem can be transmitted using
 * one of the base Send, SendAsync or SendAsyncAndClose methods. Incoming messages from the subsystem
 * are saved to a FIFO history queue. Use the getHistory method to get an array of the most recent
 * messages.
 * 
 * @author Odysseus
 */
public class NavigationConnection extends RobotConnectionBase
{
	private final int BUFFER_SIZE = 5;
	private String[] mHistory = new String[BUFFER_SIZE];
	
	/**
	 * Processes all incoming connection data from the navigation subsystem. Each
	 * incoming chunk of data corresponds to a message from the navigation subsystem.
	 * Each message is added as a string to the beginning of a fixed length history
	 * array. All previous messages are shifted toward the end of the array except
	 *  the oldest which is discarded.
	 */
	@Override
	protected void onDataReceived(byte[] data, int numBytes)
	{
		synchronized(this)
		{
			for (int i = 0; i < BUFFER_SIZE - 1; i++)
				mHistory[i] = mHistory[i + 1];
			
			mHistory[BUFFER_SIZE - 1] = new String(data, 0, numBytes);
		}
	}

	/**
	 * Returns an array of the most recent messages from the navigation subsystem.
	 * @return An array of the most recent messages from the navigation subsystem.
	 */
	public synchronized String[] getHistory()
	{
		return mHistory.clone();
	}
}