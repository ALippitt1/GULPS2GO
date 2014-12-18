package savingPackageNew;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import savingPackageNew.FileOperationsClass.INFO;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import com.extraDisplayFragments.SelectDisplayList;


/**
 * File saving class on the sdcard.
 * @author ajl157
 *
 */
public class SDCardClass {

	private File patientInfoFile;
	private File sessionFile;
	private File sessionInfoFile;
	private String filePath; //File path to the containing folder
	private int sessionNum = 0;
	
	private Map<FileOperationsClass.INFO, String> infoDict = new HashMap<FileOperationsClass.INFO, String>();
	
	/******************************************************************************************/
	/**                            Constructors                                              **/
	/******************************************************************************************/

	/**
	 * Used for loading files
	 * @param path
	 */
	public SDCardClass(String path) {
		loadFile(path);
	}
	
	/**
	 * Create a new file system. Used when saving new patient
	 * @param path
	 * @param name
	 */
	public SDCardClass(String path, String patientName) {
		filePath = path;
		infoDict.put(INFO.NAME,patientName);
		String completePath = filePath + File.separator + infoDict.get(INFO.NAME) + ".txt"; //Contains the file path and file name
		patientInfoFile = new File(completePath);
		infoDict.put(INFO.SESS_PATH,filePath + File.separator + infoDict.get(INFO.NAME) + "_sessions" + File.separator); //Add the patient session path
		new File(infoDict.get(INFO.SESS_PATH)).mkdirs(); //Make the folder to store sessions in.
		sessionInfoFile = new File(infoDict.get(INFO.SESS_PATH) + "session" + sessionNum + ".txt");
		sessionFile = new File(infoDict.get(INFO.SESS_PATH) + "session" + sessionNum + ".csv");
	}
	
	/******************************************************************************************/
	/*                           Save Functions                                               */
	/******************************************************************************************/

	/**
	 * Writes to the patient info file. Returns true if write was successful.
	 * @param id
	 * @param name
	 * @param dob
	 * @param gender
	 * @param notes
	 * @return
	 */
	public void writePatientInfo(String id, String dob, String gender, String notes) {
		//Map information
		infoDict.put(INFO.ID, id);
		infoDict.put(INFO.DOB, dob);
		infoDict.put(INFO.GENDER, gender);
		infoDict.put(INFO.ADDITIONAL, notes);
		callUpdateSessionFile();
	}
	
	/**
	 * Creates a new session info text file. Pass in a string with the
	 * type of swallow being performed.
	 * @param type
	 * @return
	 */
	public boolean addSession(String type, double[] timeCh1, double[] timeCh2, double[] ampCh1, double[] ampCh2) {
		boolean success = false;
		FileOutputStream sess = null;

		incrementSession();

		try {
			success = true;
			sess = new FileOutputStream(sessionInfoFile.getPath(), true);//true for appending if file is created add to the end
			FileOperationsClass.addSessionInfo(type, sess);
		} catch (FileNotFoundException e) {
			success = false;
			e.printStackTrace();
		} 

		new Thread(new saveThread(timeCh1, timeCh2, ampCh1, ampCh2), "SDCard Save").start();
		callUpdateSessionFile();

		return success;
	}
	
	/**
	 * Increments the session file pointer to the next file.
	 * Note: Doesn't actually create the new file.
	 */
	private void incrementSession() {
		sessionNum ++;
		sessionInfoFile = new File(infoDict.get(INFO.SESS_PATH) + "session" + sessionNum + ".txt");
		sessionFile = new File(infoDict.get(INFO.SESS_PATH) + "session" + sessionNum + ".csv");
	}
	
	/**
	 * Writes the data to a csv file. This is named session + current session number
	 * @param timeCh1
	 * @param timeCh2
	 * @param ampCh1
	 * @param ampCh2
	 * @return
	 */
	public boolean addSessionDataColumnFormat(double[] timeCh1, double[] timeCh2, double[] ampCh1, double[] ampCh2) {
		boolean success = false;

		try {
			success = true;
			FileOperationsClass.addSessionDataColumnFormat(timeCh1, timeCh2,ampCh1,ampCh2, new FileWriter(sessionFile.getPath()));
		} catch (IOException e) {
			success = false;
			e.printStackTrace();
		}
		return success;
	}

