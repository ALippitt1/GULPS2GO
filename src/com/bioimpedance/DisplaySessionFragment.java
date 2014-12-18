package com.bioimpedance;

import graphPackage.StaticGraphFragment;

import java.lang.reflect.Field;
import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.extraDisplayFragments.SelectDisplayList;

public class DisplaySessionFragment extends Fragment{
	
	StaticGraphFragment graphStatic = StaticGraphFragment.newInstance();
	TextView displaySessNum, displaySessType, displaySessDate, displaySessTime;
	
	public static DisplaySessionFragment newInstance() {
		DisplaySessionFragment f = new DisplaySessionFragment();
		return f;
	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.display_loaded_sessions, container, false);
		
		displaySessNum = (TextView) v.findViewById(R.id.displaySessionNumber);
		displaySessType = (TextView) v.findViewById(R.id.displaySessionType);
		displaySessDate = (TextView) v.findViewById(R.id.displaySessionDate);
		displaySessTime = (TextView) v.findViewById(R.id.displaySessionTime);
		
		if(savedInstanceState != null) {
			Log.d("RESUME", "Static ");
			graphStatic = (StaticGraphFragment) getChildFragmentManager().findFragmentById(R.id.staticGraphFragment);
		} else {
			FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
			transaction.add(R.id.staticGraphFragment, graphStatic).commit();
		}
		return v;
	}
		
	/**
	 * Fixes a bug in the ChildFragmentManager. This need to be called by every fragment that uses 
	 * getChildFragmentManager. Code taken from:
	 * http://stackoverflow.com/questions/15207305/getting-the-error-java-lang-illegalstateexception-activity-has-been-destroyed/15656428#15656428
	 */
	@Override
	public void onDetach() {
	    super.onDetach();
	    try {
	        Field childFragmentManager = Fragment.class.getDeclaredField("mChildFragmentManager");
	        childFragmentManager.setAccessible(true);
	        childFragmentManager.set(this, null);

	    } catch (NoSuchFieldException e) {
	        throw new RuntimeException(e);
	    } catch (IllegalAccessException e) {
	        throw new RuntimeException(e);
	    }
	}
	
	public void chooseSession(ArrayList<String> names, FragmentManager fm) {
		SelectDisplayList list = SelectDisplayList.newInstance(6,"Pick a session", names);
		list.show(fm, "Select_display_list");
	}
	
	public void addData(double[] data1, double[] data2, double[] time1, double[] time2) {
		graphStatic.clearGraphData(graphStatic.dataSet1);
		graphStatic.clearGraphData(graphStatic.dataSet2);
		graphStatic.passData(data1, time1, graphStatic.dataSet1);
		graphStatic.passData(data2, time2, graphStatic.dataSet2);
		
	}
	
	public void addDetails(String[] info) {
		displaySessNum.setText(info[0]);
		displaySessType.setText(info[3]);
		displaySessDate.setText(info[1]);
		displaySessTime.setText(info[2]);
	}
	
	public void rotateGraph() {
		graphStatic.rotateGraph();
	}
}
