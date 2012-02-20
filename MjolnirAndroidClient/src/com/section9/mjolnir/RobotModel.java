package com.section9.mjolnir;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

public class RobotModel {
	
	// Constant definitions
	private final String CAM_BASE_URL = "http://192.168.1.98/";
	private final String CAM_AUTH_PARMS = "&user=admin&pwd=section9";
	private final String NAV_IP = "192.168.1.99";
	private final int NAV_PORT = 23;
	
	// Class members
	private RobotConnection mNavConn;
	private Timer mNavUpdateTimer;
	private TimerTask mUpdateNavStateTask; 
	private NavigationStates mNavigationState;
	private boolean mCameraHQ;
	private boolean mCameraIR;
	private CameraMotionStates mCameraMotionState;
	private HttpClient mHttpclient;
	
	/**
	 * Constructor
	 */
	public RobotModel() {
		initNavigation();
		mHttpclient = new DefaultHttpClient();
	}
	
	public boolean getCameraHQ()
	{
		return mCameraHQ;
	}
	
	public boolean getCameraIR()
	{
		return mCameraIR;
	}
	
	public CameraMotionStates getCameraMotionState()
	{
		return mCameraMotionState;
	}
	
	public synchronized NavigationStates getNavigationState() {
		return mNavigationState;
	}
	
	public synchronized void setNavigationState(NavigationStates state) {
		mNavigationState = state;
	}
	
	public void setDataReceivedListener(RobotConnection.DataReceivedListener listener) {
		mNavConn.setDataReceivedListener(listener);
	}
	
	private void initNavigation() {
		
		// Create connection object.
		mNavConn = new RobotConnection();
		
		// Create timer for triggering re-occurring navigation updates.
		mNavUpdateTimer = new Timer();
	}

	public void StartNavSubsystem() throws IOException {
		// Connect to subsystem.
		mNavConn.connect(NAV_IP, NAV_PORT);
		
		// Set initial navigation state.
		setNavigationState(NavigationStates.Maintain);
		
		// Schedule re-occurring navigation updates.
		mUpdateNavStateTask = new UpdateNavStateTask();
		mNavUpdateTimer.schedule(mUpdateNavStateTask, 0, 500);
	}
	
	public void StopNavSubsystem() {
		// Halt navigation transmissions.
		if (mUpdateNavStateTask != null)
			mUpdateNavStateTask.cancel();
		
		// Disconnect from subsystem.
		mNavConn.disconnect();	
	}
	
