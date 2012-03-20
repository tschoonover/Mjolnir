package com.section9.mjolnir;

import com.section9.mjolnir.RobotModel.CameraCommands;
import com.section9.mjolnir.RobotModel.NavigationStates;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ToggleButton;

/**
 * 
 * @author Odysseus
 *
 */
public class MainActivity extends Activity
{
    private RobotModel mMjolnir;
    private boolean debug;
    private final String NAV_IP = "192.168.1.99";
	private final int NAV_PORT = 23;
	private final String VID_IP = "192.168.1.98";
	private final int VID_PORT = 80;
	
    /**
     * Constructor. Creates a new MainActivity object.
     */
    public MainActivity()
    {
        mMjolnir = new RobotModel();
    }

    /**
     * Attempts to connect to the Mjolnir robot.
     */
	private void connect()
	{
		// Configure a TextBufferingListener to process the navigation subsystem data.
		TextBufferingListener navListener = new TextBufferingListener();
		mMjolnir.setNavSubsystemListener(navListener);
		
		// Configure an MjpegDecodingListener to process the video subsystem data.
		MjpegDecodingListener vidListener = new MjpegDecodingListener();
		vidListener.setVideoFrameReceivedListener(new VideoFrameReceivedListener(navListener));
		mMjolnir.setVideoSubsystemListener(vidListener);
		
		// Attempt to start the video subsystem connection.
		try
		{
			// Start the video subsystem connection.
			mMjolnir.StartVideoSubsystem(VID_IP, VID_PORT);

	        // Initialize touch handling for the VideoFrame.
	 		findViewById(R.id.VideoFrame).setOnTouchListener(new VideoFrameTouchListener());
	 		
		}
		catch (Exception e)
		{
			Toast.makeText(this, "Unable to initialize video subsystem.", Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}

		// Attempt to initialize navigation subsystem.
		try
		{
			// Start the navigation subsystem connection.
			mMjolnir.StartNavSubsystem(NAV_IP, NAV_PORT);
			
	 		// Initialize touch handling for the DPad.
	 		findViewById(R.id.DPadImage).setOnTouchListener(new DPadTouchListener());
	 		
	 		// Enable Toggle Buttons.
	 		((ToggleButton)findViewById(R.id.ToggleHQButton)).setEnabled(true);
	 		((ToggleButton)findViewById(R.id.TogglePatrolButton)).setEnabled(true);
	 		((ToggleButton)findViewById(R.id.ToggleIRButton)).setEnabled(true);
	 		((ToggleButton)findViewById(R.id.ToggleDebugButton)).setEnabled(true);
		}
		catch (Exception e)
		{
			Toast.makeText(this, "Unable to initialize the navigation subsystem.", Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}
	}
	
	/**
	 * Disconnects from the Mjolnir robot.
	 */
	private void disconnect()
	{
 		// Disable toggle buttons.
 		((ToggleButton)findViewById(R.id.ToggleHQButton)).setEnabled(false);
 		((ToggleButton)findViewById(R.id.TogglePatrolButton)).setEnabled(false);
 		((ToggleButton)findViewById(R.id.ToggleIRButton)).setEnabled(false);
 		((ToggleButton)findViewById(R.id.ToggleDebugButton)).setEnabled(false);
 		
 		// Reset toggle button states.
 		((ToggleButton)findViewById(R.id.ToggleHQButton)).setChecked(false);
 		((ToggleButton)findViewById(R.id.TogglePatrolButton)).setChecked(false);
 		((ToggleButton)findViewById(R.id.ToggleIRButton)).setChecked(false);
 		((ToggleButton)findViewById(R.id.ToggleDebugButton)).setChecked(false);
 		
 		// Disable touch handling for direction pad.
 		ImageView dpad = (ImageView)findViewById(R.id.DPadImage);
 		dpad.setOnTouchListener(null);
 		
 		// Disable touch handling for the video frame.
 		ImageView vf = (ImageView)findViewById(R.id.VideoFrame);
 		vf.setOnTouchListener(null);
 		vf.setImageBitmap(null);
 		
		// Terminate robot connection.
		mMjolnir.StopNavSubsystem();
		mMjolnir.StopVideoSubsystem();
	}
	
    /**
     * Called when the activity is first created.
     * 
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }

    /**
     * Called when the activity is removed from the foreground.
     */
    @Override
    protected void onPause()
    {
    	super.onPause();
    	disconnect();
    }
    
    /**
     * Handler for the ToggleConnectionButton click event. Toggles the connection state.
     * 
     * @param view
     */
    public void ToggleConnectionButton_OnClick(View view)
    {
    	if (((ToggleButton)view).isChecked()) {
    		connect();
    	} else {
    		disconnect();    		
    	}
    }

    /**
     * Handler for the ToggleHQButton click event. Toggles between high quality
     * and low quality video streaming.
     * 
     * @param view
     */
    public void ToggleHQButton_OnClick(View view)
    {
    	try
    	{
    		if (((ToggleButton)view).isChecked()) {
        		mMjolnir.ExecuteCameraCommand(CameraCommands.ActivateHQ);
        	} else {
        		mMjolnir.ExecuteCameraCommand(CameraCommands.DeactivateHQ);
        	}	
    	}
    	catch (Exception e)
		{
			Toast.makeText(this, "Unable to toggle HQ.", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
    	
    }
    
    /**
     * Handler for the TogglePatrolButton click event. Toggles the camera patrol mode.
     * 
     * @param view
     */
    public void TogglePatrolButton_OnClick(View view)
    {
    	try
    	{
    		if (((ToggleButton)view).isChecked()) {
	    		mMjolnir.ExecuteCameraCommand(CameraCommands.ActivateHPatrol);
	    		mMjolnir.ExecuteCameraCommand(CameraCommands.ActivateVPatrol);
	    	} else {
	    		mMjolnir.ExecuteCameraCommand(CameraCommands.DeactivateHPatrol);
	    		mMjolnir.ExecuteCameraCommand(CameraCommands.DeactivateVPatrol);
	    	}
    	}
    	catch (Exception e)
		{
			Toast.makeText(this, "Unable to toggle Patrol.", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
    	
    }
    
    /**
     * Handler for the ToggleIRButton click event. Toggles the Infrared LED's.
     * 
     * @param view
     */
    public void ToggleIRButton_OnClick(View view)
    {
    	try
    	{
	    	if (((ToggleButton)view).isChecked()) {
	    		mMjolnir.ExecuteCameraCommand(CameraCommands.ActivateIR);
	    	} else {
	    		mMjolnir.ExecuteCameraCommand(CameraCommands.DeactivateIR);
	    	}
    	}catch (Exception e)
		{
			Toast.makeText(this, "Unable to toggle IR.", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
    }
    
    /**
     * Handler for the ToggleDebugButton click event. Toggles the display of debug output.
     * 
     * @param view
     */
    public void ToggleDebugButton_OnClick(View view)
    {
    	debug = ((ToggleButton)view).isChecked();
    }

    /**
     * 
     * @author odysseus
     *
     */
	private class DPadTouchListener implements View.OnTouchListener
	{
		public boolean onTouch(View v, MotionEvent event)
		{
			switch (event.getAction())
			{
				case MotionEvent.ACTION_DOWN:
				case MotionEvent.ACTION_MOVE:
					updateNavigationState(getDPadPosition(event));
					break;
					
				case MotionEvent.ACTION_CANCEL:
				case MotionEvent.ACTION_UP:
					mMjolnir.setNavigationState(NavigationStates.Maintain);
			}
			return true;
		}
		
		private DPadPositions getDPadPosition(MotionEvent event)
		{
			ImageView dpad = (ImageView)findViewById(R.id.DPadImage);
			if (event.getY() < dpad.getHeight() * .33
					&& event.getX() > dpad.getWidth() * .33
					&& event.getX() < dpad.getWidth() * .67 )
			{
				return DPadPositions.Up;
			}
			else if (event.getY() > dpad.getHeight() * .67
					&& event.getX() > dpad.getWidth() * .33
					&& event.getX() < dpad.getWidth() * .67 )
			{
				return DPadPositions.Down;
			}
			else if (event.getX() < dpad.getWidth() * .33
					&& event.getY() > dpad.getHeight() * .33
					&& event.getY() < dpad.getHeight() * .67 )
			{
				return DPadPositions.Left;
			}
			else if (event.getX() > dpad.getWidth() * .67
					&& event.getY() > dpad.getHeight() * .33
					&& event.getY() < dpad.getHeight() * .67 )
			{
				return DPadPositions.Right;
			}
			else if (event.getX() > dpad.getWidth() * .33
					&& event.getX() < dpad.getWidth() * .67
					&& event.getY() > dpad.getHeight() * .33
					&& event.getY() < dpad.getHeight() * .67)
			{
				return DPadPositions.Stop;
			}
			else
			{
				return DPadPositions.Invalid;
			}
		}

		private void updateNavigationState(DPadPositions position)
		{
			switch (position)
			{
				case Up:
					mMjolnir.setNavigationState(NavigationStates.Accelerate);
					break;
				case Down:
					mMjolnir.setNavigationState(NavigationStates.Decelerate);
					break;
				case Left:
					mMjolnir.setNavigationState(NavigationStates.TurnLeft);
					break;
				case Right:
					mMjolnir.setNavigationState(NavigationStates.TurnRight);
					break;
				case Stop:
					mMjolnir.setNavigationState(NavigationStates.Stop);
					break;
				default:
					mMjolnir.setNavigationState(NavigationStates.Maintain);
			}
		}
	}
	
	/**
	 * 
	 * @author odysseus
	 *
	 */
	private class VideoFrameTouchListener implements View.OnTouchListener
	{
		public boolean onTouch(View v, MotionEvent event)
		{
			try
			{
				if (event.getAction() == MotionEvent.ACTION_DOWN)
				{
					ImageView vf = (ImageView)findViewById(R.id.VideoFrame);
					if (event.getY() < vf.getHeight() * .25
							&& event.getX() > vf.getWidth() * .25
							&& event.getX() < vf.getWidth() * .75 )
					{
						mMjolnir.ExecuteCameraCommand(CameraCommands.PanUp);
					}
					else if (event.getY() > vf.getHeight() * .75
							&& event.getX() > vf.getWidth() * .25
							&& event.getX() < vf.getWidth() * .75 )
					{
						mMjolnir.ExecuteCameraCommand(CameraCommands.PanDown);
					}
					else if (event.getX() < vf.getWidth() * .25
							&& event.getY() > vf.getHeight() * .25
							&& event.getY() < vf.getHeight() * .75 )
					{
						mMjolnir.ExecuteCameraCommand(CameraCommands.PanLeft);
					}
					else if (event.getX() > vf.getWidth() * .75
							&& event.getY() > vf.getHeight() * .25
							&& event.getY() < vf.getHeight() * .75 )
					{
						mMjolnir.ExecuteCameraCommand(CameraCommands.PanRight);
					}
				} else if (event.getAction() == MotionEvent.ACTION_UP)
				{
					// Stop motion.
					switch (mMjolnir.getCameraMotionState()) {
						case PanningRight:
							mMjolnir.ExecuteCameraCommand(CameraCommands.StopRight);
							break;
						case PanningLeft:
							mMjolnir.ExecuteCameraCommand(CameraCommands.StopLeft);
							break;
						case PanningUp:
							mMjolnir.ExecuteCameraCommand(CameraCommands.StopUp);
							break;
						case PanningDown:
							mMjolnir.ExecuteCameraCommand(CameraCommands.StopDown);
							break;
					}
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			
			return true;
		}
	}
	
	/**
	 * 
	 * @author odysseus
	 *
	 */
	private class VideoFrameReceivedListener implements MjpegDecodingListener.VideoFrameReceivedListener
	{
		private TextBufferingListener mTextBufferingListener; 
		private int FPS;
		private int frameCount;
		private long startTime;
		private boolean mPendingFrame;
		
		public VideoFrameReceivedListener(TextBufferingListener listener)
		{
			mTextBufferingListener = listener;
		}
		
		public void onVideoFrameReceived(final Bitmap bm)
		{
			// Abort if there is a pending frame.
			if (mPendingFrame)
				return;

			// Set the pending frame flag.
			mPendingFrame = true;
			
			if (debug)
			{
				// Overlay bitmap with debug info.
				drawDebugOverlay(bm);
			}

			MainActivity.this.runOnUiThread(new Runnable()
			{
				public void run()
				{
					((ImageView)findViewById(R.id.VideoFrame)).setImageBitmap(bm);
					mPendingFrame = false;
				}
			});
		}
		
		private void drawDebugOverlay(Bitmap bm)
		{
			// Update FPS.
			updateFPS();
			
			// 
			Bitmap overlay;
			Canvas c = new Canvas(bm);
			
			String[] text = mTextBufferingListener.getText();
			for (int i = 0; i < text.length; i++)
			{
				overlay = getTextOverlay(text[i], bm.getWidth());
				c.drawBitmap(overlay, 0, overlay.getHeight() * i + 2, null);
			}
			
			overlay = getTextOverlay("FPS (" + String.valueOf(FPS) + ")", bm.getWidth());
			c.drawBitmap(overlay, 0, bm.getHeight() - overlay.getHeight() - 2, null);
		}
		
		private Bitmap getTextOverlay(String text, int maxWidth)
		{
			if (text == null)
				text = "";
			
			// Strip all non-printable characters.
			text = text.replaceAll("[^\\x20-\\x7E]","");
			
			Paint p = new Paint();
	        Rect r = new Rect();
	        p.getTextBounds("|", 0, 1, r);
	        Bitmap bm = Bitmap.createBitmap(maxWidth, r.height(), Bitmap.Config.ARGB_8888);
	        Canvas c = new Canvas(bm);
	        p.setColor(Color.BLACK);
	        p.setAlpha(127);
	        c.drawRect(0, 0, bm.getWidth(), bm.getHeight(), p);
	        p.setColor(Color.GREEN);
	        p.setAlpha(255);
	        c.drawText(text, 1, (r.height() / 2) - ((p.ascent() + p.descent()) / 2) + 1, p);
	        return bm;        	 
	    }
		
		/**
		 * 
		 */
		private void updateFPS()
		{
			frameCount++;
			
			if (startTime == 0) startTime = System.currentTimeMillis();
			if (System.currentTimeMillis() - startTime > 1000)
			{
				FPS = frameCount;
				frameCount = 0;
				startTime = System.currentTimeMillis();
			}
		}
	}
	
	private enum DPadPositions
	{
		Up,
		Down,
		Left,
		Right,
		Stop,
		Invalid
	};
}