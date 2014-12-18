package savingPackageNew;
import java.util.ArrayList;
import java.util.Map;

import android.util.Log;

import com.dropbox.sync.android.DbxAccountManager;

public class FileMasterClass {

	/**
	 * Class Status: 
	 * Save and Load from sdcard is fully functional.
	 * Export to dropbox works.
	 * Can write a patientInfoFile to dropbox and create the session folder.
	 */
	SDCardClass sdFile;
	DropboxClass dbxFile;
	boolean fileType = true; //True for sdcard, false for Dropbox
	
	/**
	 * Loading constructor for sdcard
	 * @param path
	 * @param location
	 */
	public FileMasterClass(String path) {
		fileType = true;
		sdFile = new SDCardClass(path);			
	}
	
	/**
	 * Saving Constructor for sdcard
	 * @param path
	 * @param patientName
	 * @param location
	 */
	public FileMasterClass(String path, String patientName) {
		fileType = true; //Set it to sdcard state
		sdFile = new SDCardClass(path, patientName);
	}
	
	/**
	 * Loading constructor for dropbox
	 * @param DbxAcctMgr 
	 * @param patientName 
	 * @param empty empty string to distinguish the constructor from the save constructor
	 */
	public FileMasterClass(DbxAccountManager DbxAcctMgr, String patientName, String empty) {
		fileType = false;
		dbxFile = new DropboxClass(DbxAcctMgr, patientName, empty);
	}
	
	/**
	 * Saving constructor for DropBox
	 * @param DbxAcctMgr
	 * @param patientName
	 */
	public FileMasterClass(DbxAccountManager DbxAcctMgr, String patientName) {
		fileType = false; //Set it to dropbox state
		dbxFile = new DropboxClass(DbxAcctMgr, patientName);
	}
	
	/**
	 * Add a session to the file
	 * @param type
	 * @param timeCh1
	 * @param timeCh2
	 * @param ampCh1
	 * @param ampCh2
	 * @return
	 */
	public boolean addSession(String type, double[] timeCh1, double[] timeCh2, double[] ampCh1, double[] ampCh2) {
		if(fileType) { //sdcard
			return sdFile.addSession(type, timeCh1, timeCh2, ampCh1, ampCh2);
		} else { //Dropbox
			return dbxFile.addSession(type, timeCh1, timeCh2, ampCh1, ampCh2);
		}		
	}
	
	/**
	 * Write the patient info file. Will overwrite the current file, if exists 
	 * @param id
	 * @param dob
	 * @param gender
	 * @param notes
	 */
	public void writePatientInfo(String id, String dob, String gender, String notes) {
		Log.d("DROPBOX", "Master File state: " + fileType);
		if(fileType) { //sdcard
			Log.d("DROPBOX", "Calling sdcard write patient");
			sdFile.writePatientInfo(id, dob, gender, notes);
		} else { //Dropbox
			Log.d("DROPBOX", "Calling dbx write patient");
			dbxFile.writePatientInfo(id, dob, gender, notes);
		}
	}
	
	/**
	 * 
	 * @return Number of Sessions
	 */
	public int getNumSessions() {
		Log.d("DROPBOX", "getNumSessions Master state: " + fileType);
		if(fileType) { //sdcard
			Log.d("DROPBOX", "getNumSessions Master state: " + sdFile.getNumSessions());
			return sdFile.getNumSessions();
		} else { //Dropbox
			return dbxFile.getNumSessions();
		}
	}
	
	/**
	 * Update The patient details
	 * @param map
	 */
	public void updatePatientDetails(Map<patientInfoMap.PATIENT_INFO, String> map) {
		Log.d("DROPBOX", "updatePatientDetails Master state: " + fileType);
		if(fileType) { //sdcard
			sdFile.updatePatientDetails(map);
		} else { //Dropbox
			dbxFile.updatePatientDetails(map);
		}
	}
	
	/**
	 * Returns the session data
	 * @param session
	 * @return 
	 * @throws IndexOutOfBoundsException
	 * @throws Exception
	 */
	public ArrayList<double[]> getSessionData(int session) throws IndexOutOfBoundsException, Exception {
		if(fileType) { //sdcard
			return sdFile.getSessionData(session);
		} else { //Dropbox
			return dbxFile.getSessionData(session);
		}
	}
	
	/**
	 * 
	 * @param sess
	 * @return
	 * @throws Exception
	 */
	public String[] getSessionInfo(int sess) throws Exception {
		if(fileType) { //sdcard
			return sdFile.getSessionInfo(sess);
		} else { //Dropbox
			return dbxFile.getSessionInfo(sess);
		}
	}
	
	/**
	 * 
	 * @return 
	 * @throws Exception
	 */
	public ArrayList<String[]> getAllSessionInfo() throws Exception {
		if(fileType) { //sdcard
			return sdFile.getAllSessionInfo();
		} else { //Dropbox
			return dbxFile.getAllSessionInfo();
		}
	}
	
	/**
	 * The patient info file format is:
	 * ID:
	 * Name:
	 * Date of Birth:
	 * Gender:
	 * Additional Notes:
	 * Session Path:
	 * Number of Sessions:
	 * 
	 * @return filled with the patient info
	 */
	public ArrayList<String> getPatientInfo() {
		Log.d("DROPBOX", "Master File state: " + fileType);
		if(fileType) { //sdcard
			return sdFile.getPatientInfo();
		} else { //Dropbox
			return dbxFile.getPatientInfo();
		}
	}
	
	/**
	 * Save the file to the dropbox. The higher level needs to indicate that saving to dropbox is occuring
	 * @param DbxAcctMgr
	 */
	public void exportToDropbox(DbxAccountManager DbxAcctMgr) {
		//If true you are currently saving on the sdcard and want to transfer to dropbox.
		if(fileType) {
			ArrayList<String> info = sdFile.getPatientInfo();
			String patientName = info.get(1);
			String filePath = sdFile.getFileLocation();
			dbxFile = new DropboxClass(patientName, filePath, sdFile.getNumSessions(), DbxAcctMgr);
			dbxFile.transferToDropBox();
		}
	}
	
	/**
	 * Return the file path
	 * @return
	 */
	public String getFilePath() {
		if(fileType)
			return sdFile.getFilePath();
		else
			return dbxFile.getFilePath();
	}
	
	/**
	 * Returns the type of file being used.
	 * @return True = sdCard, False = Dropbox
	 */
	public boolean getFileType() {
		return fileType;
	}
}
