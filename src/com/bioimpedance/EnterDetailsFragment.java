package com.bioimpedance;

import java.util.ArrayList;
import java.util.Map;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.extraDisplayFragments.CustomDatePickerFragment;
import savingPackageNew.patientInfoMap;

public class EnterDetailsFragment extends Fragment{

	private static final int DIALOG_FRAGMENT = 1;
	
	private static final String ID = "ID";
	private static final String NAME = "NAME";
	private static final String DOB = "DOB";
	private static final String GENDER = "GENDER";
	private static final String NOTES = "NOTES";
	
	private Spinner spinnerGender;
	private EditText editID, editName, editNotes;
	private Button setDate;
	private String genderText;
	private String dob;
	private CustomDatePickerFragment date = new CustomDatePickerFragment();
	
	public static EnterDetailsFragment newInstance() {
		EnterDetailsFragment f = new EnterDetailsFragment();
		return f;
	}
	
	/**
	 * Used to initialize the fields. This will be used when editing a current patients details
	 * Currently cannot default the gender field
	 */
	public static EnterDetailsFragment newInstance(Map<patientInfoMap.PATIENT_INFO, String> info) {
		EnterDetailsFragment f = new EnterDetailsFragment();
		Bundle bundle = new Bundle();
		bundle.putString(ID, info.get(patientInfoMap.PATIENT_INFO.ID));
		bundle.putString(NAME, info.get(patientInfoMap.PATIENT_INFO.NAME));
		bundle.putString(DOB, info.get(patientInfoMap.PATIENT_INFO.DOB));
		bundle.putString(GENDER, info.get(patientInfoMap.PATIENT_INFO.GENDER));
		bundle.putString(NOTES, info.get(patientInfoMap.PATIENT_INFO.ADDITIONAL));
		f.setArguments(bundle);		
		return f;		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.enter_details, container, false);
		
		setDate = (Button) v.findViewById(R.id.datePickerButton);
		setDate.setBackgroundColor(Color.TRANSPARENT);
		
		spinnerGender = (Spinner) v.findViewById(R.id.spinnerGender);
		spinnerGender.setOnItemSelectedListener(new CustomOnItemSelectedListener());
		
		editID = (EditText) v.findViewById(R.id.editID);
		editName = (EditText) v.findViewById(R.id.editName);
		editNotes = (EditText) v.findViewById(R.id.editNotes);
		
		Bundle input = getArguments();
		if (input != null) {
			editID.setText(input.getString(ID));
			editName.setText(input.getString(NAME));
			setDate.setText(input.getString(DOB));
			dob = input.getString(DOB); //initialize the DOB string as it only gets written to on click events
			editNotes.setText(input.getString(NOTES));
		}
				
		return v;
	}
	
	/**
	 * Custom listener class for the spinner
	 * @author ajl157
	 *
	 */
	private class CustomOnItemSelectedListener implements OnItemSelectedListener {

		public void onItemSelected(AdapterView<?> parent, View view, int pos,
				long id) {
			genderText = parent.getItemAtPosition(pos).toString();
		}

		public void onNothingSelected(AdapterView<?> parent) {
			
		}
	}
	
	/**
	 * Returns the information from the fields. Does not check if one field is empty
	 * 
	 */
	public Map<patientInfoMap.PATIENT_INFO, String> getPatientInfo() {
		ArrayList<String> info = new ArrayList<String>();
		
		info.add(editID.getText().toString());
		info.add(editName.getText().toString());
		info.add(dob);
		info.add(genderText);
		info.add(editNotes.getText().toString());
		try {
			return patientInfoMap.createMap(info);
		}catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public void setDate() {
		
		FragmentTransaction ft = getActivity()
				.getSupportFragmentManager().beginTransaction();
		Fragment prev = getActivity().getSupportFragmentManager()
				.findFragmentByTag("DatePicker");
		if (prev != null) { // Checks the fragment isn't already
							// created.
			ft.remove(prev);
		}
		ft.addToBackStack(null);
		ft.commit();

		date.setTargetFragment(EnterDetailsFragment.this,
				DIALOG_FRAGMENT);
		date.show(getChildFragmentManager().beginTransaction(),
				"DatePicker");
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch(requestCode) {
		case DIALOG_FRAGMENT:
			Log.d("DATE", "Button Pressed");
			switch(resultCode) {
			case CustomDatePickerFragment.RESULT_SELECT:
				dob = Integer.toString(date.getDay()) + "/" + Integer.toString(date.getMonth() + 1) + "/" + Integer.toString(date.getYear());
				setDate.setText(dob);
				break;
			case CustomDatePickerFragment.RESULT_CANCEL:
				break;
			}
			break;
		}
	}
}
