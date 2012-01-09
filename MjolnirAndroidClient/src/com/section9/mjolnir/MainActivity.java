package com.section9.mjolnir;

import org.openintents.sensorsimulator.hardware.Sensor;
import org.openintents.sensorsimulator.hardware.SensorEvent;
import org.openintents.sensorsimulator.hardware.SensorEventListener;
import org.openintents.sensorsimulator.hardware.SensorManagerSimulator;

import com.section9.mjolnir.RobotModel.CameraCommands;

import android.app.Activity;
//import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

/**
 * 
 * @author Odysseus
 *
 */
public class MainActivity extends Activity {
	
	//private SensorManager mSensorManager;
	private SensorManagerSimulator mSensorManager;
    private SensorEventListener mSensorListener;
    private RobotModel mMjolnir;
    private MjpegView mMjolnirVideo;
    
    /**
     * Constructor. Creates a new MjolnirClientActivity object.
     */
    public MainActivity() {
        mMjolnir = new RobotModel();
    }

    /**
     * Called when the activity is first created.
     * 
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        // Init listener for sensor orientation events.
        mSensorListener = new SensorEventListener() {
			
			public void onSensorChanged(SensorEvent event) {
				if (Math.abs(event.values[2]) < 20 && Math.abs(event.values[1]) < 30) {
					// Accelerate.
					mMjolnir.setNavigationState(RobotModel.NavigationStates.Accelerate);
				} else if (Math.abs(event.values[2]) < 20 && Math.abs(event.values[1]) > 60) {
					// Decelerate.
					mMjolnir.setNavigationState(RobotModel.NavigationStates.Decelerate);
				} else if ((event.values[1] < -30 && event.values[1] > -60) && event.values[2] < -20) {
					// Turn left.
					mMjolnir.setNavigationState(RobotModel.NavigationStates.TurnLeft);
				} else if ((event.values[1] < -30 && event.values[1] > -60) && event.values[2] > 20) {
					// Turn right.
					mMjolnir.setNavigationState(RobotModel.NavigationStates.TurnRight);
				}
			}
			
			public void onAccuracyChanged(Sensor arg0, int arg1) {
				return; // Not implemented.
			}
		};
		
		// Init listener to process incoming robot data.
		mMjolnir.setDataReceivedListener(new RobotConnection.DataReceivedListener() {
			public void onDataReceived(byte[] data) {
				// Convert to string.
				final String s = new String(data);
				final EditText DebugConsole = (EditText)findViewById(R.id.DebugConsole); 
				DebugConsole.post(new Runnable() {
					public void run() {
						DebugConsole.append(s);
					}
				});
			}
		});
    }

    /**
     * Called when the activity is removed from the foreground.
     */
    @Override
    protected void onPause() {
    	super.onPause();
    	disconnect();
    }
    
    /**
     * Handler for the ToggleConnectionButton click event. Toggles the connection state.
     * 
     * @param view
     */
    public void ToggleConnectionButton_OnClick(View view) {
    	if (((Button)view).getText().toString().equals("Connect")) {
    		connect();
    	} else {
    		disconnect();    		
    	}
    }
	
    /**
     * Attempt to establish a Telnet connection to the Mjolnir robot.
     */
	private void connect() {
		// Attempt to open connection.
		try {
			mMjolnir.StartNavSubsystem();
		} catch (Exception e) {
			Toast.makeText(this, "Unable to start navigation subsystem.", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
			return;
		}
		
		// Update UI.
        ((Button)findViewById(R.id.ToggleConnectionButton)).setText("Disconnect");
		
        // Init orientation sensor.
        mSensorManager = SensorManagerSimulator.getSystemService(this, SENSOR_SERVICE);
        mSensorManager.connectSimulator();
        mSensorManager.registerListener(
        	mSensorListener,
        	mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
        	SensorManagerSimulator.SENSOR_DELAY_NORMAL
        );
        
        // Init video streaming.
        mMjolnirVideo = new MjpegView(this);
        mMjolnirVideo.setSource(MjpegInputStream.read("http://192.168.1.98/videostream.cgi?user=admin&pwd=section9"));
        mMjolnirVideo.setDisplayMode(MjpegView.SIZE_BEST_FIT);
        mMjolnirVideo.showFps(true);
        FrameLayout vf = (FrameLayout)findViewById(R.id.VideoFrame);
        vf.removeAllViewsInLayout();
        vf.addView(mMjolnirVideo);
        
        // Init gesture recognition.
 		vf.setOnTouchListener(new View.OnTouchListener() {
 			public boolean onTouch(View v, MotionEvent event) {
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
 		});
	}
	
	/**
	 * Disconnects from the Mjolnir robot.
	 */
	private void disconnect() {
		
		// Stop Video Streaming.
		if (mMjolnirVideo != null)
			mMjolnirVideo.stopPlayback();
		
		// Unregister sensor listener.
		if (mSensorManager != null)
			mSensorManager.unregisterListener(mSensorListener);
		
		// Terminate robot connection.
		mMjolnir.StopNavSubsystem();
		
		// Reset UI.
		((Button)findViewById(R.id.ToggleConnectionButton)).setText("Connect");
		
		// Disable gesture recognition.
		FrameLayout vf = (FrameLayout)findViewById(R.id.VideoFrame);
		vf.setOnTouchListener(null);
	}
}