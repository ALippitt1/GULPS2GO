package com.bioimpedance;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class DropboxSaveFragment extends Fragment{

	private static final int SAVE_STATE = 1;
	private static final int LOAD_STATE = 2;
	private static final String KEY = "STATE";
	public static enum STATES {SAVE, LOAD};
	
	/**
	 * ATTENTION: Use this method instead of using a blank constructor. This allows you
	 * to set the display state. Acceptable inputs are SAVE_STATE and LOAD_STATE
	 * @param status
	 * @return
	 */
	public static DropboxSaveFragment newInstance(STATES status) {
		DropboxSaveFragment f = new DropboxSaveFragment();
		Bundle args = new Bundle();
		if (status == STATES.LOAD) {
			args.putInt(KEY, LOAD_STATE);
		} else if (status == STATES.SAVE){
			args.putInt(KEY, SAVE_STATE);
		}
		
		f.setArguments(args);
		return f;
	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.dropbox_save_fragment, container, false);
		Button load = (Button) v.findViewById(R.id.dbxLoadButton);
		Button save = (Button) v.findViewById(R.id.dbxSaveButton);
		
		//Changes the visibility of the buttons depending on the state	
		Bundle input = getArguments();
		int savedState = SAVE_STATE; //Set the default view as save
		if(input != null) {
			savedState = input.getInt(KEY);
		}
		if (savedState == LOAD_STATE) {
			save.setVisibility(View.INVISIBLE);
			load.setVisibility(View.VISIBLE);
		}else if (savedState == SAVE_STATE){
			load.setVisibility(View.INVISIBLE);
			save.setVisibility(View.VISIBLE);
		}
		return v;
	}
	
}
