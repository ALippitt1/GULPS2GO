package savingPackage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class patientInfoMap {

	public static enum PATIENT_INFO {ID, NAME, DOB, GENDER, ADDITIONAL};
	public static final int LENGTH = 5; //Max size of the enum 
	
	
	/**
	 * Puts all the values from an arraylist into the map. 
	 * Values are added in the order dictated by the order of the enum
	 * @param list
	 * @return
	 * @throws Exception
	 */
	public static Map<PATIENT_INFO, String> createMap(ArrayList<String> list) throws Exception{
		Map<PATIENT_INFO, String> infoDict = new HashMap<PATIENT_INFO, String>();
		
		if (list.size() != LENGTH) {
			throw new Exception();
		} else {
			infoDict.put(PATIENT_INFO.ID, list.get(0));
			infoDict.put(PATIENT_INFO.NAME, list.get(1));
			infoDict.put(PATIENT_INFO.DOB, list.get(2));
			infoDict.put(PATIENT_INFO.GENDER, list.get(3));
			infoDict.put(PATIENT_INFO.ADDITIONAL, list.get(4));
		}
		
		return infoDict;
	}
	
}