	/**
	 * New method of saving the data by writing it in rows rather than columns
	 * @param timeCh1
	 * @param timeCh2
	 * @param ampCh1
	 * @param ampCh2
	 */
	public void addSessionDataRowFormat(double[] timeCh1, double[] timeCh2, double[] ampCh1, double[] ampCh2) {
		try {
			FileOperationsClass.addSessionDataRowFormat(timeCh1, timeCh2, ampCh1, ampCh2, new FileWriter(sessionFile.getPath()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * This should be called at the end. Once you have made multiple sessions.
	 * Deletes and replaces the old file. 
	 */
	private void callUpdateSessionFile() {
		
		//Delete the old file. (Can't replace individual lines)
		patientInfoFile.delete();
		//Create a new file
		String completePath = filePath + File.separator + infoDict.get(INFO.NAME) + ".txt"; //Contains the file path and file name
		patientInfoFile = new File(completePath);
		
		
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(patientInfoFile.getPath(), true);//true for appending if file is created add to the end
			FileOperationsClass.updateSessionFile(out, sessionNum, infoDict);
		} catch (FileNotFoundException e) {
		} //End of try FileOutputStream
		
	}
	
	
	public void updatePatientDetails(Map<patientInfoMap.PATIENT_INFO, String> map) {
		//Add the new details to the map
		infoDict.put(INFO.ID, map.get(patientInfoMap.PATIENT_INFO.ID));
		infoDict.put(INFO.NAME, map.get(patientInfoMap.PATIENT_INFO.NAME));
		infoDict.put(INFO.DOB, map.get(patientInfoMap.PATIENT_INFO.DOB));
		infoDict.put(INFO.GENDER, map.get(patientInfoMap.PATIENT_INFO.GENDER));
		infoDict.put(INFO.ADDITIONAL, map.get(patientInfoMap.PATIENT_INFO.ADDITIONAL));
		String tempPath = filePath + File.separator + infoDict.get(INFO.NAME) + "_sessions" + File.separator;
		infoDict.put(INFO.SESS_PATH, tempPath);
		callUpdateSessionFile();
	}
	
	/******************************************************************************************/
	/*                            Load Functions                                              */
	/******************************************************************************************/
	
	/**
	 * Reads the session data from a known path file. Must be a csv file.
	 * Throws a exception if the path does not contain the ".csv" string
	 * @param path
	 * @return
	 * @throws Exception 
	 */
	public ArrayList<double[]> readSessionDataColumnFormat(String path) throws Exception {
		if(!path.contains(".csv")) {
			throw new Exception("Incorrect File");
		}
		else {
			return FileOperationsClass.readSessionDataColumnFormat(new FileReader(path));
		}
	}
	
	public ArrayList<double[]> readSessionDataRowFormat(String path) throws Exception {
		
		if(!path.contains(".csv")) {
			throw new Exception("Incorrect File");
		}
		else {
				return FileOperationsClass.readSessionDataRowFormat(new FileReader(path));
			}
	}
	
	/**
	 * Loads the patient info file and reads from it.
	 * Will Probably need to revist the exception handling
	 * 
	 * @param path
	 */
	private void loadFile(String path) {
		//filePath = path;
		patientInfoFile = new File(path);
		filePath = patientInfoFile.getParent();//Get the path to the containing folder
		try {
			infoDict = FileOperationsClass.loadFile(new FileReader(patientInfoFile));
			sessionNum = Integer.parseInt(infoDict.get(INFO.SESS_NUM));
			//Next line required because differs from dropbox
			infoDict.put(INFO.SESS_PATH,filePath + File.separator + infoDict.get(INFO.NAME) + "_sessions"+ File.separator);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	/******************************************************************************************/
	/*                              Get Functions                                             */
	/******************************************************************************************/
	
	/**
	 * Returns the number of Sessions
	 * Note: session number starts at 1;
	 * @return
	 */
	public int getNumSessions() {
		return sessionNum;
	}
	
	public String getFileLocation() {
		return patientInfoFile.getParent();
	}
	
	public String getFilePath() {
		return patientInfoFile.getPath();
	}
	
	/**
	 * Returns an arraylist with the data depending on the session.
	 * Throws an IndexOutOfBoundsException if the session number is too large 
	 * Passes the exception thrown by readSessionData up. 
	 * @param session
	 * @return
	 * @throws Exception 
	 */
	public ArrayList<double[]> getSessionData(int session) throws IndexOutOfBoundsException, Exception {
		ArrayList<double[]> data = new ArrayList<double[]>();
		//Might need to be sessionNum - 1
		if((session > sessionNum) || (session == 0)) {
			throw new IndexOutOfBoundsException("Session Number is to large or Zero");
		}else {
			String path = infoDict.get(INFO.SESS_PATH) + "session" + session + ".csv";
			data = readSessionDataRowFormat(path);
			return data;
		}
		
	}
	
	/**
	 * Returns information about each session. 
	 * @return
	 * @throws Exception 
	 */
	public ArrayList<String[]> getAllSessionInfo() throws Exception {
		ArrayList<String[]> info = new ArrayList<String[]>();
		for (int i = 1; i < (sessionNum + 1); i ++) {
			info.add(getSessionInfo(i));
		}
		return info;
	}
	
	/**
	 * Get session info depending on the passed integer
	 * 
	 * @param sess
	 * @return
	 */
	public String[] getSessionInfo(int sess) throws Exception{
		if((sess > sessionNum) || (sess == 0)) {
			throw new IndexOutOfBoundsException("Session Number is out of bounds");
		}else {
			String path = infoDict.get(INFO.SESS_PATH) + "session" + sess + ".txt"; 
			return readSessInfoFile(path);
		}
	}
	
	/**
	 * Reads from the specified file.
	 * @param filePath 
	 * @return information from the session file
	 * @throws Exception
	 */
	private String[] readSessInfoFile(String filePath) throws Exception{
		File file = new File(filePath); //Get the text file
		return FileOperationsClass.readSessInfoFile(new FileReader(file));	
	}
	
	public ArrayList<String> getPatientInfo() {
		ArrayList<String> info = new ArrayList<String>();
		
		info.add(infoDict.get(INFO.ID));
		info.add(infoDict.get(INFO.NAME));
		info.add(infoDict.get(INFO.DOB));
		info.add(infoDict.get(INFO.GENDER));
		info.add(infoDict.get(INFO.ADDITIONAL));
		
		return info;
	}
	
	
	/******************************************************************************************/
	/*                           Static Functions                                             */
	/******************************************************************************************/
	
	/**
	 * Checks if external storage is available and can be written to.
	 * returns: [is Available; is Writable]
	 * @return
	 */
	public static boolean[] checkExternalStorage() {
		boolean mExternalStorageAvailable = false;
		boolean mExternalStorageWriteable = false;
		String state = Environment.getExternalStorageState();
		
		if (Environment.MEDIA_MOUNTED.equals(state)) {
		    // We can read and write the media
		    mExternalStorageAvailable = mExternalStorageWriteable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
		    // We can only read the media
		    mExternalStorageAvailable = true;
		    mExternalStorageWriteable = false;
		} else {
		    // Something else is wrong. It may be one of many other states, but all we need
		    //  to know is we can neither read nor write
		    mExternalStorageAvailable = mExternalStorageWriteable = false;
		}
		boolean[] status = {mExternalStorageAvailable, mExternalStorageWriteable};
		return status;
	}
	
	/**
	 * Check if the file exists
	 * @param appContext
	 * @return
	 */
	public static boolean checkFileExists(String filePath) {
		boolean exists = true;
		try {
			new FileInputStream (new File(filePath));
		}catch(FileNotFoundException e) {
			exists = false;
		}
		return exists;
	}

	public static void displaySessions(FragmentActivity Activity, ArrayList<String> names, String title) {
		SelectDisplayList display = SelectDisplayList.newInstance(1, title, names);
		FragmentManager fm = Activity.getSupportFragmentManager();
		display.show(fm, "Dialog Box");
	}
	
	private class saveThread implements Runnable{

		double[] timeCh1, timeCh2, ampCh1, ampCh2;
		private saveThread(double[] t1, double[] t2, double[] a1, double[] a2) {
			timeCh1 = t1;
			timeCh2 = t2;
			ampCh1 = a1;
			ampCh2 = a2;
		}
		
		public void run() {
			addSessionDataRowFormat(timeCh1, timeCh2, ampCh1, ampCh2);
		}
		
	}
	
}
