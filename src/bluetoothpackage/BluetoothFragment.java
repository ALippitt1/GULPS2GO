package bluetoothpackage;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import bluetoothpackage.BluetoothService.MyLocalBinder;

public class BluetoothFragment extends Fragment {

	private static BluetoothListener mCallback;

	// Container Activity must implement this interface
	// Used to pass data to the top level
	public interface BluetoothListener {
		public void connected(boolean status);
	}

	// Variable linking to the app context, used for toasts and registering
	// services and receivers
	private static Context appContext;

	// Log debug tags
	private static final String TAG = "Screen Rotation";
	private static final String BTTAG = "BT Service";
	private static final String TT = "STOP";

	// on activity result flags
	protected static final int DISCOVERY_REQUEST = 1;
	protected static final int DIALOG_FRAGMENT = 2;

	// Message flags
	public static final int DATA_READ = 0;
	public static final int DATA_WRITE = 1;
	private BluetoothAdapter btAdapter; // Initializes bluetooth hardware on a
										// device

	ArrayList<BluetoothDevice> btDeviceArray; // Arraylist holding discovered
												// bluetooth devices
	private static ArrayList<String> btDeviceNames = new ArrayList<String>(); // Array list holding discovered bluetooth names

	private String toastText;

	private BluetoothDevice mDevice; // Variable used to store the selected
										// device.

	private SelectBTDevice newFragment; // Fragment displaying all the
										// discovered devices

	// Service and service intent (used for starting and stopping the service
	// different from the bind intent)
	private BluetoothService btService;
	Intent serviceIntent;
	
	bluetoothHandler mHandler = new bluetoothHandler(this);
	
	private static boolean connected = false;
	//Used to prevent multiple discovery results. Currently registers two discovery requests
	private boolean hasDiscovered = false; 

	/**
	 * OnCreate method of the fragment. initializes a variety of variables calls
	 * checkBT() method. if BT is enabled it will discover devices.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(BTTAG, "                               ");
		// Select Which device you would like to connect to dialog fragment
		newFragment = SelectBTDevice.newInstance(123);
	}

	/**
	 * OnCreateView method, currently returns null because fragment has no UI.
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		return null;
	}

	/**
	 * Checks to make sure the callback is implemented
	 */
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		// This makes sure that the container activity has implemented
		// the callback interface. If not, it throws an exception
		try {
			mCallback = (BluetoothListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnHeadlineSelectedListener");
		}
	}

	/**
	 * Called when the fragment is resumed
	 */
	public void onResume() {
		super.onResume();
		// Register receiver

		appContext = getActivity().getApplicationContext(); // Get the activity
															// context

		btDeviceArray = new ArrayList<BluetoothDevice>(); // Initate array list
															// to store found
															// bluetooth devices

		startUpService();

	}

	/**
	 * Called when the fragment is paused and before the fragment is destroyed.
	 * Calls release to release all resources.
	 */
	public void onPause() {
		super.onPause();
		Log.d(TT, "Frag Paused");
		releaseThreads();
	}

	/**
	 * onDestroy method. Called when fragment is no longer in use. Called after
	 * onStop() and before onDetach().
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(TT, "fragment destroyed");
	}

	/**
	 * Releases threads if they are active, unregisters any broadcast receivers.
	 * cancels discovery if on and cancels the connection if connected to a
	 * device.
	 */
	private void releaseThreads() {

		// unregister broadcast receivers
		try {
			appContext.unregisterReceiver(discoveryResult);
		} catch (Exception e) {
		}

		//Log.d(TT, "Frag unregisterReceiver");

		// Disable discovery
		if (btAdapter != null) {
			if (btAdapter.isDiscovering())
				btAdapter.cancelDiscovery();
		}
		Log.d(TT, "Frag cancel Discovery");
		try {
			appContext.unbindService(myConnection);
		} catch (Exception e) {
		}
		//Log.d(TT, "Frag unbinded");
	}

	/**
	 * Check to see if bluetooth is enabled. if it isn't run startBT()
	 */
	private void checkBT() {
		Log.d(BTTAG, "checkBT");
		btAdapter = BluetoothAdapter.getDefaultAdapter(); // Get the default information
		// Check to see if BT is turned on
		if (btAdapter.isEnabled()) {
		} else {
			startBT();
		}
	}

	/***
	 * clear the device array, starts an intent asking if you want to turn on BT
	 * and make it discoverable, discoverability is required to connect to other
	 * devices.
	 */
	public void startBT() {
		btDeviceArray.clear();
		// For enabling BT and Discovery//
		// Register for discovery events		
		String beDiscoverable = BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE;
		startActivityForResult(new Intent(beDiscoverable), DISCOVERY_REQUEST);
	}

