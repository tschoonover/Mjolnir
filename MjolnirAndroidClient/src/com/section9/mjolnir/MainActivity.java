package com.section9.mjolnir;

import com.section9.mjolnir.RobotModel.CameraCommands;
import com.section9.mjolnir.RobotModel.NavigationStates;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
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
    private MjpegView mMjolnirVideo;
    
    /**
     * Constructor. Creates a new MainActivity object.
     */
    public MainActivity()
    {
        mMjolnir = new RobotModel();
    }

    /**
     * Attempt to establish a TCP/IP connection to the Mjolnir robot.
     */
	private void connect()
	{
		// Attempt to open connection.
		try {
			mMjolnir.setVideoFrameReceivedListener(new VideoFrameReceivedListener());
			mMjolnir.StartVideoSubsystem();
			
			//mMjolnir.StartNavSubsystem();
		} catch (Exception e) {
			Toast.makeText(this, "Unable to establish connection.", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
			return;
		}

        // Init touch handling for the VideoFrame.
 		((ImageView)findViewById(R.id.VideoFrame)).setOnTouchListener(new VideoFrameTouchListener());
 		
 		// Init touch handling for the DPad.
 		ImageView dpad = (ImageView)findViewById(R.id.DPadImage);
 		dpad.setOnTouchListener(new DPadTouchListener());
 		
 		// Enable Toggle Buttons.
 		((ToggleButton)findViewById(R.id.ToggleHQButton)).setEnabled(true);
 		((ToggleButton)findViewById(R.id.TogglePatrolButton)).setEnabled(true);
 		((ToggleButton)findViewById(R.id.ToggleIRButton)).setEnabled(true);
 		((ToggleButton)findViewById(R.id.ToggleDebugButton)).setEnabled(true);
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
        
		// Init listener to process incoming robot data.
		mMjolnir.setDataReceivedListener(new RobotConnection.DataReceivedListener() {
			public void onDataReceived(byte[] data) {
				// Convert to string.
				final String s = new String(data);
//				final EditText DebugConsole = (EditText)findViewById(R.id.DebugConsole); 
//				DebugConsole.post(new Runnable() {
//					public void run() {
//						DebugConsole.append(s);
//					}
//				});
			}
		});
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
    	if (((ToggleButton)view).isChecked()) {
    		mMjolnir.ExecuteCameraCommand(CameraCommands.ActivateHQ);
    	} else {
    		mMjolnir.ExecuteCameraCommand(CameraCommands.DeactivateHQ);
    	}
    }
    
    /**
     * Handler for the TogglePatrolButton click event. Toggles the camera patrol mode.
     * 
     * @param view
     */
    public void TogglePatrolButton_OnClick(View view)
    {
    	if (((ToggleButton)view).isChecked()) {
    		mMjolnir.ExecuteCameraCommand(CameraCommands.ActivateHPatrol);
    		mMjolnir.ExecuteCameraCommand(CameraCommands.ActivateVPatrol);
    	} else {
    		mMjolnir.ExecuteCameraCommand(CameraCommands.DeactivateHPatrol);
    		mMjolnir.ExecuteCameraCommand(CameraCommands.DeactivateVPatrol);
    	}
    }
    
    /**
     * Handler for the ToggleIRButton click event. Toggles the Infrared LED's.
     * 
     * @param view
     */
    public void ToggleIRButton_OnClick(View view)
    {
    	if (((ToggleButton)view).isChecked()) {
    		mMjolnir.ExecuteCameraCommand(CameraCommands.ActivateIR);
    	} else {
    		mMjolnir.ExecuteCameraCommand(CameraCommands.DeactivateIR);
    	}
    }
    
    /**
     * Handler for the ToggleDebugButton click event. Toggles the display of debug output.
     * 
     * @param view
     */
    public void ToggleDebugButton_OnClick(View view)
    {
    }

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
	
	private class VideoFrameTouchListener implements View.OnTouchListener
	{
		public boolean onTouch(View v, MotionEvent event)
		{
			if (event.getAction() == MotionEvent.ACTION_DOWN)
			{
				FrameLayout vf = (FrameLayout)findViewById(R.id.VideoFrame);
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
			return true;
		}
	}
	
	private class VideoFrameReceivedListener implements MjpegDecodingListener.VideoFrameReceivedListener {
		public void onVideoFrameReceived(final Bitmap bm) {
			MainActivity.this.runOnUiThread(new Runnable() {
				public void run() {
					ImageView videoFrame = (ImageView)findViewById(R.id.VideoFrame);
					videoFrame.setImageBitmap(bm);
				}
			});
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