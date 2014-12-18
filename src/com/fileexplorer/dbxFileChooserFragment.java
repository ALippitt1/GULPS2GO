package com.fileexplorer;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.bioimpedance.R;
import com.dropbox.sync.android.DbxAccountManager;
import com.dropbox.sync.android.DbxException;
import com.dropbox.sync.android.DbxException.Unauthorized;
import com.dropbox.sync.android.DbxFileInfo;
import com.dropbox.sync.android.DbxFileSystem;
import com.dropbox.sync.android.DbxPath;
import com.dropbox.sync.android.DbxPath.InvalidPathException;

public class dbxFileChooserFragment extends ListFragment{

	private static FileChooserListener mCallback;

	private final static String APP_KEY = "yp5ziveogr422nm";
	private final static String APP_SECRET = "1xbtm5uqi2f4jgu";
	private DbxAccountManager mDbxAcctMgr;
	private DbxFileSystem dbxFs;
	private int folderCounter = 0;

	private Context appContext;
	private FileArrayAdapter adapter;

	public static dbxFileChooserFragment newInstance() {
		dbxFileChooserFragment f = new dbxFileChooserFragment();
		return f;
	}

	// Container Activity must implement this interface
	// Used to pass data to the top level
	public interface FileChooserListener {
		public void dbxFileSelected(String filePath); //This requires implementation e.g. what to pass
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		appContext = getActivity().getApplicationContext();
		Log.d("LOAD_DBX","frag onCreate");
		mDbxAcctMgr = DbxAccountManager.getInstance(appContext, APP_KEY, APP_SECRET);
		try {
			dbxFs = DbxFileSystem.forAccount(mDbxAcctMgr.getLinkedAccount());
			List<DbxFileInfo> dbxList = dbxFs.listFolder(new DbxPath("/"));
			Log.d("LOAD_DBX","Size is: " + dbxList.size());
			fill(dbxList);
		} catch (Unauthorized e) {
			e.printStackTrace();
		} catch (DbxException e) {
			e.printStackTrace();
		}

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
		View v = inflater.inflate(R.layout.gui_save, container, false);
		TextView titleText = (TextView) v.findViewById(R.id.titleString);
		Button saveButton = (Button) v.findViewById(R.id.btSave);
		Button switchButton = (Button) v.findViewById(R.id.btDbxFiles);
		titleText.setText(R.string.saveLoadHeader);
		saveButton.setVisibility(View.INVISIBLE);
		switchButton.setText(R.string.saveButton1);
		return v;
	}

	@Override
	public void onResume() {
		super.onResume();
		refreshDisplay(); //refresh display everytime this window is brought up
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

	private void fill(List<DbxFileInfo> dbxList) {
		List<DbxFileInfo> dirs = dbxList;
		List<Item>dir = new ArrayList<Item>();
		List<Item>fls = new ArrayList<Item>();
		try{
			for (DbxFileInfo ff: dirs) { 
				Date lastModDate = ff.modifiedTime;
				DateFormat formater = DateFormat.getDateTimeInstance();
				String date_modify = formater.format(lastModDate);
				DbxPath dbxPath = ff.path;
				if(ff.isFolder) {
					List<DbxFileInfo> fbuf = dbxFs.listFolder(ff.path);

					int buf = 0;
					if(fbuf != null) {
						buf = fbuf.size();
					}
					String num_item = String.valueOf(buf);
					if(buf == 0) {
						num_item = num_item + " item";
					}else {
						num_item = num_item + " items";
					}
					dir.add(new Item(dbxPath.getName(), num_item,date_modify,dbxPath.toString(),"directory_icon"));
				}
				else {//Here is where the ignore certain files needs to go.
					if (dbxPath.getName().contains(".txt")) { //Only add the files that are .txt files
						fls.add(new Item(dbxPath.getName(), ff.size + " Byte",date_modify,dbxPath.toString(), "file_icon"));
					}
				}
			}
		}catch(DbxException.NotFound e) {
			e.printStackTrace();
		}catch(DbxException.InvalidParameter e) {
			e.printStackTrace();
		}catch(DbxException e) {
			e.printStackTrace();
		}catch(Exception e) {
			e.printStackTrace();
		}
		Collections.sort(dir);
		Collections.sort(fls);
		dir.addAll(fls);
		Log.d("LOAD_DBX", "Counter = " + folderCounter);
		if(folderCounter != 0) {
			dir.add(0,new Item("..","Back","","","directory_up"));
		}
		adapter = new FileArrayAdapter(appContext, R.layout.file_explorer_layout, dir);
		setListAdapter(adapter);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Item o = adapter.getItem(position);
		//If a folder or the back button is pressed.
		if(o.getImage().equalsIgnoreCase("directory_icon")||o.getImage().equalsIgnoreCase("directory_up")) {
			try {
				if(o.getImage().equalsIgnoreCase("directory_icon")) {
					folderCounter = 1;
				}else {
					folderCounter = 0;
				}
				//Update the view with the new display contents 
				fill(dbxFs.listFolder(new DbxPath(o.getPath())));	
			} catch (InvalidPathException e) {
				e.printStackTrace();
			} catch (DbxException e) {
				e.printStackTrace();
			}
		}
		else
		{
			onFileClick(o);
		}
	}

	private void onFileClick(Item o) {
		String filePath = o.getPath(); 
		mCallback.dbxFileSelected(filePath);
	}
	
	public void refreshDisplay() {
		try {
			dbxFs = DbxFileSystem.forAccount(mDbxAcctMgr.getLinkedAccount());
			List<DbxFileInfo> dbxList = dbxFs.listFolder(new DbxPath("/"));
			for (int i = 0; i < dbxList.size(); i++) {
			}
			fill(dbxList);
		} catch (Unauthorized e) {
			e.printStackTrace();
		} catch (DbxException e) {
			e.printStackTrace();
		}
	}

}