	/**
	 * when an activity is completed this function is called. Currently looks
	 * for the completion of two different activities Case 1: DISCOVERY_REQUEST
	 * - Currently loops until user selects ok. then calls findDevices method 
	 * - Called after the pop-up box asking users if they want to turn on
	 * bluetooth Case 2: DIALOG_FRAGMENT - Called when the SelectBTDevice
	 * fragment ends. Will either connect to the - selected device, rediscover
	 * or cancel (Needs to be implemented)
	 * 
	 */
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case DISCOVERY_REQUEST:
			Log.d("BLUE", "Start Discovery");
			Toast.makeText(appContext, "Discovery in progress: ", Toast.LENGTH_SHORT).show();
			checkBT();
			findDevices();
			break;
		case DIALOG_FRAGMENT:
			switch (resultCode) {
			case SelectBTDevice.RESULT_SELECT:
				mDevice = btDeviceArray.get(newFragment.getSelectedDevice());
				startUpConnection();
				break;
			case SelectBTDevice.RESULT_REDISCOVER:
				findDevices();
				break;
			case SelectBTDevice.RESULT_CANCEL:
				// Do something maybe exit program
				break;
			}

			break;
		}
	}

	/**
	 * Binds to the bluetooth service
	 * 
	 * @param pos
	 */
	public void startUpConnection() {
		if (mDevice != null) {			
			Intent intent = new Intent(appContext, BluetoothService.class);
			appContext.bindService(intent, myConnection, 0); // Path continues with myConnection class				
		}
	}

	/**
	 * Clears all array of data, used to reset discovered devices
	 */
	private void clearArrays() {
		btDeviceArray.clear();
		btDeviceNames.clear();
	}

	/**
	 * Used to look for other active BT devices. Cancels discovery if already
	 * active (Blocking function) registers a receiver for if a device is found
	 * and if the discovery process is finished.
	 */
	private void findDevices() {
		// Disable discovery
		if (btAdapter.isDiscovering())
			btAdapter.cancelDiscovery();

		clearArrays();

		if (mDevice == null) { // If not connected to device
			toastText = "Starting discovery for remote devices....:";
			Toast.makeText(appContext, toastText, Toast.LENGTH_SHORT).show();
			// Start discovery
			if (btAdapter.startDiscovery()) {
				hasDiscovered = false;
				toastText = "Discovery thread started...Scanning for devices";
				Toast.makeText(appContext, toastText, Toast.LENGTH_SHORT).show();
				appContext.registerReceiver(discoveryResult, new IntentFilter(BluetoothDevice.ACTION_FOUND));
				appContext.registerReceiver(discoveryResult, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
			}
		}// endif

	}// end findDevices

	/**
	 * Receiver currently looking for two results. Case 1: ACTION_FOUND - A
	 * device has been found, the required details are added to the arrays Case
	 * 2: DISCOVERY_FINISHED - Finished looking for devices, will create a new
	 * fragment window to - display all found devices (Need to have a check that
	 * a device was found)
	 */
	BroadcastReceiver discoveryResult = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {
				String remoteDeviceName = intent.getStringExtra(BluetoothDevice.EXTRA_NAME);
				BluetoothDevice remoteDevice;
				remoteDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				toastText = "Discovered: " + remoteDeviceName;
				Toast.makeText(appContext, toastText, Toast.LENGTH_SHORT).show();

				// Only add the device if it has a name
				if (remoteDeviceName != null) {
					btDeviceArray.add(remoteDevice);
					btDeviceNames.add(remoteDeviceName);
				}

			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(intent.getAction())) {
				if(!hasDiscovered) {
					hasDiscovered = true;
					Toast.makeText(appContext, "Discovery Finished",Toast.LENGTH_SHORT).show();
					FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
					Fragment prev = getActivity().getSupportFragmentManager().findFragmentByTag("SelectBTDevice");
					if (prev != null) { // Checks the fragment isn't already created.
						ft.remove(prev);
					}
					ft.addToBackStack(null);
					ft.commit();
	
					newFragment.initFragment(btDeviceNames);
					newFragment.setTargetFragment(BluetoothFragment.this,
							DIALOG_FRAGMENT);
					newFragment.show(getChildFragmentManager().beginTransaction(),
							"SelectBTDevice");
				} 
			}
		}
	};// End broadcast receiver

	/**
	 * Called by the higher level to try a write to the BT output stream. Will
	 * only try if the variable isConnected is "true".
	 * 
	 * @param bytes
	 */
	public void writeBT(byte[] bytes) {
		btService.writeToBluetooth(bytes);
	}

	
	
	/**
	 * The Handler that gets information back from the BT Socket. When
	 * receive a message titled= MESSAGE READ, the handler will pass a message
	 * to the next level, currently no data processing is done at this point.
	 */
	public static class bluetoothHandler extends Handler {
		//Create a weak reference back to the calling bluetooth fragment. This is so the handler
		//can call functions from within the fragment without losing its static modifier
		//See: http://www.androiddesignpatterns.com/2013/01/inner-class-handler-memory-leak.html
		private final WeakReference<BluetoothFragment> reference;
		
		public bluetoothHandler(BluetoothFragment frag) {
			reference = new WeakReference<BluetoothFragment>(frag);
		}
		
		@Override
		public void handleMessage(Message msg) {
			BluetoothFragment mFragment = reference.get(); //Return the calling fragment
			if (mFragment != null) {
				switch (msg.what) {
				case SendReceiveBytes.MESSAGE_WRITE:
					// Do something when writing
					break;
				case SendReceiveBytes.MESSAGE_READ:
					break;
				case SendReceiveBytes.LOST_CONNECTION:
					Log.d("BT Service", "Lost Connection");
					connected = false;
					mCallback.connected(false);
					mFragment.disconnectBT();
					Toast.makeText(appContext, "Disconnected", Toast.LENGTH_SHORT).show();
					break;
				case ConnectToBluetooth.CONNECTED:
					Log.d("BT Service", "Connected");
					mCallback.connected(true);
					connected = true;
					Toast.makeText(appContext, "Connected", Toast.LENGTH_SHORT).show();
					break;
				case ConnectToBluetooth.FAIL_CONNECT:
					Log.d("BT Service", "Failed to connect");
					connected = false;
					mCallback.connected(false);
					mFragment.disconnectBT();
					Toast.makeText(appContext, "Failed to Connect", Toast.LENGTH_SHORT).show();
					break;
				}
			}
		}
	};

	/**
	 * Starts up the service, checks if it is already running, if so tries to
	 * reconnect else it will create a new service
	 */
	private void startUpService() {
		// Check if the service has been initiated, if it hasn't
		// start bluetooth connection process
		if (!isMyServiceRunning()) {
			serviceIntent = new Intent(appContext, BluetoothService.class);
			appContext.startService(serviceIntent);
		} else {
			// If the service is running we want to reconnect to the service
			serviceIntent = new Intent(appContext, BluetoothService.class);
			startUpConnection();
		}
	}

	/**
	 * Checks to see if the bluetooth service is running Taken from:
	 * http://stackoverflow
	 * .com/questions/600207/android-check-if-a-service-is-running
	 * 
	 * @return
	 */
	public boolean isMyServiceRunning() {
		ActivityManager manager = (ActivityManager) appContext
				.getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager
				.getRunningServices(Integer.MAX_VALUE)) {
			if (BluetoothService.class.getName().equals(
					service.service.getClassName())) {
				Log.d(BTTAG, "Found BT Service");
				return true;
			}
		}
		return false;
	}

	/**
	 * Called when trying to bind to the service
	 */
	private ServiceConnection myConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			MyLocalBinder binder = (MyLocalBinder) service;
			btService = binder.getService();
			binder.setHandler(mHandler);
			if (btService.getSelectedDevice() == null) {
				if (mDevice != null) {
					btService.initiateConnection(mDevice, btAdapter);
					Log.d(TAG, "initiateConnection");
				} else
					btService = null;
			} else {
				mDevice = btService.getSelectedDevice();
				btAdapter = btService.getBTAdapter();
			}
		}

		public void onServiceDisconnected(ComponentName className) {
		}

	};

	/**
	 * Stop the service when shutting down the app.
	 */
	public void stopService() {
		if (serviceIntent != null) {
			appContext.stopService(serviceIntent);
		}
	}

	/**
	 * Called to start bluetooth connection, Connects/creates the bluetooth
	 * service as well
	 */
	public void connectBT() {
		startUpService();
		checkBT();
		if (btAdapter.isEnabled())
			findDevices();
	}

	/**
	 * Called to end bluetooth connection and stops the service
	 */
	public void disconnectBT() {
		stopService();
		releaseThreads();
		mDevice = null;
		btAdapter = null;
		mCallback.connected(false);
	}
	
	public Integer readBuffer() throws Exception{
		return btService.readBuffer();
	}
	
	/**
	 * Return true if connected to a device
	 * @return
	 */
	public boolean isConnected() {
		return connected;
	}
	
	/**
	 * Checks if bluetooth is supported by checking that a bluetooth adapter can be returned.
	 * See BluetoothAdapater documentation for more details
	 * @return
	 */
	public static boolean isSupported() {
		boolean supp = false;
		if(BluetoothAdapter.getDefaultAdapter() != null)
			supp = true;
		else
			supp = false;
		return supp;
	}
}


