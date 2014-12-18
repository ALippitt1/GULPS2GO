package graphPackage;

import java.util.ArrayList;
import java.util.Arrays;

import org.achartengine.GraphicalView;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bioimpedance.R;

public class StaticGraphFragment extends Fragment{

	private static GraphicalView view;
	private LineGraph line;
	public int dataSet1;
	public int dataSet2;
	public int indicator;
	private double[] initRange = {0,0,Double.MAX_VALUE,0};
	
	//Bundle Keys
	private static final String TIME_CH1 = "TIME_CH1";
	private static final String TIME_CH2 = "TIME_CH2";
	private static final String AMP_CH1 = "AMP_CH1";
	private static final String AMP_CH2 = "AMP_CH2";
	private static final String ROTATE = "ROTATE";
	
	public static StaticGraphFragment newInstance() {
		StaticGraphFragment f = new StaticGraphFragment();
		return f;		
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View v = inflater.inflate(R.layout.gui_graph,container, false);
		
		TextView displayCoOrds = (TextView) v.findViewById(R.id.txtDisplayPoint);
		
		line = new LineGraph();		
		dataSet1 = line.addDataSet("Channel 1", Color.GREEN);
		dataSet2 = line.addDataSet("Channel 2", Color.BLUE);
		indicator = line.addPointIndicator("Indicator", Color.RED);
		
		// Create the plot view
		view = line.getView(getActivity().getApplicationContext());
		view.setOnClickListener(new GraphOnClickListener(displayCoOrds, line, indicator, view));
		
		RelativeLayout layout = (RelativeLayout) v.findViewById(R.id.chart_layout);
		layout.addView(view);
		
		//Initialize the display range
		initRange[0] = 0;
		initRange[1] = 0;
		initRange[2] = Double.MAX_VALUE;
		initRange[3] = 0;
		
		// If reloading the page, get the currently displayed plot, and draw it
		if(savedInstanceState != null) {
			Log.d("RESUME", "Static savedState");
			double[] amp1 = savedInstanceState.getDoubleArray(AMP_CH1);
			double[] time1 = savedInstanceState.getDoubleArray(TIME_CH1);
			double[] amp2 = savedInstanceState.getDoubleArray(AMP_CH2);
			double[] time2 = savedInstanceState.getDoubleArray(TIME_CH2);
			
			passData(amp1, time1, dataSet1);
			passData(amp2, time2, dataSet2);
			
			if (savedInstanceState.getBoolean(ROTATE)) {
				rotateGraph();
			}
		}
				
		return v;
	}
	
	/**
	 * Save the currently displayed plot
	 */
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState)  {
		ArrayList<double[]> set1 = line.returnEntireDataSet(dataSet1);
		ArrayList<double[]> set2 = line.returnEntireDataSet(dataSet2);
		double[] time1 = new double[set1.size()];
		double[] amp1 = new double[set1.size()];
		double[] time2 = new double[set2.size()];
		double[] amp2 = new double[set2.size()];
		//Splits the data into separate arrays to be stored into a bundle
		for(int i = 0; i < set1.size(); i++) {
			time1[i] = set1.get(i)[0];
			amp1[i] = set1.get(i)[1];
		}
		for(int i = 0; i < set2.size(); i++) {
			time2[i] = set2.get(i)[0];
			amp2[i] = set2.get(i)[1];
		}
		
		savedInstanceState.putDoubleArray(AMP_CH1, amp1);
		savedInstanceState.putDoubleArray(AMP_CH2, amp2);
		savedInstanceState.putDoubleArray(TIME_CH1, time1);
		savedInstanceState.putDoubleArray(TIME_CH2, time2);
		savedInstanceState.putBoolean(ROTATE, line.isRotated());
		
		super.onSaveInstanceState(savedInstanceState);
	}

		
	/**
	 * Adds the new data to the plot. Calculates the axes range to 
	 * display and redraws the plot 
	 * @param data array of y values
	 * @param time array of x values
	 * @param set either StaticGraphFragment.dataSet1 or StaticGraphFragment.dataSet2
	 */
	public void passData(double[] data, double[] time, int set) {
		//Log.d("STATIC", "Adding data");
		CustomPoint p;
		if(set == dataSet1) {
			for (int i = 0; (i < data.length && i < time.length); i++) {
				p = new CustomPoint(time[i],data[i]);
				line.addNewPoint(p, dataSet1);
				
			}
		} else if(set == dataSet2) {
			for (int i = 0; (i < data.length && i < time.length); i++) {
				p = new CustomPoint(time[i],data[i]);
				line.addNewPoint(p, dataSet2);
			}
		}
		
		
		//Update the display range
		//Xmax
		// Display the first ten seconds
		initRange[0] = 0;
		initRange[1] = 10;
		//Ymin
		// Calculate the Y-axis range
		double temp;
		temp = calcMin(data);
		if(temp < initRange[2]) {
			initRange[2] = temp;
		}
		//Ymax
		temp = calcMax(data);
		if(temp > initRange[3]) {
			initRange[3] = temp;
		}
		
		line.setInitialRange(initRange);
		
		new Thread(new Runnable() {
			public void run() {
				view.repaint(); 
			}
		}, "Static Graph Redraw").start();
	}
	
	/**
	 * Returns the data drawn on the plot
	 * @param set
	 * @return
	 */
	public ArrayList<double[]> getDataSet(int set) {
		return line.returnEntireDataSet(set);
	}
	
	/**
	 * Clears the data in the plot
	 * @param set
	 */
	public void clearGraphData(int set) {
		line.clearData(set);
	}
	
	/**
	 * Rotates the plot, calls the function in LineGraph.java 
	 */
	public void rotateGraph() {
		line.rotateGraph(getActivity().getResources().getConfiguration().orientation);
	}
	
	/**
	 * Determines the maximum value in the array
	 * @param array
	 * @return the max value of the array
	 */
	private double calcMax(double[] array) {
		double [] temp = array;
		Arrays.sort(temp);
		double max = temp[temp.length-1];
		return max;
	}
	
	/**
	 * Determines the minimum value in the array
	 * @param array
	 * @return the min value in the array
	 */
	private double calcMin(double[] array) {
		double [] temp = array;
		Arrays.sort(temp);
		double min = temp[0];
		return min;
	}
	
}
