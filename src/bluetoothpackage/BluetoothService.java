package bluetoothpackage;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public class BluetoothService extends Service {

	private final IBinder myBinder = new MyLocalBinder();
	public static final String TAG = "BT Service"; // Debug tag
	public static final String TT = "STOP"; // Debug tag

	private BluetoothAdapter btAdapter = null;
	private BluetoothDevice btDevice = null;
	private BluetoothSocket btSocket = null;

	private ConnectToBluetooth connectBT = null;
	private SendReceiveBytes sendReceiveBT = null;

	private Handler mHandle;

	@Override
	public IBinder onBind(Intent arg0) {
		Log.d(TAG, "onBind");
		return myBinder;
	}

	public class MyLocalBinder extends Binder {
		// Return the service to connect to
		BluetoothService getService() {
			return BluetoothService.this;
		}

		// Set the handler to pass messages to the upper level
		void setHandler(Handler handle) {
			mHandle = handle;
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
		registerReceiver(checkIsConnected, new IntentFilter(
				BluetoothDevice.ACTION_ACL_CONNECTED));

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		return Service.START_STICKY;
	}

	public void disconnectBluetooth() {
		disconnect();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		disconnect();
		
		unregisterReceiver(checkIsConnected);

		stopSelf();

	}

	/**
	 * Starts the bluetooth connection by setting the state of the service to
	 * bluetoothStates.CONNECT, this will start the connection thread on the
	 * next iteration.
	 * 
	 * @param device
	 * @param adapter
	 */
	public void initiateConnection(BluetoothDevice device,
			BluetoothAdapter adapter) {
		btDevice = device;
		btAdapter = adapter;
		connectBT = new ConnectToBluetooth(btDevice, btAdapter, mHandle);
		new Thread(connectBT,"Bluetooth Connect").start();
	}

	/**
	 * Tries to write to the bluetooth socket. Sends the data to the
	 * SendReceiveBytes thread
	 * 
	 * @param bytes
	 */
	public void writeToBluetooth(byte[] bytes) {
		if (sendReceiveBT.checkRunning()) {
			sendReceiveBT.write(bytes);
		}
	}

	/**
	 * Used to return the btDevice if the fragment is reconnecting to the
	 * service
	 * 
	 * @return
	 */
	public BluetoothDevice getSelectedDevice() {
		return btDevice;
	}

	/**
	 * Used to return the btAdapter if the fragment is trying to reconnect to
	 * the service
	 * 
	 * @return
	 */
	public BluetoothAdapter getBTAdapter() {
		return btAdapter;
	}
	
	
	public boolean isBluetoothConnected() {
		return connectBT.checkConnection();
	}

	/**
	 * Broadcast receiver used to detect if the bluetooth device is connected
	 */
	BroadcastReceiver checkIsConnected = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
				// Device is connected
				btSocket = connectBT.getSocket();
				sendReceiveBT = new SendReceiveBytes(btSocket, mHandle);
				new Thread(sendReceiveBT,"Bluetooth Read_Write").start();
			}
		}

	}; // End Broadcast Receiver

	/**
	 * Reads data from the queue in the send/receive thread and passes
	 * it up
	 * @return
	 * @throws Exception
	 */
	public Integer readBuffer() throws Exception {
		return sendReceiveBT.readQueue();
	}

	public void disconnect() {
		if (sendReceiveBT != null) {
			sendReceiveBT.cancel();
			sendReceiveBT = null;
		}
		Log.d(TT, "Stopped sendReceiveBT");
		if (connectBT != null) {
			connectBT.cancel();
			connectBT = null; 
		}
		btDevice = null;
		btAdapter = null;
	}

}
