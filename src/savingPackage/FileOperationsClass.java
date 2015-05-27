package savingPackage;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.text.format.Time;
import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

import com.dropbox.sync.android.DbxException;
import com.dropbox.sync.android.DbxException.Unauthorized;

public class FileOperationsClass {

	private static String[] headerStrings = {"ID: ","Name: ","Date of Birth: ","Gender: ",
			"Additional Notes: ","Session Path: ","Number of Sessions: "};
	public static enum INFO {ID, NAME, DOB, GENDER, ADDITIONAL, SESS_PATH, SESS_NUM};

	private static final int SESS_INFO_SIZE = 3;
	
	private static Map<INFO, String> infoDict = new HashMap<FileOperationsClass.INFO, String>();

	/**
	 * Each line in the file is separated by \n character. 
	 * @param inReader dbxFile = InputStreamReader, File = FileReader
	 * @return
	 * @throws Exception
	 */
	public static String[] readSessInfoFile(Reader inReader) throws Exception{

		StringBuilder text = new StringBuilder();
		BufferedReader br = new BufferedReader(inReader);
		String line;
		String[] info = new String[SESS_INFO_SIZE];
		int count = 0;
		while((line = br.readLine()) != null) {
			text.append(line);
			text.append('\n');
			info[count] = line;
			count++;
		}
		br.close();
		return info;	
	}

	/**
	 * This should be called at the end. Once you have made multiple sessions.
	 * Deletes and replaces the old file. 
	 * @param sess
	 * @param sessionNum
	 * @param infoDict
	 */
	public static void updateSessionFile(FileOutputStream sess, int sessionNum, Map<INFO, String> infoDict) {

		FileOutputStream out = sess;
		if(out != null) {
			try {
				out.write(new String(headerStrings[0] + infoDict.get(INFO.ID) + "\n").getBytes());
				out.write(new String(headerStrings[1] + infoDict.get(INFO.NAME) + "\n").getBytes());
				out.write(new String(headerStrings[2] + infoDict.get(INFO.DOB) + "\n").getBytes()); 
				out.write(new String(headerStrings[3] + infoDict.get(INFO.GENDER) + "\n").getBytes());
				out.write(new String(headerStrings[4] + infoDict.get(INFO.ADDITIONAL) + "\n").getBytes());
				out.write(new String(headerStrings[5] + infoDict.get(INFO.SESS_PATH) + "\n").getBytes());
				out.write(new String(headerStrings[6] + Integer.toString(sessionNum)  + "\n").getBytes());
				out.close();
			} catch(Exception e) {
			}//End of writing
		}
	}

	/**
	 * Adds the information to sessionfile
	 * @param type
	 * @param timeCh1
	 * @param timeCh2
	 * @param ampCh1
	 * @param ampCh2
	 * @param out FileOutputStream to the required file
	 */
	public static void addSessionInfo(String type, FileOutputStream outStream) {
		FileOutputStream out = null;

		//The time class is a faster replacement for the calendar class
		//Reference: http://stackoverflow.com/questions/5369682/android-get-current-time-and-date
		Time now = new Time(); 
		now.setToNow();

		int day = now.monthDay;
		int month = now.month;
		int year = now.year;
		int hour = now.hour;
		int min = now.minute;
		int sec = now.second;

		String date = new String("Date: " + day + "/" + month + "/" + year + "\n");
		String time = new String("Time: " + hour + ":" + min + ":" + sec + "\n");

		out = outStream; 
		if(out !=null) {
			try {
				out.write(date.getBytes());
				out.write(time.getBytes());
				out.write(new String("Type of Swallow: " + type + "\n").getBytes());
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}		
		}
	}

