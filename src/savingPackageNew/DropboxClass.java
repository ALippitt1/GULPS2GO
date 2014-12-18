package savingPackageNew;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import savingPackageNew.FileOperationsClass.INFO;
import android.util.Log;
import au.com.bytecode.opencsv.CSVReader;

import com.dropbox.sync.android.DbxAccountManager;
import com.dropbox.sync.android.DbxException;
import com.dropbox.sync.android.DbxException.Unauthorized;
import com.dropbox.sync.android.DbxFile;
import com.dropbox.sync.android.DbxFileSystem;
import com.dropbox.sync.android.DbxPath;
import com.dropbox.sync.android.DbxPath.InvalidPathException;

public class DropboxClass {
	
	private String sdPath; //Location of patient data (where the patient info file is) used for exporting
	private int number; //number of sessions
	DbxAccountManager mDbxAcctMgr;
	DbxFileSystem dbxFs;
	DbxFile dbxPatientFile; //Patient File
	DbxPath dbxPatientFilePath; //File path
	
	private Map<INFO, String> infoDict = new HashMap<INFO, String>();
		
	/**
	 * Constructor for loading form the dropbox
	 * @param DbxAcctMgr
	 * @param path File path to the patient info file on the dropbox
	 * @param empty Is not used, used to distinguish this constructor apart from saving constructor
	 */
	public DropboxClass (DbxAccountManager DbxAcctMgr, String path, String empty) {
		mDbxAcctMgr = DbxAcctMgr; //Get DB manager
		dbxPatientFilePath = new DbxPath(path); //Create the path to the file
		loadFile(); //Get details from file
	}
	
	
	/**
	 * Constructor for creating a new patient
	 * @param DbxAcctMgr
	 * @param patientName
	 * @param id
	 * @param dob
	 * @param gender
	 * @param notes
	 */
	public DropboxClass(DbxAccountManager DbxAcctMgr,String patientName) {
		mDbxAcctMgr = DbxAcctMgr;
		number = 0;
		infoDict.put(INFO.NAME,patientName);
		String sessionPath = infoDict.get(INFO.NAME) + "_sessions/";
		infoDict.put(INFO.SESS_PATH, sessionPath);
		dbxPatientFilePath = new DbxPath(infoDict.get(INFO.NAME) + ".txt");
		try {
			dbxFs = DbxFileSystem.forAccount(mDbxAcctMgr.getLinkedAccount());
			//Create the session folder.
			if(!dbxFs.isFolder(new DbxPath(infoDict.get(INFO.SESS_PATH))))
				dbxFs.createFolder(new DbxPath(infoDict.get(INFO.SESS_PATH)));
		} catch (Unauthorized e) {
			e.printStackTrace();
		} catch (InvalidPathException e) {
			e.printStackTrace();
		} catch (DbxException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Export to dropbox constructor
	 * @param patientName
	 * @param sdFolderPath
	 * @param sessionNum
	 */
	public DropboxClass(String patientName, String sdFolderPath, int sessionNum, DbxAccountManager DbxAcctMgr) {
		mDbxAcctMgr = DbxAcctMgr;
		infoDict.put(INFO.NAME, patientName);
		sdPath = sdFolderPath;
		infoDict.put(INFO.SESS_PATH, sdPath + "/" + infoDict.get(INFO.NAME) + "_sessions" + "/"); 
		number = sessionNum;
	}
	
	/**
	 * Exports all the patient data to the dropbox, using the given patient name. Need to have a flag that is 
	 * check that the export conditions are meet, Upper level needs to indicate to the user that this event is
	 * Occurring. 
	 */
	public void transferToDropBox() {
		new Thread(new Runnable() {
			public void run() {
				dbxSavePatientInfo();
				dbxSaveSessionData();
						//Need to through error message if unsuccessful.
			}			
		},"Transfer to Dropbox").start();
		
	}
	
	/**
	 * Saves the patient info to the dropbox in a .txt file
	 * Returns false if an error has occurred (no info on type of error)
	 * @return
	 */
	private boolean dbxSavePatientInfo() {		
		try {
			dbxFs = DbxFileSystem.forAccount(mDbxAcctMgr.getLinkedAccount());
			dbxPatientFilePath = new DbxPath(infoDict.get(INFO.NAME) + ".txt");
			Log.d("DROPBOX","DropBox Path:" + dbxPatientFilePath.toString());
			DbxFile dbxPatientInfoFile;
			if(!dbxFs.exists(dbxPatientFilePath)) {
				dbxPatientInfoFile = dbxFs.create(dbxPatientFilePath);
			} else {
				dbxPatientInfoFile = dbxFs.open(dbxPatientFilePath);
			}
			File tempPatientFile = new File(sdPath + File.separator + infoDict.get(INFO.NAME) + ".txt");
			Log.d("DROPBOX","SDCard Path:" + tempPatientFile.getAbsolutePath());
			dbxPatientInfoFile.writeFromExistingFile(tempPatientFile, false);
			dbxPatientInfoFile.close();
			return true;
		} catch (Unauthorized e) {
			e.printStackTrace();
			return false;
		} catch (InvalidPathException e) {
			e.printStackTrace();
			return false;
		} catch (DbxException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Saves that session data. Will create a session folder if needed, 
	 * Creates a session info file (.txt) and a session data file (.csv)
	 * Returns false if an error has occurred (No info on type of error passed)
	 * @return
	 */
	private boolean dbxSaveSessionData() {
		DbxFileSystem dbxFs;
		try {
			dbxFs = DbxFileSystem.forAccount(mDbxAcctMgr.getLinkedAccount());
			dbxFs.createFolder(new DbxPath(infoDict.get(INFO.NAME) + "_sessions"));
			DbxPath dbxSessionFilePath;
			DbxFile dbxSessionFile = null;
			File tempSessionFile;
			for(int i = 1; i < (number+1) ; i++) {
				//Session Info File
				dbxSessionFilePath = new DbxPath(infoDict.get(INFO.NAME) + "_sessions/session" + i + ".txt");
				if(!dbxFs.exists(dbxSessionFilePath)) {
					dbxSessionFile = dbxFs.create(dbxSessionFilePath);
					tempSessionFile = new File(infoDict.get(INFO.SESS_PATH) + "session" + i+ ".txt");
					dbxSessionFile.writeFromExistingFile(tempSessionFile, false);
					dbxSessionFile.close();
				}
				//Session Data File
				dbxSessionFilePath = new DbxPath(infoDict.get(INFO.NAME) + "_sessions/session" + i + ".csv");
				if(!dbxFs.exists(dbxSessionFilePath)) {
					dbxSessionFile = dbxFs.create(dbxSessionFilePath);
					tempSessionFile = new File(infoDict.get(INFO.SESS_PATH) + "session" + i+ ".csv");
					dbxSessionFile.writeFromExistingFile(tempSessionFile, false);
					dbxSessionFile.close();
				}
			}
			if (dbxSessionFile != null)
				dbxSessionFile.close();
			return true;
		} catch (Unauthorized e) {
			e.printStackTrace();
			return false;
		} catch (InvalidPathException e) {
			e.printStackTrace();
			return false;
		} catch (DbxException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Write the patient info to the patient file, updates the patients file 
	 * @param id
	 * @param dob
	 * @param gender
	 * @param notes
	 */
	public void writePatientInfo(String id, String dob, String gender, String notes) {
		infoDict.put(INFO.ID, id);
		infoDict.put(INFO.DOB, dob);
		infoDict.put(INFO.GENDER, gender);
		infoDict.put(INFO.ADDITIONAL, notes);
		callUpdatePatientFile();
	}
	
	/**
	 * Update the structure then call to update the patient file
	 * @param map patient info structure
	 */
	public void updatePatientDetails(Map<patientInfoMap.PATIENT_INFO, String> map) {
		//Add the new details to the map
		infoDict.put(INFO.ID, map.get(patientInfoMap.PATIENT_INFO.ID));
		infoDict.put(INFO.NAME, map.get(patientInfoMap.PATIENT_INFO.NAME));
		infoDict.put(INFO.DOB, map.get(patientInfoMap.PATIENT_INFO.DOB));
		infoDict.put(INFO.GENDER, map.get(patientInfoMap.PATIENT_INFO.GENDER));
		infoDict.put(INFO.ADDITIONAL, map.get(patientInfoMap.PATIENT_INFO.ADDITIONAL));
		infoDict.put(INFO.SESS_PATH, sdPath + "/" + infoDict.get(INFO.NAME) + "_sessions" + "/");
		callUpdatePatientFile();
	}
	
	/**
	 * Writes the patientInfo file. This will overwrite what ever is in the file. This means
	 * a delete of the patient file is not necessary.
	 */
	private void callUpdatePatientFile() {
		try {
			//Create or open the patient file.
			if(!dbxFs.exists(dbxPatientFilePath)) { //Check if the file already exists
				Log.d("DROPBOX", "File does not exist");
				dbxPatientFile = dbxFs.create(new DbxPath(infoDict.get(INFO.NAME) + ".txt"));
			}else {
				Log.d("DROPBOX", "File already exists, opening instead");
				dbxPatientFile = dbxFs.open(dbxPatientFilePath);
			}
			FileOperationsClass.updateSessionFile(dbxPatientFile.getWriteStream(), number, infoDict);
			dbxPatientFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @param type Type of swallow being performed
	 * @param timeCh1 Array with the time data for channel 1
	 * @param timeCh2 Array with the time data for channel 2 
	 * @param ampCh1 Array with the amplitude data for channel 1
	 * @param ampCh2 Array with the amplitude data for channel 2
	 * @return
	 */
	public boolean addSession(String type, double[] timeCh1, double[] timeCh2, double[] ampCh1, double[] ampCh2) {
		boolean success = false;
		Log.d("DEBUG", "Writing data");
		
		//Create the session info file.
		number++; //Add one to session count
		DbxPath sessionInfoPath = new DbxPath(infoDict.get(INFO.SESS_PATH) + "session" + number + ".txt");
		try {			
			DbxFile sessionInfo = dbxFs.create(sessionInfoPath);
			FileOperationsClass.addSessionInfo(type, sessionInfo.getWriteStream());
			success = true;
			sessionInfo.close();
		} catch (DbxException e) {
			success = false;
			e.printStackTrace();
		} catch (IOException e) {
			success = false;
			e.printStackTrace();
		}
		
		addSessionDataRowFormat(timeCh1, timeCh2, ampCh1, ampCh2); //Write the data
		callUpdatePatientFile(); //Update the session file to update the session count
		return success;
	}
	
	/**
	 * Adds the session data in a row format, This format creates four columns, and increments the rows.
	 * This allows for more data to be stored than incrementing the columns (going horizontally)
	 * @param timeCh1
	 * @param timeCh2
	 * @param ampCh1
	 * @param ampCh2
	 */
	private void addSessionDataRowFormat(double[] timeCh1, double[] timeCh2, double[] ampCh1, double[] ampCh2) {
		DbxPath sessionDataPath = new DbxPath(infoDict.get(INFO.SESS_PATH) + "session" + number + ".csv");
		try {
			DbxFile sessionData = dbxFs.create(sessionDataPath);
			FileOperationsClass.addSessionDataRowFormat(timeCh1, timeCh2, ampCh1, ampCh2, new OutputStreamWriter(sessionData.getWriteStream()));
			sessionData.close();
		} catch (DbxException e) {
			Log.d("DROPBOX", "Error writing csv file");
			e.printStackTrace();
		} catch (IOException e) {
			Log.d("DROPBOX", "Error writing csv file");
			e.printStackTrace();
		}
	}
	
	/**
	 * Gets the file path to the patient file
	 * @return String containing the file path to the patient file
	 */
	public String getFilePath() {
		return dbxPatientFilePath.toString();
	}
	
	/**
	 * Load the Patient Info file and extract the required information from it
	 */
	private void loadFile() {
		try {
			dbxFs = DbxFileSystem.forAccount(mDbxAcctMgr.getLinkedAccount());
			dbxPatientFile = dbxFs.open(dbxPatientFilePath);		
			infoDict = FileOperationsClass.loadFile(new InputStreamReader(dbxPatientFile.getReadStream()));
			number = Integer.parseInt(infoDict.get(INFO.SESS_NUM));
		} catch (Unauthorized e) {
			e.printStackTrace();
		} catch (DbxException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			dbxPatientFile.close();
		}
	}
	
	/**
	 * Return patient info
	 * @return ArrayList<String> of the patient info
	 */
	public ArrayList<String> getPatientInfo() {
		ArrayList<String> info = new ArrayList<String>();
		
		info.add(infoDict.get(INFO.ID));
		info.add(infoDict.get(INFO.NAME));
		info.add(infoDict.get(INFO.DOB));
		info.add(infoDict.get(INFO.GENDER));
		info.add(infoDict.get(INFO.ADDITIONAL));
		
		return info;
	}
	
	/**
	 * Gets the session info from the data structure and converts it into a string array
	 * @return
	 * @throws Exception
	 */
	public ArrayList<String[]> getAllSessionInfo() throws Exception {
		ArrayList<String[]> info = new ArrayList<String[]>();
		for (int i = 1; i < (number + 1); i ++) {
			info.add(getSessionInfo(i));
		}
		return info;
	}
	
	/**
	 * Gets the session info
	 * @param sess The session number
	 * @return
	 * @throws Exception
	 */
	public String[] getSessionInfo(int sess) throws Exception{
		if((sess > number) || (sess == 0)) {
			throw new IndexOutOfBoundsException("Session Number is out of bounds");
		}else {
			String path = infoDict.get(INFO.SESS_PATH) + "session" + sess + ".txt"; 
			return readSessInfoFile(path);
		}
	}
	
	/**
	 * Read the data from a text file and return the data in a string array
	 * @param filePath The file path to the file to be opened
	 * @return Each element in the array is a new line in the file
	 * @throws Exception
	 * TODO: Check first that the file exists+
	 */
	private String[] readSessInfoFile(String filePath) throws Exception{
		DbxFile testFile = null;
		dbxFs = DbxFileSystem.forAccount(mDbxAcctMgr.getLinkedAccount());
		testFile = dbxFs.open(new DbxPath(filePath));
		String[] info = FileOperationsClass.readSessInfoFile(new InputStreamReader(testFile.getReadStream()));
		testFile.close();
		return info;	
	}
	
	/**
	 * 
	 * @param session
	 * @return
	 * @throws IndexOutOfBoundsException
	 * @throws Exception
	 */
	public ArrayList<double[]> getSessionData(int session) throws IndexOutOfBoundsException, Exception {
		ArrayList<double[]> data = new ArrayList<double[]>();
		if((session > number) || (session == 0)) {
			throw new IndexOutOfBoundsException("Session Number is to large or Zero");
		}else {
			String sessionPath = infoDict.get(INFO.SESS_PATH) + "session" + session + ".csv";
			data = readSessionDataRowFormat(sessionPath);
			return data;
		}
	}
	
	/**
	 * Read the data in the specified file.
	 * @param path The dropbox file path (Needs to include extension)
	 * @return the data read from the file in the format time Ch1, Amp Ch1. time Ch2, Amp Ch2
	 * @throws Exception General Exception if file is not of the type .csv
	 */
	private ArrayList<double[]> readSessionDataRowFormat(String path) throws Exception {
		ArrayList<double[]> data = new ArrayList<double[]>();
		CSVReader csvReader = null;
		DbxFile testFile = null;
		//Check that file is a .csv
		if(!path.contains(".csv")) {
			throw new Exception("Incorrect File");
		} else {
			try {
				dbxFs = DbxFileSystem.forAccount(mDbxAcctMgr.getLinkedAccount());
				testFile = dbxFs.open(new DbxPath(path));
				FileInputStream input = testFile.getReadStream();
				data = FileOperationsClass.readSessionDataRowFormat(new InputStreamReader(input));				
			} catch (Unauthorized e) {
				e.printStackTrace();
			} catch (InvalidPathException e) {
				e.printStackTrace();
			} catch (DbxException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if(csvReader != null)
					try {
						csvReader.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				if(testFile != null) {
					testFile.close();
				}
			}
		
			return data;
		}
	}
	
	/**
	 * Checks to see if the patient file already exists
	 * @param dbxAccMgr
	 * @param name
	 * @return
	 * @throws InvalidPathException
	 * @throws DbxException
	 */
	public static boolean fileExists(DbxAccountManager dbxAccMgr, String name) throws InvalidPathException, DbxException {
		DbxFileSystem dbxFs;
			dbxFs = DbxFileSystem.forAccount(dbxAccMgr.getLinkedAccount());
			return dbxFs.exists(new DbxPath(name + ".txt"));
	}
	
	/**
	 * Return the number of sessions
	 * @return
	 */
	public int getNumSessions() {
		return number;
	}
}