	public void ExecuteCameraCommand(CameraCommands cmd) {
		
		// Build the URL command and update state.
		String URL = CAM_BASE_URL;
		
		// Append the camera function and parameters values.
		switch (cmd)
		{
			case ActivateIR:
				mCameraIR = true;
				URL += "decoder_control.cgi?command=95";
				break;
				
			case DeactivateIR:
				mCameraIR = false;
				URL += "decoder_control.cgi?command=94";
				break;
				
			case ActivateVPatrol:
				if (mCameraMotionState == CameraMotionStates.PatrollingHorizontally || mCameraMotionState == CameraMotionStates.PatrollingHorizontallyAndVertically)
					mCameraMotionState = CameraMotionStates.PatrollingHorizontallyAndVertically;
				else
					mCameraMotionState = CameraMotionStates.PatrollingVertically;
				URL += "decoder_control.cgi?command=26";
				break;
				
			case DeactivateVPatrol:
				if (mCameraMotionState == CameraMotionStates.PatrollingHorizontallyAndVertically || mCameraMotionState == CameraMotionStates.PatrollingHorizontally)
					mCameraMotionState = CameraMotionStates.PatrollingHorizontally;
				else
					mCameraMotionState = CameraMotionStates.NotMoving;
				URL += "decoder_control.cgi?command=27";
				break;
				
			case ActivateHPatrol:
				if (mCameraMotionState == CameraMotionStates.PatrollingVertically || mCameraMotionState == CameraMotionStates.PatrollingHorizontallyAndVertically)
					mCameraMotionState = CameraMotionStates.PatrollingHorizontallyAndVertically;
				else
					mCameraMotionState = CameraMotionStates.PatrollingHorizontally;
				URL += "decoder_control.cgi?command=28";
				break;
				
			case DeactivateHPatrol:
				if (mCameraMotionState == CameraMotionStates.PatrollingHorizontallyAndVertically || mCameraMotionState == CameraMotionStates.PatrollingVertically)
					mCameraMotionState = CameraMotionStates.PatrollingVertically;
				else
					mCameraMotionState = CameraMotionStates.NotMoving;
				URL += "decoder_control.cgi?command=29";
				break;
				
			case PanRight: // Note Left and right are reversed on the device.
				mCameraMotionState = CameraMotionStates.PanningRight;
				URL += "decoder_control.cgi?command=4";
				break;
				
			case StopLeft: // Note Left and right are reversed on the device.
				mCameraMotionState = CameraMotionStates.NotMoving;
				URL += "decoder_control.cgi?command=5";
				break;
				
			case PanLeft: // Note Left and right are reversed on the device.
				mCameraMotionState = CameraMotionStates.PanningLeft;
				URL += "decoder_control.cgi?command=6";
				break;
				
			case StopRight: // Note Left and right are reversed on the device.
				mCameraMotionState = CameraMotionStates.NotMoving;
				URL += "decoder_control.cgi?command=7";
				break;
				
			case PanUp:
				mCameraMotionState = CameraMotionStates.PanningUp;
				URL += "decoder_control.cgi?command=0";
				break;
				
			case StopUp:
				mCameraMotionState = CameraMotionStates.NotMoving;
				URL += "decoder_control.cgi?command=1";
				break;
				
			case PanDown:
				mCameraMotionState = CameraMotionStates.PanningDown;
				URL += "decoder_control.cgi?command=2";
				break;
				
			case StopDown:
				mCameraMotionState = CameraMotionStates.NotMoving;
				URL += "decoder_control.cgi?command=3";
				break;
				
			case ActivateHQ:
				mCameraHQ = true;
				URL += "camera_control.cgi?param=0&value=32";
				break;
				
			case DeactivateHQ:
				mCameraHQ = false;
				URL += "camera_control.cgi?param=0&value=8";
				break;
				
			default:
				return;
		}
		
		// Append the authentication parameters.
		URL += CAM_AUTH_PARMS;
		
		// Execute the HTTP command.
		try {
			mHttpclient.execute(new HttpGet(URL));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public enum NavigationStates {
		Maintain,
		Accelerate,
		Decelerate,
		TurnLeft,
		TurnRight,
		Stop
    }
	
	public enum CameraMotionStates {
		NotMoving,
		PanningUp,
		PanningDown,
		PanningLeft,
		PanningRight,
		PatrollingHorizontally,
		PatrollingVertically,
		PatrollingHorizontallyAndVertically
	}
	
	public enum CameraCommands {
		ActivateIR,
		DeactivateIR,
		ActivateVPatrol,
		DeactivateVPatrol,
		ActivateHPatrol,
		DeactivateHPatrol,
		PanLeft,
		StopLeft,
		PanRight,
		StopRight,
		PanUp,
		StopUp,
		PanDown,
		StopDown,
		ActivateHQ,
		DeactivateHQ;
	}
	
	private class UpdateNavStateTask extends TimerTask {
		@Override
		public void run() {
			// Attempt to transmit navigation state.
			try {
				switch (getNavigationState()) {
					case Accelerate:
						mNavConn.send(new byte[] {'w'});
						break;
					case Decelerate:
						mNavConn.send(new byte[] {'s'});
						break;
					case TurnLeft:
						mNavConn.send(new byte[] {'a'});
						break;
					case TurnRight:
						mNavConn.send(new byte[] {'d'});
						break;
					case Stop:
						mNavConn.send(new byte[] {'q'});
						break;
					default:
						mNavConn.send(new byte[] {'m'});
						break;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
