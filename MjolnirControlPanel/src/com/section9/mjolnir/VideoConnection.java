package com.section9.mjolnir;

import java.io.ByteArrayInputStream;
import java.util.Properties;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.Config;

/**
 * Used to establish a socket connection to the Mjolnir robot video subsystem. Once a
 * connection has been established, outgoing messages to the subsystem can be transmitted using
 * one of the base Send, SendAsync or SendAsyncAndClose methods. Incoming messages from the subsystem
 * are saved to a buffer which is then parsed for Mjpeg content. Use the setVideoFrameReceived method
 * register for new video frame notifications.
 * messages.
 * 
 * @author Odysseus
 */
public class VideoConnection extends RobotConnectionBase
{
	private final int CAM_MAX_FRAME_SIZE = 1024 * 1024;
	private final byte[] CAM_SOI_MARKER = { (byte) 0xFF, (byte) 0xD8 };

	private VideoFrameReceivedListener mVideoFrameReceivedListener;
	private byte[] buffer = new byte[CAM_MAX_FRAME_SIZE];
	int bufferIndex;
	int SOIPosition;
	int imageSize;
	
	/**
	 * Closes the connection to the video subsystem, waits for the input listener thread to terminate and clears the buffer.
	 */
	@Override
	public void disconnect()
	{
		super.disconnect();
		resetBuffer();
	}
	
	/**
	 * Search the byte array data for the first occurrence of the bytes specified in sequence. If sequence
	 * is found, returns it's starting position. If sequence is not found, returns -1. 
	 * @param data - The byte array to search.
	 * @param sequence - The byte sequence to find.
	 * @return The position of the first occurrence of sequence in data or -1 if not found.
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
	 * Processes all incoming data from the video subsystem. All incoming data is appended to the end of a buffer
	 * if there is room in the buffer for it. If there is not room, the buffer is reset. The buffer is then
	 * processed for Mjpeg content. If a full Mjpeg frame is found it is decoded into a Bitmap object and passed
	 * to the onVideoFrameReceived listener (if one has been set). 
	 */
	@Override
	protected void onDataReceived(byte[] data, int numBytes)
	{
		// Abort if incoming video data too large for buffer.
		if (buffer.length < numBytes)
		{
			resetBuffer();
			return;
		}
		
		// Reset buffer if there is not enough room to append video data.
		if (bufferIndex + numBytes > buffer.length)
			resetBuffer();
		
		// Append video data to the end of buffer.
		System.arraycopy(data, 0, buffer, bufferIndex, numBytes);
		bufferIndex += numBytes;
		
		// Attempt to decode a video frame from the buffer.
		TryDecodeBuffer();
	}
	
	/**
	 * Parses the image size value from an Mjpeg header.
	 * @param header - The Mjpeg header to parse as a byte array.
	 * @return The image size.
	 * @throws Exception Unable to parse header.
	 */
    private int parseImageSize(byte[] header) throws Exception
    {
        ByteArrayInputStream headerStream = new ByteArrayInputStream(header);
        Properties props = new Properties();
        props.load(headerStream);
        return Integer.parseInt(props.getProperty("Content-Length"));
    }	

    /**
     * Resets the incoming data buffer.
     */
	private void resetBuffer()
	{
		imageSize = 0;
		bufferIndex = 0;
		buffer = new byte[CAM_MAX_FRAME_SIZE];
	}

	/**
	 * Registers a listener for new video frame notifications.
	 * @param listener
	 */
	public synchronized void setVideoFrameReceivedListener(VideoFrameReceivedListener listener)
	{
		mVideoFrameReceivedListener = listener;
	}
	
	/**
	 * Parses the incoming data buffer for Mjpeg content. Passes all Mjpeg frames found in the buffer to
	 * the VideoFrameReceived listener (if one has been set). 
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
			
			// If buffer contains all of the bytes for the next image...
			if (SOIPosition + imageSize <= bufferIndex)
			{
				// Decode image bytes into a mutable bitmap.
				Bitmap bm = BitmapFactory.decodeByteArray(buffer, SOIPosition, imageSize).copy(Config.ARGB_8888, true);
				
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
				imageSize = 0;
			}
			else
			{
				// Buffer does not contain enough data so break out of loop and wait for more data.
				return;
			}
		}
	}

	/**
	 * Interface to register for VideoFrameReceived notifications. 
	 * @author Odysseus
	 */
	public interface VideoFrameReceivedListener
	{
		public void onVideoFrameReceived(Bitmap bm);
	}
}
