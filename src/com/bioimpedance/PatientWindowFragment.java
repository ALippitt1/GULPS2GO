package com.bioimpedance;

import java.util.Map;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import savingPackage.patientInfoMap;

public class PatientWindowFragment extends Fragment{
	
	private Button disconnect;
	private Button connectDropBox;
	private static boolean disconnectState = false;
	private TextView textID, textName, textDOB, textGender, textNotes;
	private static final String ID = "ID";
	private static final String NAME = "NAME";
	private static final String DOB = "DOB";
	private static final String GENDER = "GENDER";
	private static final String NOTES = "NOTES";
	private static final String STATUS = "STATUS"; 
	
	
	public static PatientWindowFragment newInstance(Map<patientInfoMap.PATIENT_INFO, String> info) {
		PatientWindowFragment f = new PatientWindowFragment();
		Bundle bundle = new Bundle();
		bundle.putString(ID, info.get(patientInfoMap.PATIENT_INFO.ID));
		bundle.putString(NAME, info.get(patientInfoMap.PATIENT_INFO.NAME));
		bundle.putString(DOB, info.get(patientInfoMap.PATIENT_INFO.DOB));
		bundle.putString(GENDER, info.get(patientInfoMap.PATIENT_INFO.GENDER));
		bundle.putString(NOTES, info.get(patientInfoMap.PATIENT_INFO.ADDITIONAL));
		f.setArguments(bundle);		
		return f;
	}
	
	public static PatientWindowFragment newInstance(Map<patientInfoMap.PATIENT_INFO, String> info, boolean status) {
		PatientWindowFragment f = new PatientWindowFragment();
		Bundle bundle = new Bundle();
		bundle.putString(ID, info.get(patientInfoMap.PATIENT_INFO.ID));
		bundle.putString(NAME, info.get(patientInfoMap.PATIENT_INFO.NAME));
		bundle.putString(DOB, info.get(patientInfoMap.PATIENT_INFO.DOB));
		bundle.putString(GENDER, info.get(patientInfoMap.PATIENT_INFO.GENDER));
		bundle.putString(NOTES, info.get(patientInfoMap.PATIENT_INFO.ADDITIONAL));
		disconnectState = status;
		f.setArguments(bundle);		
		return f;
	}
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.patient_window, container, false);
		
		disconnect = (Button) v.findViewById(R.id.disconnectBT);
		connectDropBox = (Button) v.findViewById(R.id.exportDropBox);
		textID = (TextView) v.findViewById(R.id.viewID);
		textName = (TextView) v.findViewById(R.id.viewName);
		textDOB = (TextView) v.findViewById(R.id.viewDOB);
		textGender = (TextView) v.findViewById(R.id.viewGender);
		textNotes = (TextView) v.findViewById(R.id.viewNotes);
		
		Bundle input = getArguments();
		if (input != null) {
			textID.setText(input.getString(ID));
			textName.setText(input.getString(NAME));
			textDOB.setText(input.getString(DOB));
			textGender.setText(input.getString(GENDER));
			textNotes.setText(input.getString(NOTES));
		}
		return v;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		disconnectButton(disconnectState);
	}
	
	public void onSavedInstanceState(Bundle outState){
		super.onSaveInstanceState(outState);
		outState.putBoolean(STATUS, disconnectState);
	}
		
	public void disconnectButton(boolean visible) {
		disconnectState = visible;
		if(visible) {
			disconnect.setVisibility(View.VISIBLE);
		}else
			disconnect.setVisibility(View.INVISIBLE);
	}
	
	public void setPatientDetails(Map<patientInfoMap.PATIENT_INFO, String> info) {
		textID.setText(info.get(patientInfoMap.PATIENT_INFO.ID));
		textName.setText(info.get(patientInfoMap.PATIENT_INFO.NAME));
		textDOB.setText(info.get(patientInfoMap.PATIENT_INFO.DOB));
		textGender.setText(info.get(patientInfoMap.PATIENT_INFO.GENDER));
		textNotes.setText(info.get(patientInfoMap.PATIENT_INFO.ADDITIONAL));
	}
	
	public boolean getBTState() {
		return disconnectState;
	}
	
	public void exportDropBoxEnable(boolean status) {
		if(status) {
			connectDropBox.setVisibility(View.VISIBLE);
		}else {
			connectDropBox.setVisibility(View.INVISIBLE);
		}
			
	}
}
