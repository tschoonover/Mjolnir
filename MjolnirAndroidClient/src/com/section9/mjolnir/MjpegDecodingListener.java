package com.section9.mjolnir;

import java.io.ByteArrayInputStream;
import java.util.Properties;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

class MjpegDecodingListener implements RobotConnection.DataReceivedListener
{
	private final int CAM_MAX_FRAME_SIZE = 1024 * 1024;
	private final byte[] CAM_SOI_MARKER = { (byte) 0xFF, (byte) 0xD8 };
	
	private VideoFrameReceivedListener mVideoFrameReceivedListener;
	private byte[] buffer = new byte[CAM_MAX_FRAME_SIZE];
	int bufferIndex;
	int SOIPosition;
	int imageSize;
	
	/**
	 * 
	 * @param data
	 */
	private void appendToBuffer(byte[] data)
	{
		System.arraycopy(data, 0, buffer, bufferIndex, data.length);
		bufferIndex += data.length;
	}

	/**
	 * 
	 * @param data
	 * @param sequence
	 * @return
	 */
	private int findPosition(byte[] data, byte[] sequence)
	{
		int index = 0;
		byte b;
		for (int position = 0; position < bufferIndex; position++)
		{
			b = data[position];
			if (b == sequence[index])
			{
				index++;
				if (index == sequence.length)
					return position - sequence.length + 1;
			}
			else
			{
				index = 0;
			}
		}
		return -1;
	}

	/**
	 * 
	 */
	public void onDataReceived(byte[] data)
	{
		// Abort if incoming video data too large for buffer.
		if (buffer.length < data.length)
		{
			resetBuffer();
			return;
		}
		
		// Reset buffer if there is not enough room to append video data.
		if (bufferIndex + data.length > buffer.length)
			resetBuffer();
		
		// Append video data to the end of buffer.
		appendToBuffer(data);
		
		// Attempt to decode a video frame from the buffer.
		TryDecodeBuffer();
	}

	/**
	 * 
	 * @param headerBytes
	 * @return
	 * @throws Exception
	 */
    private int parseImageSize(byte[] headerBytes) throws Exception
    {
        ByteArrayInputStream headerIn = new ByteArrayInputStream(headerBytes);
        Properties props = new Properties();
        props.load(headerIn);
        return Integer.parseInt(props.getProperty("Content-Length"));
    }	

    /**
     * 
     */
	private void resetBuffer()
	{
		bufferIndex = 0;
		buffer = new byte[CAM_MAX_FRAME_SIZE];
	}

	public synchronized void setVideoFrameReceivedListener(VideoFrameReceivedListener listener)
	{
		mVideoFrameReceivedListener = listener;
	}
	
	/**
	 * 
	 */
	private void TryDecodeBuffer()
	{
		// Process buffer as long as it contains SOI markers.
		while ((SOIPosition = findPosition(buffer, CAM_SOI_MARKER)) != -1)
		{
			// Parse image size from the header (the bytes preceding the SOI marker), unless previously parsed.
			if (imageSize == 0)
			{
				try
				{
					byte[] header = new byte[SOIPosition];
					System.arraycopy(buffer, 0, header, 0, SOIPosition);
					imageSize = parseImageSize(header);	
				}
				catch (Exception e)
				{
					// Malformed header, abort.
					resetBuffer();
					return;
				}
			}
			
			// If buffer contains all of the bytes for the current image...
			if (SOIPosition + imageSize <= bufferIndex)
			{
				// Decode image bytes into a bitmap.
				Bitmap bm = BitmapFactory.decodeByteArray(buffer, SOIPosition, imageSize);
				
				// If there is any trailing data after the last image byte, use it to reinitialize the buffer.
				int trailingDataStartPosition = SOIPosition + imageSize;
				int trailingDataSize = bufferIndex - trailingDataStartPosition;
				byte[] trailingData = new byte[CAM_MAX_FRAME_SIZE];
				System.arraycopy(buffer, trailingDataStartPosition, trailingData, 0, trailingDataSize);
				buffer = trailingData;
				bufferIndex = trailingDataSize;
				
				// Notify any listeners.
				synchronized(this)
				{
					if (mVideoFrameReceivedListener != null)
						mVideoFrameReceivedListener.onVideoFrameReceived(bm);
				}
				
				// Reset image size.
				imageSize = -1;
			}
			else
			{
				// Buffer does not contain enough data so break out of loop and wait for more data.
				return;
			}
		}
	}
	
	/**
	 * 
	 * @author Odysseus
	 *
	 */
	public interface VideoFrameReceivedListener
	{
		public void onVideoFrameReceived(Bitmap bm);
	}
}
