package com.section9.mjolnir;

public class TextBufferingListener implements RobotConnection.DataReceivedListener
{
	private final int BUFFER_SIZE = 5;
	private String[] mText = new String[BUFFER_SIZE];
	
	public synchronized String[] getText()
	{
		return mText.clone();
	}
	
	public void onDataReceived(byte[] data)
	{
		synchronized (this)
		{
			for (int i = 0; i < BUFFER_SIZE - 1; i++)
				mText[i] = mText[i+1];
			
			mText[BUFFER_SIZE - 1] = new String(data);
		}
	}

}
