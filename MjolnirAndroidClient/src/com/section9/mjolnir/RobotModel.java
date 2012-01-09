package com.section9.mjolnir;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

public class RobotModel {
	
	private RobotConnection mNavConn;
	private Timer mNavUpdateTimer;
	private TimerTask mUpdateNavStateTask; 
	private NavigationStates mNavigationState;
	private boolean CameraIR;
	private CameraMotionStates CameraMotionState;
	HttpClient httpclient;
	
	public RobotModel() {
		initNavigation();
		httpclient = new DefaultHttpClient();
	}
	
	public boolean getCameraIR()
	{
		return CameraIR;
	}
	
	public CameraMotionStates getCameraMotionState()
	{
		return CameraMotionState;
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
		mNavConn.connect("192.168.1.99", 23);
		
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
		String URL = "http://192.168.1.98/decoder_control.cgi?user=admin&pwd=section9&command=";
		switch (cmd)
		{
			case ActivateIR:
				CameraIR = true;
				URL += "95";
				break;
				
			case DeactivateIR:
				CameraIR = false;
				URL += "94";
				break;
				
			case ActivateVPatrol:
				if (CameraMotionState == CameraMotionStates.PatrollingHorizontally || CameraMotionState == CameraMotionStates.PatrollingHorizontallyAndVertically)
					CameraMotionState = CameraMotionStates.PatrollingHorizontallyAndVertically;
				else
					CameraMotionState = CameraMotionStates.PatrollingVertically;
				URL += "26";
				break;
				
			case DeactivateVPatrol:
				if (CameraMotionState == CameraMotionStates.PatrollingHorizontallyAndVertically || CameraMotionState == CameraMotionStates.PatrollingHorizontally)
					CameraMotionState = CameraMotionStates.PatrollingHorizontally;
				else
					CameraMotionState = CameraMotionStates.NotMoving;
				URL += "27";
				break;
				
			case ActivateHPatrol:
				if (CameraMotionState == CameraMotionStates.PatrollingVertically || CameraMotionState == CameraMotionStates.PatrollingHorizontallyAndVertically)
					CameraMotionState = CameraMotionStates.PatrollingHorizontallyAndVertically;
				else
					CameraMotionState = CameraMotionStates.PatrollingHorizontally;
				URL += "28";
				break;
				
			case DeactivateHPatrol:
				if (CameraMotionState == CameraMotionStates.PatrollingHorizontallyAndVertically || CameraMotionState == CameraMotionStates.PatrollingVertically)
					CameraMotionState = CameraMotionStates.PatrollingVertically;
				else
					CameraMotionState = CameraMotionStates.NotMoving;
				URL += "29";
				break;
				
			case PanRight: // Note Left and right are reversed on the device.
				CameraMotionState = CameraMotionStates.PanningRight;
				URL += "4";
				break;
				
			case StopLeft: // Note Left and right are reversed on the device.
				CameraMotionState = CameraMotionStates.NotMoving;
				URL += "5";
				break;
				
			case PanLeft: // Note Left and right are reversed on the device.
				CameraMotionState = CameraMotionStates.PanningLeft;
				URL += "6";
				break;
				
			case StopRight: // Note Left and right are reversed on the device.
				CameraMotionState = CameraMotionStates.NotMoving;
				URL += "7";
				break;
				
			case PanUp:
				CameraMotionState = CameraMotionStates.PanningUp;
				URL += "0";
				break;
				
			case StopUp:
				CameraMotionState = CameraMotionStates.NotMoving;
				URL += "1";
				break;
				
			case PanDown:
				CameraMotionState = CameraMotionStates.PanningDown;
				URL += "2";
				break;
				
			case StopDown:
				CameraMotionState = CameraMotionStates.NotMoving;
				URL += "3";
				break;
				
			default:
				return;
		}
		
		// Call the URL command.
		try {
			httpclient.execute(new HttpGet(URL));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public enum NavigationStates {
		Maintain,
		Accelerate,
		Decelerate,
		TurnLeft,
		TurnRight
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
		StopDown
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
					default:
						mNavConn.send(new byte[] {'m'});
						break;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			// Reset navigation state.
			setNavigationState(NavigationStates.Maintain);
		}
	}
}