	/**
	 * Write the session data to the passed outputStream
	 * @param timeCh1
	 * @param timeCh2
	 * @param ampCh1
	 * @param ampCh2
	 * @param out FileOutputStream to the required file
	 */
	public static void addSessionDataRowFormat(double[] timeCh1, double[] timeCh2, double[] ampCh1, double[] ampCh2, Writer outWrite) {
		CSVWriter writer = new CSVWriter(outWrite);
		String[] temp = new String[4]; //temporary array to hold the data then storing it in an array
		if (writer != null) {
			for (int i = 0; (i < timeCh1.length && i < timeCh2.length); i++) {
				temp[0] = Double.toString(timeCh1[i]);
				temp[1] = Double.toString(ampCh1[i]);
				temp[2] = Double.toString(timeCh2[i]);
				temp[3] = Double.toString(ampCh2[i]);
				writer.writeNext(temp);
			}
		}
		try {
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Writes the data to a csv file. This is named session + current session number
	 * @param timeCh1
	 * @param timeCh2
	 * @param ampCh1
	 * @param ampCh2
	 * @return
	 */
	public static void addSessionDataColumnFormat(double[] timeCh1, double[] timeCh2, double[] ampCh1, double[] ampCh2, Writer outWrite) {
		CSVWriter writer = new CSVWriter(outWrite);
			//Convert the double array inputs to string arrays and store
			//them in a list. This list is then passed to the CSV writer
			
			List<String[]> stringList = new ArrayList<String[]>();
			String[] data = new String[timeCh1.length];
			for(int i = 0; i < timeCh1.length; i++) {
				data[i] = Double.toString(timeCh1[i]);
			}
			stringList.add(data);
			
			String[] data2 = new String[ampCh1.length];
			for(int i = 0; i < ampCh1.length; i++) {
				data2[i] = Double.toString(ampCh1[i]);
			}
			stringList.add(data2);
			
			String[] data3 = new String[timeCh2.length];
			for(int i = 0; i < timeCh2.length; i++) {
				data3[i] = Double.toString(timeCh2[i]);
			}
			stringList.add(data3);
			
			String[] data4 = new String[ampCh2.length];
			for(int i = 0; i < ampCh2.length; i++) {
				data4[i] = Double.toString(ampCh2[i]);
			}
			stringList.add(data4);
			writer.writeAll(stringList);
			try {
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
	}
	
	
	/**
	 * Reads the session data from a known path file. Must be a csv file.
	 * Throws a exception if the path does not contain the ".csv" string
	 * @param path
	 * @return
	 * @throws Exception 
	 */
	public static ArrayList<double[]> readSessionDataColumnFormat(Reader inReader) {
		String[] row = null;
		CSVReader csvReader = null;
		ArrayList<double[]> stuff = new ArrayList<double[]>();
		List<?> content;

		try {
			csvReader = new CSVReader(inReader);
			content = csvReader.readAll();
			for (Object object : content) {
				row = (String[]) object;
				double[] data = new double[row.length];
				for(int i = 0; i < row.length; i++) {
					data[i] = Double.parseDouble(row[i]);
				}
				stuff.add(data);
			}
			csvReader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return stuff;
	}

	public static ArrayList<double[]> readSessionDataRowFormat(Reader inReader) {
		String[] row = null;
		CSVReader csvReader = null;
		ArrayList<double[]> data = new ArrayList<double[]>();
		List<?> content;

		try {
			csvReader = new CSVReader(inReader);
			content = csvReader.readAll();
			double[] t1 = new double[content.size()];
			double[] t2 = new double[content.size()];
			double[] a1 = new double[content.size()];
			double[] a2 = new double[content.size()];
			int i = 0;
			for (Object object : content) {
				row = (String[]) object;
				t1[i] = Double.parseDouble(row[0]);
				a1[i] = Double.parseDouble(row[1]);
				t2[i] = Double.parseDouble(row[2]);
				a2[i] = Double.parseDouble(row[3]);
				i++;
			}

			data.add(t1);
			data.add(a1);
			data.add(t2);
			data.add(a2);

			csvReader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return data;
	}
	
	/**
	 * Load the Patient Info file and extract the required information from it
	 * Reference: http://stackoverflow.com/questions/12421814/how-to-read-text-file-in-android
	 * By Sandip Armal Patil
	 */
	public static Map<INFO, String> loadFile(Reader in) {
		try {			
			BufferedReader br = new BufferedReader(in);
			String line;
			while((line = br.readLine()) != null) {
				extractInfo(line);
			}
			br.close();
		} catch (Unauthorized e) {
			e.printStackTrace();
		} catch (DbxException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return infoDict;
	}
		
	/**
	 * Match the string to the information structure and write to the structure. 
	 * (Similar to the same method in the SaveLoadClass.java)
	 * @param line This parameter will be tested to see if it has any of the header strings, if
	 * it does the information following will be saved.
	 */
	private static void extractInfo(String line) {
		if(line.contains(headerStrings[0])) { //ID
			infoDict.put(INFO.ID,line.replace(headerStrings[0], ""));
		}else if(line.contains(headerStrings[1])) { //Name
			infoDict.put(INFO.NAME,line.replace(headerStrings[1], ""));
		}else if(line.contains(headerStrings[2])) { //DOB
			infoDict.put(INFO.DOB,line.replace(headerStrings[2], ""));
		}else if(line.contains(headerStrings[3])) { //Gender
			infoDict.put(INFO.GENDER,line.replace(headerStrings[3], ""));
		}else if(line.contains(headerStrings[4])) { // Additional Notes
			infoDict.put(INFO.ADDITIONAL,line.replace(headerStrings[4], ""));
		}else if(line.contains(headerStrings[5])) { //SessionInfo Path
			//Dropbox uses different file path to the one in file. 
			infoDict.put(INFO.SESS_PATH, infoDict.get(INFO.NAME) + "_sessions/");
		}else if(line.contains(headerStrings[6])) { //Number of sessions
			infoDict.put(INFO.SESS_NUM, line.replace(headerStrings[6],""));
		}
	}
}
