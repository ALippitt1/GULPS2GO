package com.extraDisplayFragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;

import com.bioimpedance.R;

public class CustomDatePickerFragment extends DialogFragment {

	DatePicker date;
	public static final int RESULT_SELECT = 1;
	public static final int RESULT_CANCEL = 0;
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	    // Get the layout inflater
	    LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.date_picker_fragment, null);
        builder.setView(view);
                
        date = (DatePicker) view.findViewById(R.id.customDatePicker);
        builder.setTitle("Pick a date:");
               
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				Log.d("DATE", "Date is: " + date.getDayOfMonth() + "/" + date.getMonth() + "/" + date.getYear());
				getTargetFragment().onActivityResult(
						getTargetRequestCode(), RESULT_SELECT,
						getActivity().getIntent());
			}
		});
			
        
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				getTargetFragment().onActivityResult(
						getTargetRequestCode(), RESULT_CANCEL,
						getActivity().getIntent());
			}
		});
		
        return builder.create();
    }

	public int getDay() {
		return date.getDayOfMonth();
	}
	
	public int getMonth() {
		return date.getMonth();
	}
	
	public int getYear() {
		return date.getYear();
	}

}