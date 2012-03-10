package com.section9.mjolnir;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class RobotModel
{
	
	// Camera definitions.
	private final String CAM_BASE_URL = "http://192.168.1.98/";
	private final String CAM_AUTH_PARMS = "&user=admin&pwd=section9";
	private final String CAM_VIDEO_STEAM = "/videostream.cgi?";
	private final String CAM_EXEC_COMMAND = "decoder_control.cgi?"; 
	private final String CAM_SET_MODE = "camera_control.cgi?";
	
	
	private final String NAV_IP = "192.168.1.99";
	private final int NAV_PORT = 23;
	private final String VID_IP = "192.168.1.98";
	private final int VID_PORT = 80;
	
	// Class members
	private MjpegDecodingListener mMjpegDecodingListener;
	private RobotConnection mNavConn;
	private Timer mNavUpdateTimer;
	private TimerTask mUpdateNavStateTask; 
	private NavigationStates mNavigationState;
	private boolean mCameraHQ;
	private boolean mCameraIR;
	private CameraMotionStates mCameraMotionState;
	private RobotConnection mVidConn;
	private RobotConnection mVidControlConn;
	
	/**
	 * Constructor
	 */
	public RobotModel()
	{
		// Create connection object to the navigation subsystem.
		mNavConn = new RobotConnection();
		
		// Create connection object to the video subsystem.
		mVidConn = new RobotConnection();
		
		// Create connection object to the video motion subsystem.
		mVidControlConn = new RobotConnection();
		
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
	
	public String getVideoStreamRequest()
	{
		return "GET " + CAM_VIDEO_STEAM + CAM_AUTH_PARMS + " HTTP/1.1\r\nHost: " + VID_IP.toString() + "\r\n\r\n";
	}
	
	public synchronized void setNavigationState(NavigationStates state)
	{
		mNavigationState = state;
	}
	
	public void setDataReceivedListener(RobotConnection.DataReceivedListener listener)
	{
		mNavConn.setDataReceivedListener(listener);
	}

	public void StartNavSubsystem() throws IOException
	{
		// Connect to navigation subsystem.
		mNavConn.connect(NAV_IP, NAV_PORT);
		
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
	
	public void StartVideoSubsystem() throws Exception
	{
		// Register to receive video subsystem data.
		mMjpegDecodingListener = new MjpegDecodingListener();
		mVidConn.setDataReceivedListener(mMjpegDecodingListener);
		
		// Connect to video subsystem.
		mVidConn.connect(VID_IP, VID_PORT);
		mVidConn.sendAsync(getVideoStreamRequest().getBytes("UTF8"));
		
		// Connect to the video control subsystem.
//		mVidControlConn.connect(VID_IP, VID_PORT);
		
		// Set initial video states.
//		ExecuteCameraCommand(CameraCommands.DeactivateHQ);
//		ExecuteCameraCommand(CameraCommands.DeactivateIR);
//		ExecuteCameraCommand(CameraCommands.StopUp);
	}

	public void StopVideoSubsystem()
	{
		// Disconnect from the video subsystem.
		mVidConn.disconnect();
		
		// disconnect from the video control subsystem.
//		mVidControlConn.disconnect();
	}

	public void ExecuteCameraCommand(CameraCommands cmd)
	{
		// Build the command URL. 
		String URL = CAM_BASE_URL;
		
		// Append the command and parameter(s).
		switch (cmd)
		{
			case ActivateIR:
				mCameraIR = true;
				URL += CAM_EXEC_COMMAND + "command=95";
				break;
				
			case DeactivateIR:
				mCameraIR = false;
				URL += CAM_EXEC_COMMAND + "command=94";
				break;
				
			case ActivateVPatrol:
				if (mCameraMotionState == CameraMotionStates.PatrollingHorizontally
					|| mCameraMotionState == CameraMotionStates.PatrollingHorizontallyAndVertically)
				{
					mCameraMotionState = CameraMotionStates.PatrollingHorizontallyAndVertically;
				}
				else
					mCameraMotionState = CameraMotionStates.PatrollingVertically;
				URL += CAM_EXEC_COMMAND + "command=26";
				break;
				
			case DeactivateVPatrol:
				if (mCameraMotionState == CameraMotionStates.PatrollingHorizontallyAndVertically
					|| mCameraMotionState == CameraMotionStates.PatrollingHorizontally)
				{
					mCameraMotionState = CameraMotionStates.PatrollingHorizontally;
				}
				else
					mCameraMotionState = CameraMotionStates.NotMoving;
				URL += CAM_EXEC_COMMAND + "command=27";
				break;
				
			case ActivateHPatrol:
				if (mCameraMotionState == CameraMotionStates.PatrollingVertically
					|| mCameraMotionState == CameraMotionStates.PatrollingHorizontallyAndVertically)
				{
					mCameraMotionState = CameraMotionStates.PatrollingHorizontallyAndVertically;
				}
				else
					mCameraMotionState = CameraMotionStates.PatrollingHorizontally;
				URL += CAM_EXEC_COMMAND + "command=28";
				break;
				
			case DeactivateHPatrol:
				if (mCameraMotionState == CameraMotionStates.PatrollingHorizontallyAndVertically
					|| mCameraMotionState == CameraMotionStates.PatrollingVertically)
				{
					mCameraMotionState = CameraMotionStates.PatrollingVertically;
				}
				else
					mCameraMotionState = CameraMotionStates.NotMoving;
				URL += CAM_EXEC_COMMAND + "command=29";
				break;
				
			case PanRight: // Note Left and right are reversed on the device.
				mCameraMotionState = CameraMotionStates.PanningRight;
				URL += CAM_EXEC_COMMAND + "command=4";
				break;
				
			case StopLeft: // Note Left and right are reversed on the device.
				mCameraMotionState = CameraMotionStates.NotMoving;
				URL += CAM_EXEC_COMMAND + "command=5";
				break;
				
			case PanLeft: // Note Left and right are reversed on the device.
				mCameraMotionState = CameraMotionStates.PanningLeft;
				URL += CAM_EXEC_COMMAND + "command=6";
				break;
				
			case StopRight: // Note Left and right are reversed on the device.
				mCameraMotionState = CameraMotionStates.NotMoving;
				URL += CAM_EXEC_COMMAND + "command=7";
				break;
				
			case PanUp:
				mCameraMotionState = CameraMotionStates.PanningUp;
				URL += CAM_EXEC_COMMAND + "command=0";
				break;
				
			case StopUp:
				mCameraMotionState = CameraMotionStates.NotMoving;
				URL += CAM_EXEC_COMMAND + "command=1";
				break;
				
			case PanDown:
				mCameraMotionState = CameraMotionStates.PanningDown;
				URL += CAM_EXEC_COMMAND + "command=2";
				break;
				
			case StopDown:
				mCameraMotionState = CameraMotionStates.NotMoving;
				URL += CAM_EXEC_COMMAND + "command=3";
				break;
				
			case ActivateHQ:
				mCameraHQ = true;
				URL += CAM_SET_MODE + "param=0&value=32";
				break;
				
			case DeactivateHQ:
				mCameraHQ = false;
				URL += CAM_SET_MODE + "param=0&value=8";
				break;
				
			default:
				return;
		}
		
		// Append the authentication parameters.
		URL += CAM_AUTH_PARMS;
		
		// Execute the camera command as an HTTP GET request.
		//mVidControlConn.sendAsync(buildHttpRequest().getBytes("UTF8"));
	}
	
	private String buildHttpRequest()
	{
		return "";
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

	
	public void setVideoFrameReceivedListener(MjpegDecodingListener.VideoFrameReceivedListener videoFrameReceivedListener)
	{
		
	}
}
