package com.bioimpedance;

import graphPackage.DynamicGraphFragment;

import java.lang.reflect.Field;
import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


public class NewSessionFragment extends Fragment{

	private DynamicGraphFragment graph = DynamicGraphFragment.newInstance();
	private Button buttonStart;
	private Button buttonResume;
	private Button buttonPause;
	
	
	public static NewSessionFragment newInstance() {
		NewSessionFragment f = new NewSessionFragment();
		return f;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.new_session, container, false);
		buttonStart = (Button) v.findViewById(R.id.newSessStart);
		buttonResume = (Button) v.findViewById(R.id.newSessResume);
		buttonPause = (Button) v.findViewById(R.id.newSessPause);
		buttonResume.setVisibility(View.GONE);
		
		if (savedInstanceState != null) {
			graph = (DynamicGraphFragment) getChildFragmentManager().findFragmentById(R.id.newSessionGraphFragment);				
		} else {
			FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
			transaction.add(R.id.newSessionGraphFragment, graph).commit();
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
	
	public void startGraph() {
		graph.startGraph();
		buttonStart.setVisibility(View.GONE);
		
	}
	
	public void pauseGraph() {
		buttonPause.setVisibility(View.GONE);
		buttonResume.setVisibility(View.VISIBLE);
		graph.pauseGraph();
	}
	
	public void resumeGraph() {
		buttonPause.setVisibility(View.VISIBLE);
		buttonResume.setVisibility(View.GONE);
		graph.resumeGraph();
	}
	
	public void restartGraph() {
		buttonPause.setVisibility(View.VISIBLE);
		buttonResume.setVisibility(View.GONE);
		graph.startGraph();
		
	}
	
	public boolean isPaused() {
		//Log.d("RESUME","New Sess Calling is Pause: " + graph.isPaused());
		return graph.isPaused();
	}
	
	public void writeDataBuffer1(int data, double time) {
		graph.writeData(data, time, graph.dataset1);
	}
	
	public void writeDataBuffer2(int data, double time) {
		graph.writeData(data, time, graph.dataset2);
	}
	
	public void clearQueues() {
		graph.clearBuffer(graph.dataset1);
		graph.clearBuffer(graph.dataset2);
	}
	
	public ArrayList<double[]> getData() {
		graph.pauseGraph();
		ArrayList<double[]> dataCh1 = graph.readData(graph.dataset1);
		ArrayList<double[]> dataCh2 = graph.readData(graph.dataset2);
		ArrayList<double[]> dataCombined = new ArrayList<double[]>();
		Log.d("SAVING_DATA", "Frag Called");
		//Find the length of the shortest arraylist to use in the for loop
		//The shortest one (ch2) will be short by 1.
		int length = 0;
		if (dataCh1.size() <= dataCh2.size())
			length = dataCh1.size();
		else
			length = dataCh2.size();
		
		for(int i = 0; i < length; i++) {
			double[] temp = {dataCh1.get(i)[0], dataCh1.get(i)[1], dataCh2.get(i)[0], dataCh2.get(i)[1]};
			dataCombined.add(temp);
		}
		graph.resumeGraph();
		return dataCombined;
	}
	
	public void rotateGraph() {
		graph.rotateGraph();
	}
}
