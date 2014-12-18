/*
 * Name: SendReceiveBytes.java
 * Author: Alex Lippitt
 * Date Created: 27/06/2013
 * Purpose: Send and receive data across bluetooth. receives data and sends a message
 * to the handler. The handler will require implementation in main activity.
 * Reference - http://arduinobasics.blogspot.co.nz/2013/03/arduinobasics-bluetooth-android_25.html
 *           - http://developer.android.com/guide/topics/connectivity/bluetooth.html#ManagingAConnection
 */

package bluetoothpackage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ConcurrentLinkedQueue;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

public class SendReceiveBytes implements Runnable {
		
	private BluetoothSocket btSocket;
	private InputStream btInputStream = null;
	private OutputStream btOutputStream = null;
	String TAG = "SendReceiveBytes";
	Handler mHandler;

	// Message types used by the Handler
	public static final int MESSAGE_WRITE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int LOST_CONNECTION = 3;

	private static final String EXCP_LOG = "App_Exceptions";

	// Is true as long as the input/output streams are open
	private boolean isRunning = false;
	
	private ConcurrentLinkedQueue<Integer> queue = new ConcurrentLinkedQueue<Integer>();

	public SendReceiveBytes(BluetoothSocket socket, Handler handle) {
		btSocket = socket;
		mHandler = handle;
		InputStream tmpIn = null;
		OutputStream tmpOut = null;
		try {
			tmpIn = btSocket.getInputStream();
			tmpOut = btSocket.getOutputStream();
		} catch (IOException streamError) {
			Log.e(TAG, "Error when getting input or output Stream");
		}
		btInputStream = tmpIn;
		btOutputStream = tmpOut;
		isRunning = true;

	}
	
	/**
	 * Read from the queue
	 * @return the value in the queue
	 * @throws Exception if the buffer is empty
	 */
	public Integer readQueue() throws Exception{
		return queue.remove(); 
	}
	
	public int queueLength() {
		return queue.size();
	}
	

	public synchronized void run() {
		// Moves the current Thread into the background
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
		byte[] buffer = new byte[1024]; // buffer store for the stream
		int bytes; // bytes returned from read()

		// Keep listening to the InputStream until an exception occurs
		while (!Thread.currentThread().isInterrupted()) {
			try {
				// Read from the InputStream
				bytes = btInputStream.read(buffer);
				
				//Write to the queue 
				for (int i = 0; i < bytes; i++) {
					queue.offer((int)buffer[i]);
				}
			} catch (IOException e) {
				mHandler.obtainMessage(LOST_CONNECTION).sendToTarget();
				break;
			}
		}
	}

	/**
	 * Call this from the main activity to send data to the remote device
	 * @param bytes Bytes to be sent via Bluetooth
	 */
	public void write(byte[] bytes) {
		try {
			btOutputStream.write(bytes);
		} catch (IOException e) {
			mHandler.obtainMessage(LOST_CONNECTION).sendToTarget();
			Log.e(EXCP_LOG, "exception", e);
		}
	}

	/**
	 * Call this from the main activity to shutdown the connection 
	 */
	public void cancel() {
		try {
			btSocket.close();
		} catch (IOException e) {
			Log.e(EXCP_LOG, "exception", e);
		}
		isRunning = false;
		Thread.currentThread().interrupt();
	}

	/**
	 * Check if the reading thread is running
	 * @return
	 */
	public boolean checkRunning() {
		return isRunning;
	}
}