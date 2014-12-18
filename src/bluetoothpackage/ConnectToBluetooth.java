/* Name: ConnectToBluetooth
 * Author: Alex Lippitt
 * Date Created: 27/06/2013
 * Last Modified:
 * Purpose: Thread to initiate connect to the desired device. An intent filter must be used in the main application
 * to look for BluetoothDevice.ACTION_ACL_CONNECTED goes true. When this occurs the device is connected. 
 * References: http://arduinobasics.blogspot.com.au/2013/03/arduinobasics-bluetooth-android.html
 * 
 */

package bluetoothpackage;

import java.io.IOException;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

public class ConnectToBluetooth implements Runnable {
	private BluetoothAdapter btAdapter;
	private BluetoothDevice btDevice;
	private Handler mHandler;
	
	public final static int CONNECTED = 9;
	public final static int FAIL_CONNECT = 8;
	
	private static final String EXCP_LOG = "App_Exceptions";

	private boolean isConnected = false; // Flag set high when connection is
											// completed

	// Connection Variables
	private BluetoothSocket mySocket = null;
	private UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

	public ConnectToBluetooth(BluetoothDevice remoteDevice, BluetoothAdapter adapter, Handler handle) {
		btDevice = remoteDevice;
		btAdapter = adapter;
		mHandler = handle;
		try {
			mySocket = btDevice.createRfcommSocketToServiceRecord(uuid);
		} catch (IOException createSocketException) {
			// Problem with creating a socket
			Log.e(EXCP_LOG, "exception", createSocketException);
		}
	}
	
	/**
	 * Connect to the bluetoothShield through the Socket. This will
	 * block until it succeeds or throws an IOException
	 */
	public synchronized void run() {
		// Moves the current Thread into the background
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
		// Cancel discovery on Bluetooth Adapter to prevent slow connection
		btAdapter.cancelDiscovery();

		try {
			
			mySocket.connect();
			isConnected = true;
			mHandler.obtainMessage(CONNECTED).sendToTarget();
		} catch (IOException connectException) {
			connectException.printStackTrace();			
			try {
				mySocket.close(); 
				isConnected = false;
				mHandler.obtainMessage(FAIL_CONNECT).sendToTarget();
			} catch (IOException closeException) {
				Log.e(EXCP_LOG, "exception", closeException);
			}
			
			return;
		}

	}

	/**
	 *  Will cancel an in-progress connection, and close the socket 
	 */
	public synchronized void cancel() {
		try {
			mySocket.close();
		} catch (IOException e) {
		}
	}

	/**
	 * Returns the socket created
	 * 
	 * @return
	 */
	public BluetoothSocket getSocket() {
		return mySocket;
	}

	/**
	 * Checks the status of the connection
	 * 
	 * @return
	 */
	public boolean checkConnection() {
		return isConnected;
	}

}