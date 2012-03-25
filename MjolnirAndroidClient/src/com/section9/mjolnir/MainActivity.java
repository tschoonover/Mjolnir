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
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ToggleButton;

/**
 * Main activity class for the Mjolnir remote control application. Provides UI controls and logic for
 * interacting with the Mjolnir robot. Please see http://section9.choamco.com/mjolnir for more information.
 * 
 * @author Odysseus
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
		// Initialize listener to process video frames from the video subsystem.
		mMjolnir.setVideoFrameReceivedHandler(new VideoFrameReceivedListener());
		
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
     * @param savedInstanceState - Not used.
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
     * @param view - The ToggleConnectionButton ToggleButton view.
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
     * @param view - The ToggleHQButton ToggleButton view.
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
     * @param view - The TogglePatrolButton ToggleButton view.
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
     * @param view - The ToggleIRButton ToggleButton view.
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
     * @param view - The ToggleDebugButton ToggleButton view.
     */
    public void ToggleDebugButton_OnClick(View view)
    {
    	debug = ((ToggleButton)view).isChecked();
    }

    /**
     * Touch event listener class for the DPadImage ImageView. Sets the navigation state of the
     * Mjolnir robot model object based on user touch input. Supports navigation operations
     * in the four cardinal directions, as well as stop and maintain.
     */
	private class DPadTouchListener implements View.OnTouchListener
	{
		/**
		 * Processes the touch motion event. Sets the navigation state of the Mjolnir
		 * robot model object according to the xy coordinate of the touch event. If the
		 * xy coordinate does not correspond to any of the direction pad image hotspots,
		 * navigation state will be set to the default of maintain.
		 * @param v - The view the touch event has been dispatched to.
		 * @param event - The MotionEvent object containing full information about the event.
		 * @return True if the listener has consumed the event, false otherwise. 
		 */
		public boolean onTouch(View v, MotionEvent event)
		{
			switch (event.getAction())
			{
				case MotionEvent.ACTION_DOWN:
				case MotionEvent.ACTION_MOVE:
					mMjolnir.setNavigationState(getNavigationState(event));
					break;
					
				case MotionEvent.ACTION_CANCEL:
				case MotionEvent.ACTION_UP:
					mMjolnir.setNavigationState(NavigationStates.Maintain);
			}
			return true;
		}
		
		/**
		 * Returns the robot navigation state corresponding to the specified motion event xy coordinates.
		 * @param event - The motion event to analyze.
		 * @return The robot navigation state corresponding to the specified motion event xy coordinates.
		 */
		private NavigationStates getNavigationState(MotionEvent event)
		{
			// Get a reference to the DPadImage ImageView.
			ImageView dpad = (ImageView)findViewById(R.id.DPadImage);
			
			// Top middle.
			if (event.getY() < dpad.getHeight() * .33
					&& event.getX() > dpad.getWidth() * .33
					&& event.getX() < dpad.getWidth() * .67 )
			{
				return NavigationStates.Accelerate;
			}
			// Bottom middle.
			else if (event.getY() > dpad.getHeight() * .67
					&& event.getX() > dpad.getWidth() * .33
					&& event.getX() < dpad.getWidth() * .67 )
			{
				return NavigationStates.Decelerate;
			}
			// Left middle.
			else if (event.getX() < dpad.getWidth() * .33
					&& event.getY() > dpad.getHeight() * .33
					&& event.getY() < dpad.getHeight() * .67 )
			{
				return NavigationStates.TurnLeft;
			}
			// Right middle.
			else if (event.getX() > dpad.getWidth() * .67
					&& event.getY() > dpad.getHeight() * .33
					&& event.getY() < dpad.getHeight() * .67 )
			{
				return NavigationStates.TurnRight;
			}
			// Center.
			else if (event.getX() > dpad.getWidth() * .33
					&& event.getX() < dpad.getWidth() * .67
					&& event.getY() > dpad.getHeight() * .33
					&& event.getY() < dpad.getHeight() * .67)
			{
				return NavigationStates.Stop;
			}
			else
				return NavigationStates.Maintain;
		}
	}
	
	/**
	 * Touch event listener class for the VideoFrame ImageView. Executes commands against the Mjolnir
	 * video subsystem according to user touch input. Supports panning in the four cardinal directions.
	 */
	private class VideoFrameTouchListener implements View.OnTouchListener
	{
		/**
		 * Processes the touch motion event. Analyzes the xy coordinates and event action of
		 * the motion event to determine which camera command to execute. Touching any of the edges
		 * of the VideoFrame ImageView will cause the video subsystem to pan in that direction for
		 * as long as the touch persists or maximum panning in that direction has occurred.
		 * @param v - The view the touch event has been dispatched to.
		 * @param event - The MotionEvent object containing full information about the event.
		 * @return True if the listener has consumed the event, false otherwise.
		 */
		public boolean onTouch(View v, MotionEvent event)
		{
			try
			{
				// Start panning.
				if (event.getAction() == MotionEvent.ACTION_DOWN)
				{
					// Get a reference to the video frame.
					ImageView vf = (ImageView)findViewById(R.id.VideoFrame);
					
					if (event.getY() < vf.getHeight() * .25
							&& event.getX() > vf.getWidth() * .25
							&& event.getX() < vf.getWidth() * .75 )
					{
						// Pan up if touch occurred in top 25% of video frame.
						mMjolnir.ExecuteCameraCommand(CameraCommands.PanUp);
					}
					else if (event.getY() > vf.getHeight() * .75
							&& event.getX() > vf.getWidth() * .25
							&& event.getX() < vf.getWidth() * .75 )
					{
						// Pan down if touch occurred in bottom 25% of video frame.
						mMjolnir.ExecuteCameraCommand(CameraCommands.PanDown);
					}
					else if (event.getX() < vf.getWidth() * .25
							&& event.getY() > vf.getHeight() * .25
							&& event.getY() < vf.getHeight() * .75 )
					{
						// Pan left if touch occurred in left 25% of video frame.
						mMjolnir.ExecuteCameraCommand(CameraCommands.PanLeft);
					}
					else if (event.getX() > vf.getWidth() * .75
							&& event.getY() > vf.getHeight() * .25
							&& event.getY() < vf.getHeight() * .75 )
					{
						// Pan right if touch occurred in right 25% of video frame.
						mMjolnir.ExecuteCameraCommand(CameraCommands.PanRight);
					}
				} else if (event.getAction() == MotionEvent.ACTION_UP)
				{
					// Stop panning.
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
	 * Video frame received event listener class for the Mjolnir video subsystem. Processes all video
	 * frames generated by the video subsystem and displays them in the VideoFrame ImageView.
	 */
	private class VideoFrameReceivedListener implements VideoConnection.VideoFrameReceivedListener
	{
		private int FPS;
		private int frameCount;
		private long startTime;
		private boolean mPendingFrame;

		/**
		 * Calculates the current frames per second measurement.
		 */
		private void calculateFPS()
		{
			if (startTime == 0) startTime = System.currentTimeMillis();
			if (System.currentTimeMillis() - startTime > 1000)
			{
				FPS = frameCount;
				frameCount = 0;
				startTime = System.currentTimeMillis();
			}
		}
		
		/**
		 * Draws a text overlay with semi-transparent background onto the specified bitmap. Overlay includes
		 * the most recent navigation subsystem messages and current FPS.
		 * @param bm - The bitmap to overlays.
		 */
		private void drawDebugOverlay(Bitmap bm)
		{
			// Initialize drawing objects.
			Bitmap overlay;
			Canvas c = new Canvas(bm);
			
			// Get the most recent navigation subsystem messages.
			String[] text = mMjolnir.getNavigationOutput();
			
			// Draw each navigation subsystem message.
			for (int i = 0; i < text.length; i++)
			{
				overlay = getTextOverlay(text[i], bm.getWidth());
				c.drawBitmap(overlay, 0, overlay.getHeight() * i, null);
			}
			
			// Draw the current FPS.
			overlay = getTextOverlay("FPS (" + String.valueOf(FPS) + ")", bm.getWidth());
			c.drawBitmap(overlay, 0, bm.getHeight() - overlay.getHeight(), null);
		}
		
		/**
		 * Returns a bitmap of the specified text drawn over a semi-transparent background.
		 * @param text - The text to include in the bitmap.
		 * @param width - The width of the bitmap.
		 * @return A bitmap of the specified text over an semi-transparent background.
		 */
		private Bitmap getTextOverlay(String text, int width)
		{
			// Assign default for null input.
			if (text == null)
				text = "";
			
			// Strip all non-printable characters.
			text = text.replaceAll("[^\\x20-\\x7E]","");
			
			// Create a paint object for drawing and measuring.
			Paint p = new Paint();
			
			// Create a rectangle object for storing the text width and height.
			Rect r = new Rect();
			
			// Measure the text width and height. Note that this actually measures the "|" character.
			// This is to ensure that all bitmaps produced by this method have a consistent height.
	        p.getTextBounds("|", 0, 1, r);
	        
	        // Create a bitmap of the specified width and the measured height.
	        Bitmap bm = Bitmap.createBitmap(width, r.height(), Bitmap.Config.ARGB_8888);
	        
	        // Get a canvas object for drawing into the bitmap.
	        Canvas c = new Canvas(bm);
	        
	        // Fill the canvas with a semi-transparent background. 
	        p.setColor(Color.BLACK);
	        p.setAlpha(127);
	        c.drawRect(0, 0, bm.getWidth(), bm.getHeight(), p);
	        
	        // Draw the text.
	        p.setColor(Color.GREEN);
	        p.setAlpha(255);
	        c.drawText(text, 1, (r.height() / 2) - ((p.ascent() + p.descent()) / 2) + 1, p);
	        
	        // Return the final bitmap. 
	        return bm;        	 
	    }

		/**
		 * Processes all video frames received from the Mjolnir video subsystem. Sets the received bitmap as
		 * the image source for the VideoFrame ImageView. If debug mode is on, draws a debug overlay onto
		 * the bitmap before assigning it to the VideoFrame. 
		 * @param bm - The bitmap received from the video subsystem.
		 */
		public void onVideoFrameReceived(final Bitmap bm)
		{
			// Abort if there is a pending frame.
			if (mPendingFrame)
				return;

			// Set the pending frame flag.
			mPendingFrame = true;
			
			// Increment the frame count and recalculate FPS.
			frameCount++;
			calculateFPS();
			
			if (debug)
			{
				// Overlay bitmap with debug info.
				drawDebugOverlay(bm);
			}

			// Set the image as the VideoFrame image source. Note that this must run on the UI thread.
			MainActivity.this.runOnUiThread(new Runnable()
			{
				public void run()
				{
					((ImageView)findViewById(R.id.VideoFrame)).setImageBitmap(bm);
					mPendingFrame = false;
				}
			});
		}
	}
}