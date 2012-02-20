package com.section9.mjolnir;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * 
 * @author tschoonover
 *
 */
public class RobotConnection {
	
	private Socket mRemoteSocket;
	private DataReceivedListener mDataReceivedListener;
	private InputStream mInStream;
	private OutputStream mOutStream;
	private Thread ListenerThread;
	
	/**
	 * 
	 * @param address
	 * @param port
	 * @throws IOException
	 */
	public void connect(String address, int port) throws IOException {
		// Attempt to open connection.
		mRemoteSocket = new Socket(address, port);
		
		// Init input/output streams for use by threads.
		mInStream = mRemoteSocket.getInputStream();
		mOutStream = mRemoteSocket.getOutputStream();
		
		// Spawn worker thread to listen for incoming data.
		ListenerThread = new Thread(new Runnable() {
			public void run() {
				int readCount = -1;
				byte[] buffer = new byte[4096];
				do {
					try {
						readCount = mInStream.read(buffer);
						synchronized(this) {
							if (readCount != -1 && mDataReceivedListener != null) {
								byte[] data = new byte[readCount];
								System.arraycopy(buffer, 0, data, 0, readCount);
								mDataReceivedListener.onDataReceived(data);
							}
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				} while (readCount != -1);
			};
		});
		ListenerThread.start();
	}
	
	/**
	 * 
	 * @param data
	 * @throws Exception
	 */
	public void send(byte[] data) throws Exception {
		if (!mRemoteSocket.isConnected())
			throw new Exception("Not connected.");
		
		synchronized (mOutStream) {
			mOutStream.write(data);
		}
	}
	
	/**
	 * 
	 * @param data
	 * @throws Exception
	 */
	public void sendAsync(final byte[] data) throws Exception {
		if (!mRemoteSocket.isConnected())
			throw new Exception("Not connected.");
		
		// Spawn worker thread to send the data.
		Thread t = new Thread(new Runnable() {
			public void run() {
				try {
					synchronized (mOutStream) {
						mOutStream.write(data);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		t.start();
	}
	
	/**
	 * 
	 */
	public void disconnect() {
		// Attempt to close the socket.
		try {
			if (mRemoteSocket != null) {
				mRemoteSocket.shutdownInput();
				mRemoteSocket.shutdownOutput();
				mRemoteSocket.close();
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		// Wait for the listener thread to terminate.
		try {
			if (ListenerThread != null)
				ListenerThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param listener
	 */
	public synchronized void setDataReceivedListener(DataReceivedListener listener)	{
		mDataReceivedListener = listener;
	}

	/**
	 * 
	 * @author Odysseus
	 *
	 */
	public interface DataReceivedListener {
		public void onDataReceived(byte[] data);
	}
}
