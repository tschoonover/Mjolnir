package com.section9.mjolnir;

import java.util.Timer;
import java.util.TimerTask;

public class RobotModel
{
	// Camera definitions.
	private final String CAM_AUTH_PARMS = "&user=admin&pwd=section9";
	private final String CAM_VIDEO_STEAM = "/videostream.cgi?";
	private final String CAM_EXEC_COMMAND = "/decoder_control.cgi?"; 
	private final String CAM_SET_MODE = "/camera_control.cgi?";
	
	private String mNavSubsystemIP;
	private int mNavSubsystemPort;
	private String mVidSubsystemIP;
	private int mVidSubsystemPort;
	
	// Class members
	private Timer mNavUpdateTimer;
	private TimerTask mUpdateNavStateTask; 
	private NavigationStates mNavigationState;
	private boolean mCameraHQ;
	private boolean mCameraIR;
	private CameraMotionStates mCameraMotionState;
	private VideoConnection mVidConn;
	private NavigationConnection mNavConn;
	
	/**
	 * Constructor
	 */
	public RobotModel()
	{
		// Create connection object to the navigation subsystem.
		mNavConn = new NavigationConnection();
		
		// Create connection object to the video subsystem.
		mVidConn = new VideoConnection();

		// Create timer for triggering re-occurring navigation updates.
		mNavUpdateTimer = new Timer();
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
	
	public synchronized NavigationStates getNavigationState()
	{
		return mNavigationState;
	}
	
	public synchronized void setNavigationState(NavigationStates state)
	{
		mNavigationState = state;
	}

	public String[] getNavigationOutput()
	{
		return mNavConn.getHistory();
	}
	
	public void setVideoFrameReceivedHandler(VideoConnection.VideoFrameReceivedListener listener)
	{
		mVidConn.setVideoFrameReceivedListener(listener);
	}
	
	public void StartNavSubsystem(String ip, int port) throws Exception
	{
		// Save the connection information.
		mNavSubsystemIP = ip;
		mNavSubsystemPort = port;
		
		// Connect to navigation subsystem.
		mNavConn.connect(mNavSubsystemIP, mNavSubsystemPort);
		
		// Set initial navigation state.
		setNavigationState(NavigationStates.Maintain);
		
		// Schedule re-occurring navigation updates.
		mUpdateNavStateTask = new UpdateNavStateTask();
		mNavUpdateTimer.schedule(mUpdateNavStateTask, 0, 500);
	}

	public void StopNavSubsystem()
	{
		// Halt navigation transmissions.
		if (mUpdateNavStateTask != null)
			mUpdateNavStateTask.cancel();
		
		// Disconnect from navigation subsystem.
		mNavConn.disconnect();	
	}
	
	public void StartVideoSubsystem(String ip, int port) throws Exception
	{
		// Save the connection information.
		mVidSubsystemIP = ip;
		mVidSubsystemPort = port;
		
		// Set initial video states.
		ExecuteCameraCommand(CameraCommands.DeactivateHQ);
		ExecuteCameraCommand(CameraCommands.DeactivateIR);
		ExecuteCameraCommand(CameraCommands.StopUp);
		
		// Connect to video subsystem.
		mVidConn.connect(mVidSubsystemIP, mVidSubsystemPort);
		mVidConn.sendAsync(buildHttpGetRequest(mVidSubsystemIP, CAM_VIDEO_STEAM + CAM_AUTH_PARMS).getBytes("UTF8"));
	}

	public void StopVideoSubsystem()
	{
		// Disconnect from the video subsystem.
		mVidConn.disconnect();
	}

	public void ExecuteCameraCommand(CameraCommands cmd) throws Exception
	{
		// Build the request-URI. 
		String requestURI = "";
		switch (cmd)
		{
			case ActivateIR:
				mCameraIR = true;
				requestURI += CAM_EXEC_COMMAND + "command=95";
				break;
				
			case DeactivateIR:
				mCameraIR = false;
				requestURI += CAM_EXEC_COMMAND + "command=94";
				break;
				
			case ActivateVPatrol:
				if (mCameraMotionState == CameraMotionStates.PatrollingHorizontally
					|| mCameraMotionState == CameraMotionStates.PatrollingHorizontallyAndVertically)
				{
					mCameraMotionState = CameraMotionStates.PatrollingHorizontallyAndVertically;
				}
				else
					mCameraMotionState = CameraMotionStates.PatrollingVertically;
				requestURI += CAM_EXEC_COMMAND + "command=26";
				break;
				
			case DeactivateVPatrol:
				if (mCameraMotionState == CameraMotionStates.PatrollingHorizontallyAndVertically
					|| mCameraMotionState == CameraMotionStates.PatrollingHorizontally)
				{
					mCameraMotionState = CameraMotionStates.PatrollingHorizontally;
				}
				else
					mCameraMotionState = CameraMotionStates.NotMoving;
				requestURI += CAM_EXEC_COMMAND + "command=27";
				break;
				
			case ActivateHPatrol:
				if (mCameraMotionState == CameraMotionStates.PatrollingVertically
					|| mCameraMotionState == CameraMotionStates.PatrollingHorizontallyAndVertically)
				{
					mCameraMotionState = CameraMotionStates.PatrollingHorizontallyAndVertically;
				}
				else
					mCameraMotionState = CameraMotionStates.PatrollingHorizontally;
				requestURI += CAM_EXEC_COMMAND + "command=28";
				break;
				
			case DeactivateHPatrol:
				if (mCameraMotionState == CameraMotionStates.PatrollingHorizontallyAndVertically
					|| mCameraMotionState == CameraMotionStates.PatrollingVertically)
				{
					mCameraMotionState = CameraMotionStates.PatrollingVertically;
				}
				else
					mCameraMotionState = CameraMotionStates.NotMoving;
				requestURI += CAM_EXEC_COMMAND + "command=29";
				break;
				
			case PanRight: // Note Left and right are reversed on the device.
				mCameraMotionState = CameraMotionStates.PanningRight;
				requestURI += CAM_EXEC_COMMAND + "command=4";
				break;
				
			case StopLeft: // Note Left and right are reversed on the device.
				mCameraMotionState = CameraMotionStates.NotMoving;
				requestURI += CAM_EXEC_COMMAND + "command=5";
				break;
				
			case PanLeft: // Note Left and right are reversed on the device.
				mCameraMotionState = CameraMotionStates.PanningLeft;
				requestURI += CAM_EXEC_COMMAND + "command=6";
				break;
				
			case StopRight: // Note Left and right are reversed on the device.
				mCameraMotionState = CameraMotionStates.NotMoving;
				requestURI += CAM_EXEC_COMMAND + "command=7";
				break;
				
			case PanUp:
				mCameraMotionState = CameraMotionStates.PanningUp;
				requestURI += CAM_EXEC_COMMAND + "command=0";
				break;
				
			case StopUp:
				mCameraMotionState = CameraMotionStates.NotMoving;
				requestURI += CAM_EXEC_COMMAND + "command=1";
				break;
				
			case PanDown:
				mCameraMotionState = CameraMotionStates.PanningDown;
				requestURI += CAM_EXEC_COMMAND + "command=2";
				break;
				
			case StopDown:
				mCameraMotionState = CameraMotionStates.NotMoving;
				requestURI += CAM_EXEC_COMMAND + "command=3";
				break;
				
			case ActivateHQ:
				mCameraHQ = true;
				requestURI += CAM_SET_MODE + "param=0&value=32";
				break;
				
			case DeactivateHQ:
				mCameraHQ = false;
				requestURI += CAM_SET_MODE + "param=0&value=8";
				break;
				
			default:
				return;
		}
		
		// Append the authentication parameters.
		requestURI += CAM_AUTH_PARMS;
		
		// Execute the camera command as an HTTP GET request.
		RobotConnectionBase.sendAsyncAndClose(
				mVidSubsystemIP,
				mVidSubsystemPort,
				buildHttpGetRequest(mVidSubsystemIP, requestURI).getBytes("UTF8")
		);
	}
	
	private String buildHttpGetRequest(String host, String requestURI)
	{
		String requestMessage = "GET " + requestURI + " HTTP/1.1\r\nHost: " + host + "\r\n\r\n";
		return requestMessage;
	}
	
	public enum NavigationStates
	{
		Maintain,
		Accelerate,
		Decelerate,
		TurnLeft,
		TurnRight,
		Stop
    }
	
	public enum CameraMotionStates
	{
		NotMoving,
		PanningUp,
		PanningDown,
		PanningLeft,
		PanningRight,
		PatrollingHorizontally,
		PatrollingVertically,
		PatrollingHorizontallyAndVertically
	}
	
	public enum CameraCommands
	{
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
	
	/**
	 * 
	 * @author Odysseus
	 *
	 */
	private class UpdateNavStateTask extends TimerTask
	{
		@Override
		public void run()
		{
			// Attempt to transmit navigation state.
			try
			{
				switch (getNavigationState())
				{
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
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
}
