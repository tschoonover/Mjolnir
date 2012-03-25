package com.section9.mjolnir;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 
 * @author Odysseus
 */
public class RobotModel
{
	// Camera definitions.
	private final String CAM_AUTH_PARMS = "&user=admin&pwd=section9";
	private final String CAM_VIDEO_STEAM = "/videostream.cgi?";
	private final String CAM_EXEC_COMMAND = "/decoder_control.cgi?"; 
	private final String CAM_SET_MODE = "/camera_control.cgi?";
	
	// Class members
	private Timer mNavUpdateTimer;
	private TimerTask mUpdateNavStateTask; 
	private NavigationStates mNavigationState;
	private boolean mCameraHQ;
	private boolean mCameraIR;
	private CameraMotionStates mCameraMotionState;
	private VideoConnection mVidConn;
	private NavigationConnection mNavConn;
	private String mNavSubsystemIP;
	private int mNavSubsystemPort;
	private String mVidSubsystemIP;
	private int mVidSubsystemPort;
	
	/**
	 * Constructor. Creates a new RobotModel object.
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
	
	/**
	 * Gets the video subsystem camera high quality mode.
	 * @return True if the camera is in high quality mode, false otherwise.
	 */
	public boolean getCameraHQ()
	{
		return mCameraHQ;
	}
	
	/**
	 * Gets the video subsystem camera IR state.
	 * @return True if the camera infrared LED's are on, false otherwise.
	 */
	public boolean getCameraIR()
	{
		return mCameraIR;
	}
	
	/**
	 * Gets the video subsystem camera motion state.
	 * @return The current camera motion state.
	 */
	public CameraMotionStates getCameraMotionState()
	{
		return mCameraMotionState;
	}
	
	/**
	 * Gets the navigation subsystem state.
	 * @return The current navigation state.
	 */
	public synchronized NavigationStates getNavigationState()
	{
		return mNavigationState;
	}
	
	/**
	 * Sets the navigation subsystem state.
	 * @param state - The new navigation state. 
	 */
	public synchronized void setNavigationState(NavigationStates state)
	{
		mNavigationState = state;
	}

	/**
	 * Gets the navigation subsystem history. 
	 * @return
	 */
	public String[] getNavigationHistory()
	{
		return mNavConn.getHistory();
	}
	
	/**
	 * Registers the specified listener for video frame received events.
	 * @param listener - The listener to register.
	 */
	public void setVideoFrameReceivedListener(VideoConnection.VideoFrameReceivedListener listener)
	{
		mVidConn.setVideoFrameReceivedListener(listener);
	}
	
	/**
	 * Starts the navigation subsystem.
	 * @param ip - The IP address or DNS name of the navigation subsystem.
	 * @param port - The port address of the navigation subsystem.
	 * @throws Exception Unable to start navigation subsystem.
	 */
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

	/**
	 * Stops the navigation subsystem.
	 */
	public void StopNavSubsystem()
	{
		// Halt navigation transmissions.
		if (mUpdateNavStateTask != null)
			mUpdateNavStateTask.cancel();
		
		// Disconnect from navigation subsystem.
		mNavConn.disconnect();	
	}
	
	/**
	 * Starts the video subsystem.
	 * @param ip - The IP address or DNS name of the video subsystem.
	 * @param port - The port address of the video subsystem.
	 * @throws Exception Unable to start video subsystem.
	 */
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
		mVidConn.sendAsync(getHttpGetRequest(mVidSubsystemIP, CAM_VIDEO_STEAM + CAM_AUTH_PARMS).getBytes("UTF8"));
	}

	/**
	 * Stops the video subsystem.
	 */
	public void StopVideoSubsystem()
	{
		// Disconnect from the video subsystem.
		mVidConn.disconnect();
	}

	/**
	 * Executes a video subsystem camera command.
	 * @param cmd - The command to execute.
	 * @throws Exception Unable to execute command.
	 */
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
				getHttpGetRequest(mVidSubsystemIP, requestURI).getBytes("UTF8")
		);
	}
	
	/**
	 * Returns the text of an HTTP GET request for the specified host and request URI. 
	 * @param host - The host to which the request will be submitted.
	 * @param requestURI - The URI of the requested resource.
	 * @return The HTTP GET request text.
	 */
	private String getHttpGetRequest(String host, String requestURI)
	{
		String requestMessage = "GET " + requestURI + " HTTP/1.1\r\nHost: " + host + "\r\n\r\n";
		return requestMessage;
	}
	
	/**
	 * The set of all valid navigation states for the navigation subsystem.
	 */
	public enum NavigationStates
	{
		Maintain,
		Accelerate,
		Decelerate,
		TurnLeft,
		TurnRight,
		Stop
    }
	
	/**
	 * The set of all valid camera motion states for the video subsystem.
	 */
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
	
	/**
	 * The set of all valid camera commands for the video subsystem.
	 */
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
	 * Class for transmitting navigation commands to the navigation subsystem
	 * at fixed intervals.
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
