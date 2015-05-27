package com.bioimpedance;

import java.util.ArrayList;
import java.util.Map;

import savingPackage.DropboxClass;
import savingPackage.FileMasterClass;
import savingPackage.SDCardClass;
import savingPackage.patientInfoMap;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import bluetoothpackage.BluetoothFragment;

import com.dropbox.sync.android.DbxAccountManager;
import com.dropbox.sync.android.DbxException;
import com.dropbox.sync.android.DbxPath.InvalidPathException;
import com.extraDisplayFragments.EditTypeDialog;
import com.extraDisplayFragments.SelectDisplayList;
import com.fileexplorer.FileChooserFragment;
import com.fileexplorer.dbxFileChooserFragment;

public class MainActivity extends FragmentActivity implements FileChooserFragment.FileChooserListener,
	BluetoothFragment.BluetoothListener, SelectDisplayList.SelectListener, EditTypeDialog.EditTypeListener, 
	dbxFileChooserFragment.FileChooserListener {

	//Fragment Declaration
	private WelcomeFragment introFrag = WelcomeFragment.newInstance();
	private EnterDetailsFragment detailFrag = EnterDetailsFragment.newInstance();
	private PatientWindowFragment patientFrag;
	private DisplaySessionFragment dispSessFrag = DisplaySessionFragment.newInstance();
	private NewSessionFragment newSessFrag = NewSessionFragment.newInstance();
	private FileChooserFragment saveFileFrag = FileChooserFragment.newInstance(FileChooserFragment.STATES.SAVE);
	private FileChooserFragment loadFileFrag = FileChooserFragment.newInstance(FileChooserFragment.STATES.LOAD);
	private BluetoothFragment bluetoothFrag = new BluetoothFragment();
	private EditTypeDialog editType = new EditTypeDialog();
	private DropboxSaveFragment dbxSaveFrag = DropboxSaveFragment.newInstance(DropboxSaveFragment.STATES.SAVE);
	private dbxFileChooserFragment dbxLoadFrag = dbxFileChooserFragment.newInstance();
	
	//Tags used to identify each fragment
	private static final String TAG_WELCOME = "welcome";
	private static final String TAG_DETAIL = "detail";
	private static final String TAG_PATIENT = "patient_window";
	private static final String TAG_DISP_SESS = "display_session";
	private static final String TAG_NEW_SESS = "new_session";
	private static final String TAG_SAVE_FILE = "save_file";
	private static final String TAG_LOAD_FILE = "load_file";
	private static final String TAG_DBX_SAVE = "dbx_save";
	private static final String TAG_DBX_LOAD = "dbx_load";
	
	//Keys for saving state
	private static final String FILE_KEY = "FILE_KEY";
	private static final String FILE_BOOL = "FILE_BOOL";
	private static final String BT_KEY = "BLUETOOTH_KEY";
	
	private FragmentManager fm;
	private FragmentTransaction fragmentTransaction;
	private static final String BT_TAG = "BluetoothFragment";
	private TextView statusDisplay;
	
	private boolean startGraphing = false;
	private decodeThread readThread;
	
	private Map<patientInfoMap.PATIENT_INFO, String> mapInfo;
	private FileMasterClass file = null;
	private String swallowType;
	
	private boolean editDetails = false;
	
	private DbxAccountManager mDbxAcctMgr;
	private final static String APP_KEY = "yp5ziveogr422nm"; //App Console -> GULPS2GO
	private final static String APP_SECRET = "1xbtm5uqi2f4jgu";
	static final int REQUEST_LINK_TO_DBX = 99;  
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d("RESUME", "        ");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		statusDisplay = (TextView) findViewById(R.id.bluetoothStatusDisplay);
		
		mDbxAcctMgr = DbxAccountManager.getInstance(getApplicationContext(), APP_KEY, APP_SECRET);
		//Check to see if there is a linked account, if not initiate the connection.
		if (mDbxAcctMgr.getLinkedAccount() == null) {
			mDbxAcctMgr.startLink(this, REQUEST_LINK_TO_DBX);
		}
		
		readThread = new decodeThread();
		new Thread(readThread, "Decode Thread").start();
			
		
		//Add the bluetooth fragment
		fm = getSupportFragmentManager();
		fragmentTransaction = fm.beginTransaction();
		fragmentTransaction.add(bluetoothFrag, BT_TAG);
		fragmentTransaction.commit();
		// Check whether the activity is using the layout version with
		// the fragment_container FrameLayout. If so, we must add the first
		// fragment
		
		if (findViewById(R.id.fragmentContainer) != null) {

			// However, if we're being restored from a previous state,
			// then we don't need to do anything and should return or else
			// we could end up with overlapping fragments.
			if (savedInstanceState != null) {
				//Restore fragments if already created, prevents creating a second instance of the fragment
				PatientWindowFragment tempfrag1 = (PatientWindowFragment) getSupportFragmentManager().findFragmentByTag(TAG_PATIENT);
				NewSessionFragment tempfrag2 = (NewSessionFragment) getSupportFragmentManager().findFragmentByTag(TAG_NEW_SESS);
				DisplaySessionFragment tempfrag3 = (DisplaySessionFragment) getSupportFragmentManager().findFragmentByTag(TAG_DISP_SESS);
				if (tempfrag1 != null) {
					patientFrag = tempfrag1;
				} 
				if (tempfrag2 != null) {
					newSessFrag = tempfrag2;
				}
				if(tempfrag3 != null) { 
					dispSessFrag = tempfrag3;
				}
				
				if(savedInstanceState.getBoolean(FILE_BOOL)) { //Using SD Card
					loadFile(savedInstanceState.getString(FILE_KEY), true);
				} else {
					loadFile(savedInstanceState.getString(FILE_KEY), false);
				}
				setStatusDisplay(savedInstanceState.getBoolean(BT_KEY));
				
				return;
			}

			// Add the fragment to the 'fragment_container' FrameLayout
			getSupportFragmentManager().beginTransaction()
			.add(R.id.fragmentContainer, introFrag, TAG_WELCOME).commit();
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		Log.d("RESUME", "Main Activity Saving State");
		if (file != null) {
			outState.putString(FILE_KEY, file.getFilePath());
			outState.putBoolean(FILE_BOOL, file.getFileType());
		}
		//Get the bluetooth status
		if(bluetoothFrag != null)
			outState.putBoolean(BT_KEY, bluetoothFrag.isConnected());
		
		super.onSaveInstanceState(outState);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		WelcomeFragment fragment = (WelcomeFragment) getSupportFragmentManager().findFragmentByTag(TAG_WELCOME);
		if((fragment != null) && (fragment.isVisible())) {
			Log.d("TEST", "Stop bluetooth here");		
			if(bluetoothFrag.isConnected()) {
				bluetoothFrag.disconnectBT();
			}
		}
		NewSessionFragment frag = (NewSessionFragment) getSupportFragmentManager().findFragmentByTag(TAG_NEW_SESS);
		if((frag != null) && (frag.isVisible())) {
			Log.d("RESUME","Stop Graphing here");
			startGraphing = false;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	private void changeFragment(Fragment f, String tag) {
		fragmentTransaction = getSupportFragmentManager().beginTransaction();
		fragmentTransaction.replace(R.id.fragmentContainer, f, tag);
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.commit();
	}
	
	
	/**
	 * welcome_page.xml onClick Listener. 
	 * @param v
	 */
	public void welcomeEvent(View v) {
		int id = v.getId();
		if(id == R.id.welcomeButton1) { //Create new Patient Record
			editDetails = false;
			detailFrag = EnterDetailsFragment.newInstance();
			changeFragment(detailFrag, TAG_DETAIL);
		}else if(id == R.id.welcomeButton2) { //Load Previous Patient Record
			changeFragment(loadFileFrag, TAG_LOAD_FILE);
		}
	}
	
	/**
	 * enter_details.xml onClickListener
	 * @param v
	 */
	public void detailsDone(View v) {
		int id = v.getId();
		if (R.id.datePickerButton == id){
			detailFrag.setDate();
		}else if(R.id.detailsButton1 == id) {

			mapInfo = detailFrag.getPatientInfo();

			boolean rFlag = true;

			//Checks if any values in the map are empty.
			//Don't want to check additional notes
			for (patientInfoMap.PATIENT_INFO key : mapInfo.keySet()) {

				if (key != patientInfoMap.PATIENT_INFO.ADDITIONAL) {
					String value = mapInfo.get(key);

					if(!notEmpty(value)) {
						rFlag = false;
					}
				}
			}
			//Only progress to the next fragment if details are complete
			if(rFlag) {
				//Checks to see if a new patient is being created or if the 
				//details of a patient are being added.
				if(editDetails) {
					file.updatePatientDetails(mapInfo);					
					patientFrag = PatientWindowFragment.newInstance(mapInfo, bluetoothFrag.isConnected());
					changeFragment(patientFrag, TAG_PATIENT);
				} else {
					changeFragment(saveFileFrag, TAG_SAVE_FILE);
				}
			} else {
				Toast.makeText(this,"Patient details are not complete", Toast.LENGTH_LONG).show();
			}
		}
	}
	
	/**
	 * patient_window.xml onClickListener
	 * @param v
	 */
	public void patientWindow(View v) {
		int id = v.getId();
		if (R.id.startNewSession == id) {
			if(bluetoothFrag.isConnected()) {
				FragmentManager fm = getSupportFragmentManager();
				editType.show(fm, "Edit Type");
			} else {
				bluetoothFrag.connectBT();
			}
		}else if (R.id.loadSession == id) {
			if(file.getNumSessions() > 0) {
				try {
					ArrayList<String[]> info = file.getAllSessionInfo();
					ArrayList<String> name = new ArrayList<String>();
					for(int i = 0; i < info.size(); i++) {
						name.add("Session " + (i+1) + "             " + info.get(i)[2]);
					}
					dispSessFrag.chooseSession(name, getSupportFragmentManager());
					changeFragment(dispSessFrag,TAG_DISP_SESS);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else
				Toast.makeText(this, "No Previous Sessions", Toast.LENGTH_SHORT).show();
			
		}else if (R.id.startNewPatient == id) {
			editDetails = false;
			detailFrag = EnterDetailsFragment.newInstance();
			changeFragment(detailFrag, TAG_DETAIL);
		}else if (R.id.loadPatient == id) {
			changeFragment(loadFileFrag, TAG_LOAD_FILE);			
		}else if (R.id.connectBT == id) {
			bluetoothFrag.connectBT();
		}else if (R.id.disconnectBT == id) {
			bluetoothFrag.disconnectBT();
		}else if (R.id.detailsButton1 == id) {
			editDetails = true;
			detailFrag = EnterDetailsFragment.newInstance(mapInfo);
			changeFragment(detailFrag, TAG_DETAIL);
		}else if (R.id.exportDropBox == id) {
			if(!mDbxAcctMgr.hasLinkedAccount()) { //Check if dropbox has already been linked.
				mDbxAcctMgr.startLink((FragmentActivity)this, REQUEST_LINK_TO_DBX);
				Log.d("DROPBOX", "Linking....");
				Toast.makeText(this, "Linking to Dropbox Account", Toast.LENGTH_SHORT).show();
			}else {
				Log.d("DROPBOX", "Already Linked");
				Toast.makeText(this, "Exporting to Dropbox Account", Toast.LENGTH_SHORT).show();
				file.exportToDropbox(mDbxAcctMgr);
				
			}
		}
	}
	
	/**
	 * display_loaded_sessions.xml onClickListener
	 * @param v
	 */
	public void loadPreviousSession(View v) {
		int id = v.getId();
		if (R.id.displaySessionNew == id) {
			if(file.getNumSessions() > 0) {
				try {
					ArrayList<String[]> info = file.getAllSessionInfo();
					ArrayList<String> name = new ArrayList<String>();
					for(int i = 0; i < info.size(); i++) {
						name.add("Session " + (i+1) + "             " + info.get(i)[2]);
					}
					dispSessFrag.chooseSession(name, getSupportFragmentManager());
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else
				Toast.makeText(this, "No Previous Sessions", Toast.LENGTH_SHORT).show();
		}
	}
	
	/**
	 * new_session.xml onClickListener
	 * @param v
	 */
	public void newSessionEvent(View v) {
		int id = v.getId();
		if(R.id.newSessStart == id) {	
			newSessFrag.startGraph();
			readThread.resetDecode();
			startGraphing = true;
		}else if(R.id.newSessResume == id) {
			newSessFrag.resumeGraph();
			startGraphing = true;
		}else if(R.id.newSessPause == id) {
			newSessFrag.pauseGraph();
			startGraphing = false;
		}else if(R.id.newSessRestart == id) {
			newSessFrag.restartGraph();
			readThread.resetDecode();
			startGraphing = true;
		}else if(R.id.newSessSave == id) {
			startGraphing = false;
			ArrayList<double[]> data = newSessFrag.getData();
			double[] timeCh1 = new double[data.size()];
			double[] timeCh2 = new double[data.size()];
			double[] ampCh1 = new double[data.size()];
			double[] ampCh2 = new double[data.size()];
			for (int i = 0; i < data.size(); i++) {
				timeCh1[i] = data.get(i)[0];
				ampCh1[i] = data.get(i)[1];
				timeCh2[i] = data.get(i)[2];
				ampCh2[i] = data.get(i)[3];
			}

			file.addSession(swallowType, timeCh1, timeCh2, ampCh1, ampCh2);
			Toast.makeText(this, "Save Complete", Toast.LENGTH_SHORT).show();
		}
	}
	
	/**
	 * Called by gui_save. 
	 * If saving on sdcard, function will initialize the Master save class
	 * Handles the switching to dropbox window
	 * @param v
	 */
	public void saveLocationEvent(View v) {
		int id = v.getId();
		if(R.id.btSave == id) {
			String filePath = saveFileFrag.getFilePath(); //Get the file path
			String path = filePath + "/" + mapInfo.get(patientInfoMap.PATIENT_INFO.NAME) + ".txt";
			if(!SDCardClass.checkFileExists(path)) { //Check if file does not exist
				//Open a file at file path location
				file = new FileMasterClass(filePath, mapInfo.get(patientInfoMap.PATIENT_INFO.NAME));
				file.writePatientInfo(mapInfo.get(patientInfoMap.PATIENT_INFO.ID), 
								mapInfo.get(patientInfoMap.PATIENT_INFO.DOB),
										mapInfo.get(patientInfoMap.PATIENT_INFO.GENDER),
												mapInfo.get(patientInfoMap.PATIENT_INFO.ADDITIONAL));
				
				patientFrag = PatientWindowFragment.newInstance(mapInfo, bluetoothFrag.isConnected());
				changeFragment(patientFrag, TAG_PATIENT);
			}
			else { //Patient already exists
				Toast.makeText(this, "Patient File already exists", Toast.LENGTH_SHORT).show();
			}			
		} else if(R.id.btDbxFiles == id) {
			//Test to see which fragment is visible, allows to pick the right dropbox save/load fragment
			FileChooserFragment test = (FileChooserFragment) getSupportFragmentManager().findFragmentByTag(TAG_SAVE_FILE);
			FileChooserFragment test2 = (FileChooserFragment) getSupportFragmentManager().findFragmentByTag(TAG_LOAD_FILE);
			dbxFileChooserFragment test3 = (dbxFileChooserFragment) getSupportFragmentManager().findFragmentByTag(TAG_DBX_LOAD);
			if (test != null && test.isVisible()) {
				changeFragment(dbxSaveFrag, TAG_DBX_SAVE);
			}
			if (test2 != null && test2.isVisible()) {
				changeFragment(dbxLoadFrag,TAG_DBX_LOAD);
			} 
			if (test3 != null && test3.isVisible()) {
				changeFragment(loadFileFrag,TAG_LOAD_FILE);
			}
		}
	}
	
	/**
	 * dropbox_save_fragment.xml
	 * @param v
	 */
	public void dbxSaveEvent(View v) {
		int id = v.getId();
		
		if (R.id.btSDFiles == id) {
			//Load the sdcard file reader
			Log.d("DROPBOX", "Change to SDCard");
			changeFragment(saveFileFrag, TAG_SAVE_FILE);
		}else if (R.id.dbxSaveButton == id) {
			//Save to dropbox
			Log.d("DROPBOX", "Save Button pressed");
			try {
				if(!DropboxClass.fileExists(mDbxAcctMgr, mapInfo.get(patientInfoMap.PATIENT_INFO.NAME))) {
					Log.d("DROPBOX", "Creating patient in dropbox");
					file = new FileMasterClass(mDbxAcctMgr, mapInfo.get(patientInfoMap.PATIENT_INFO.NAME));
					file.writePatientInfo(mapInfo.get(patientInfoMap.PATIENT_INFO.ID), 
								mapInfo.get(patientInfoMap.PATIENT_INFO.DOB),
										mapInfo.get(patientInfoMap.PATIENT_INFO.GENDER),
												mapInfo.get(patientInfoMap.PATIENT_INFO.ADDITIONAL));
					patientFrag = PatientWindowFragment.newInstance(mapInfo, bluetoothFrag.isConnected());
					changeFragment(patientFrag, TAG_PATIENT);
				} else {
					Toast.makeText(this, "Patient File already exists", Toast.LENGTH_SHORT).show();
				}
			} catch (InvalidPathException e) {
				e.printStackTrace();
			} catch (DbxException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	/**
	 * Called by gui_graph.xml
	 * Handles the rotation of the graph depending on the type of graph fragment visible. Could
	 * be either new session or displaying a previous session
	 * @param v
	 */
	public void changeGraphDisplay(View v) {
		int id = v.getId();
		DisplaySessionFragment fragment = (DisplaySessionFragment) getSupportFragmentManager().findFragmentByTag(TAG_DISP_SESS);
		NewSessionFragment fragment2 = (NewSessionFragment) getSupportFragmentManager().findFragmentByTag(TAG_NEW_SESS);
		if (fragment != null && fragment.isVisible()) {
			if(R.id.displaySessionRotate == id) {
				dispSessFrag.rotateGraph();
			}
		}
		if(fragment2 != null && fragment2.isVisible() ) {
			if(R.id.displaySessionRotate == id) {
				newSessFrag.rotateGraph();
			}
			
		}
	}

	/**
	 * Called from the FileChooserFragment. Means a file has been selected.
	 */
	public void fileSelected(String filePath) {
		loadFile(filePath, true);		
		patientFrag = PatientWindowFragment.newInstance(mapInfo, bluetoothFrag.isConnected());
		changeFragment(patientFrag, TAG_PATIENT);
	}
	
	/**
	 * Called from the dbxFileChooserFragment, means a file has been selected
	 */
	public void dbxFileSelected(String filePath) {
		loadFile(filePath, false);		
		patientFrag = PatientWindowFragment.newInstance(mapInfo, bluetoothFrag.isConnected());
		changeFragment(patientFrag, TAG_PATIENT);
	}
	
	/**
	 * File loader
	 * @param path path of file to be loaded
	 * @param type pass true for sdcard, false for dropbox
	 */
	private void loadFile(String path, boolean type) {
		if(type) {
			file = new FileMasterClass(path);
		} else {
			file = new FileMasterClass(mDbxAcctMgr, path, new String("Empty"));
		}
		try {
			mapInfo = patientInfoMap.createMap(file.getPatientInfo());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	/**
	 * Class to read data from the bluetooth fragment and store it into two different buffers
	 * @author ajl157
	 *
	 */
	private class decodeThread implements Runnable {
		private static final int SYNC_BYTE = 255;
		private static final int WAIT_4_SYNC = 0;
		private static final int LOW_BYTE_CH1 = 1;
		private static final int HIGH_BYTE_CH1 = 2;
		private static final int LOW_BYTE_CH2 = 3;
		private static final int HIGH_BYTE_CH2 = 4;
		private static final int BYTE_SHIFT = 8;
		private static final int DISCARD_VALUE = 4000;
		private static final double TIME_STEP = 0.0201;

		private int position = WAIT_4_SYNC;
		double t = 0;

		/**
		 * Called to reset the decode buffers and start from the start. Should
		 * be called, if stopping mid-way through a read.
		 */
		public void resetDecode() {
			position = WAIT_4_SYNC;
			newSessFrag.clearQueues();
			t = 0;
		}

		public void run() {
			// Moves the current Thread into the background
	        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
			Integer value = 0;
			int impLevel1 = 0;
			int impLevel2 = 0;
			int lowByte = 0;
			boolean read = true;

			while (true) {
				
				//
				if (bluetoothFrag.isConnected()) {
					//Try read from the bluetooth buffer. If there is nothing to read then set the read flag to false
					try {
						value = bluetoothFrag.readBuffer() & 0xFF;
						read = true;
					} catch (Exception e) {
						// Nothing in buffer
						read = false;
					}
					//If the read and graphing flags are set then go through the decode sequence outlined in thesis 
					if (read) {
						if (startGraphing) {
							if (value != null) {
								if ((position == WAIT_4_SYNC)
										&& (value == SYNC_BYTE)) {
									impLevel1 = 0;
									impLevel2 = 0;
									
									position = LOW_BYTE_CH1;
								} else if (position == LOW_BYTE_CH1) {
									lowByte = value;
									position = HIGH_BYTE_CH1;
								} else if (position == HIGH_BYTE_CH1) {
									impLevel1 = value << BYTE_SHIFT;
									impLevel1 += lowByte;
									position = LOW_BYTE_CH2;
								} else if (position == LOW_BYTE_CH2) {
									lowByte = value;
									position = HIGH_BYTE_CH2;
								} else if (position == HIGH_BYTE_CH2) {
									impLevel2 = value << BYTE_SHIFT;
									impLevel2 += lowByte;
									
									t = t + TIME_STEP;
									Log.d("SEND_TIME", "Calculated time = " + t);
									//Discard value if abnormally high
									if (impLevel1 < DISCARD_VALUE) {
										newSessFrag.writeDataBuffer1(impLevel1,t);
									}
									if (impLevel2 < DISCARD_VALUE) {
										newSessFrag.writeDataBuffer2(impLevel2,t);
									}
									position = WAIT_4_SYNC;
								} 
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Used to check if the string is empty or consists only of white spaces
	 * returns true if not empty
	 * Reference: http://javahowto.blogspot.co.nz/2006/09/is-string-empty.html
	 * @param s
	 * @return
	 */
	private boolean notEmpty(String s) {
		return (s != null && s.trim().length() > 0);
	}

	/**
	 * Bluetooth Fragment Implemented Method
	 */
	public void connected(boolean status) {
		//Checks if the patientFragment exists before trying to write to it
		if(patientFrag != null)
			patientFrag.disconnectButton(status);
		setStatusDisplay(status);
	}
	
	/**
	 * Changes the display status of the bluetooth
	 * @param status
	 */
	private void setStatusDisplay(boolean status) {
		if(status) {
			statusDisplay.setText(R.string.connected);
			statusDisplay.setTextColor(getResources().getColor(R.color.green));
		}
		else {
			statusDisplay.setText(R.string.disconnected);
			statusDisplay.setTextColor(getResources().getColor(R.color.red));
		}
	}

	/**
	 * 
	 */
	public void onFinishedEditDialog(String text) {
		swallowType = text;
		changeFragment(newSessFrag, TAG_NEW_SESS);
	}

	/**
	 * From the SelectDisplayList Fragment, Called when a session has been selected to be viewed.
	 * Writes data to the display session fragment
	 */
	public void buttonClicked(int select) {
		try {
			select = select+1; // add a offset because session number starts at 1
			ArrayList<double[]> data = file.getSessionData(select);
			double[] dataCh1 = new double[data.get(1).length];
			double[] dataCh2 = new double[data.get(1).length];
			double[] timeCh1 = new double[data.get(1).length];
			double[] timeCh2 = new double[data.get(1).length];
			timeCh1 = data.get(0);
			timeCh2 = data.get(2);
			for(int i = 0; i < data.get(1).length; i++) {
				dataCh1[i] = (int) data.get(1)[i];
				dataCh2[i] = (int) data.get(3)[i];
			}
			// Read the session details from the session info file.
			// Store the type, date, time and number of swallows in String Array
			// To be displayed in when reviewing graphs
			String[] info = new String[4];
			info[0] = Integer.toString(select);
			String[] temp = file.getSessionInfo(select);
			for (int i = 1; i < (temp.length + 1); i++) {
				info[i] = temp[i-1];
			}
			
			dispSessFrag.addData(dataCh1, dataCh2, timeCh1, timeCh2);
			dispSessFrag.addDetails(info);
			
		} catch (IndexOutOfBoundsException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void buttonCancel() {
	}
	
	/**
	 * * DropBox activity onClick listener
	 * taken from: https://www.dropbox.com/developers/sync/start/android 
	 * on linking accounts
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	    if (requestCode == REQUEST_LINK_TO_DBX) {
	        if (resultCode == Activity.RESULT_OK) {
	            // ... Start using Dropbox files.
	        	
	        } else {
	            // ... Link failed or was cancelled by the user.
	        	Toast.makeText(this, "Linking to DropBox Failed", Toast.LENGTH_SHORT).show();
	        }
	    } else {
	        super.onActivityResult(requestCode, resultCode, data);
	    }
	}


}

