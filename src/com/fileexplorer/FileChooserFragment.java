package com.fileexplorer;

import java.io.File;
import java.sql.Date;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.bioimpedance.R;



//Reference: http://custom-android-dn.blogspot.co.nz/2013/01/create-simple-file-explore-in-android.html
public class FileChooserFragment extends ListFragment{
	private static FileChooserListener mCallback;
	
	// Container Activity must implement this interface
	// Used to pass data to the top level
	public interface FileChooserListener {
		public void fileSelected(String filePath); //This requires implementation e.g. what to pass
	}
	
	public static enum STATES {SAVE, LOAD};
	private static final int SAVE_STATE = 1;
	private static final int LOAD_STATE = 2;
	private static final String KEY = "STATE";
	
	private Bundle input;
	private STATES currState = STATES.SAVE;
	private File currentDir;
	private FileArrayAdapter adapter;
	private Context appContext;
	private TextView filePathDisplay;
	private TextView titleText;
	private Button saveButton;
	
	
	
	/**
	 * ATTENTION: Use this method instead of using a blank constructor. This allows you
	 * to set the display state. Acceptable inputs are SAVE_STATE and LOAD_STATE
	 * @param status
	 * @return
	 */
	public static FileChooserFragment newInstance(STATES status) {
		FileChooserFragment f = new FileChooserFragment();
		Bundle args = new Bundle();
		if (status == STATES.LOAD) {
			args.putInt(KEY, LOAD_STATE);
		} else if (status == STATES.SAVE){
			args.putInt(KEY, SAVE_STATE);
		}
		
		f.setArguments(args);
		return f;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		appContext = getActivity().getApplicationContext();
		currentDir = new File(Environment.getExternalStorageDirectory().getPath()); //Get the path to the sdcard
		fill(currentDir);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
		View v = inflater.inflate(R.layout.gui_save, container, false);
		filePathDisplay = (TextView) v.findViewById(R.id.textfilePath);
		titleText = (TextView) v.findViewById(R.id.titleString);
		saveButton = (Button) v.findViewById(R.id.btSave);
		updateFilePath(currentDir.getPath());
		Button switchButton = (Button) v.findViewById(R.id.btDbxFiles);
		switchButton.setText(R.string.saveButton2);
		
		
		//Gets the arguments from the passed bundle. Checks it is not null,
		//then changes the display depending on the argument.
		input = getArguments();
		int savedState = SAVE_STATE; //Set the default view as save
		if(input != null) {
			savedState = input.getInt(KEY);
		}
		if (savedState == LOAD_STATE) {
			changeDisplay(LOAD_STATE);
			currState = STATES.LOAD;
		}else if (savedState == SAVE_STATE){
			changeDisplay(SAVE_STATE);
			currState = STATES.SAVE;
		}
		return v;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		refreshDisplay();//refresh display everytime this window is brought up
	}
	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception.
        try {
            mCallback = (FileChooserListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
	}
	
	/**
	 * Change the view of the fragment depending on the state
	 * @param state
	 */
	private void changeDisplay(int state) {
		if (state == SAVE_STATE) {
			titleText.setText(R.string.saveSaveHeader);
			saveButton.setVisibility(View.VISIBLE);
		} else if (state == LOAD_STATE) {
			titleText.setText(R.string.saveLoadHeader);
			saveButton.setVisibility(View.INVISIBLE);
		}
	}

	private void fill(File f) {
		File[] dirs = f.listFiles();
		List<Item>dir = new ArrayList<Item>();
		List<Item>fls = new ArrayList<Item>();
		try{
			for (File ff: dirs) { 
				Date lastModDate = new Date(ff.lastModified());
				DateFormat formater = DateFormat.getDateTimeInstance();
				String date_modify = formater.format(lastModDate);
				if(ff.isDirectory()) {
					File[] fbuf = ff.listFiles();
					int buf = 0;
					if(fbuf != null) {
						buf = fbuf.length;
					}
					else buf = 0;
					String num_item = String.valueOf(buf);
					if(buf == 0) {
						num_item = num_item + " item";
					}else {
						num_item = num_item + " items";
					}
					dir.add(new Item(ff.getName(), num_item,date_modify,ff.getAbsolutePath(),"directory_icon"));
				}
				else {//Here is where the ignore certain files needs to go.
					if (currState == STATES.LOAD) { //Only add files in the load state
						if (ff.getName().contains(".txt")) { //Only add the files that are .txt files 
							fls.add(new Item(ff.getName(), ff.length() + " Byte",date_modify,ff.getAbsolutePath(), "file_icon"));
						}
					}
				}
			}
		}catch(Exception e) {
			//Exception uncaught
		}
		Collections.sort(dir);
		Collections.sort(fls);
		dir.addAll(fls);
		//prevent from adding a back button if at lowest level
		if((!f.getName().equalsIgnoreCase("storage"))|| (!f.getName().equalsIgnoreCase("sdcard"))) {
			dir.add(0,new Item("..","Back","",f.getParent(),"directory_up"));
		}
		adapter = new FileArrayAdapter(appContext, R.layout.file_explorer_layout, dir);
		setListAdapter(adapter);
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Item o = adapter.getItem(position);
		if(o.getImage().equalsIgnoreCase("directory_icon")||o.getImage().equalsIgnoreCase("directory_up")) {
            currentDir = new File(o.getPath());
            fill(currentDir);
            updateFilePath(o.getPath());
		}
		else
		{
		    onFileClick(o);
		}
	}
	
	private void onFileClick(Item o) {
		String filePath = o.getPath(); 
		mCallback.fileSelected(filePath);
	}
	
	private void updateFilePath(String path) {
		filePathDisplay.setText(path);
		 
	}
	
	/**
	 * Get file path of current location
	 * @return
	 */
	public String getFilePath() {
		return currentDir.getPath();
	}

	public void refreshDisplay() {
		fill(currentDir);
	}
	
	public STATES getState() {
		return currState;
	}
}
