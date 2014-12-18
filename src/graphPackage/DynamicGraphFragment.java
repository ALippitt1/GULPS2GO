package graphPackage;

import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.achartengine.GraphicalView;
import org.achartengine.tools.ZoomEvent;
import org.achartengine.tools.ZoomListener;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bioimpedance.R;

/**
 * Dynamic Graph fragment module, Add this to a app to have a dynamic graph
 * @author ajl157
 *
 */
public class DynamicGraphFragment extends Fragment {

	private static GraphicalView view;
	private LineGraph line;
	private static updateThread runnable;
	private static Thread thread;
	private Context appContext;
	public int dataset1;
	public int dataset2;
	private TextView pauseTitle;
	private TextView displayCoords;
	private double currTime = 0;
	private double scale = 10;
	private double lowerBound = 0; //Boundary values of the x-axis
	private double upperBound = scale;
	private double scaleStep = 0.25;
	private double[] yLimits = {4000, 0};
	//Holds the currently displayed data, used for pausing and then redisplaying
	private ArrayList<double[]> currentData1 = new ArrayList<double[]>();
	private ArrayList<double[]> currentData2 = new ArrayList<double[]>();
	
	//Thread message handler
	private Handler redrawHandle = new RedrawGraphHandler();
	
	//Bundle Keys
	private static final String TIME_CH1 = "TIME_CH1";
	private static final String AMP_CH1 = "AMP_CH1";
	private static final String TIME_CH2 = "TIME_CH2";
	private static final String AMP_CH2 = "AMP_CH2";
	private static final String ROTATION = "ROTATION";
	public static final int REDRAW = 1;
	
	//Exception Log String
	private static final String EXCP_LOG = "App_Exceptions";
	
	public static DynamicGraphFragment newInstance() {
		DynamicGraphFragment f = new DynamicGraphFragment();
		return f;
	}
 	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		runnable = new updateThread(redrawHandle);
		thread = new Thread(runnable,"Buffer Read");
		
		Log.d("DEBUGGING", "Start Tracing");
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		appContext = getActivity().getApplicationContext();
		
		View v = inflater.inflate(R.layout.gui_graph,container, false);
				
		displayCoords = (TextView) v.findViewById(R.id.txtDisplayPoint);
		pauseTitle = (TextView) v.findViewById(R.id.txtPaused);
		pauseTitle.setVisibility(View.INVISIBLE);
		
		line = new LineGraph();
		dataset1 = line.addDataSet("Channel 1", Color.GREEN);
		dataset2 = line.addDataSet("Channel 2", Color.BLUE);
		int indicator = line.addPointIndicator("Indicator", Color.RED);

		double[] range = {0.0, scale,0, 10};
		line.setInitialRange(range);
		
		view = line.getView(appContext);
		view.setOnClickListener(new GraphOnClickListener(displayCoords, line, indicator, view));
		view.setZoomRate(1f); //Setting to 1 will not change the zoom (taken care below)
		//Add Zoom Listener
		view.addZoomListener(new ZoomListener (){
			//Overwriting the built in zoom function by calculating the
			//new x-axis range. 
			//Note: Does not modify the y-axis
			public void zoomApplied(ZoomEvent zoom) {
				
				if(zoom.isZoomIn()) { //Zooming in
					if (!((scale - scaleStep) <= 1)) {
						scale = scale - scaleStep;
					}
				} else { //Zooming Out
					scale = scale + 2*scaleStep;
				}
				
				double t = currTime; // Current time
				lowerBound = t;
				upperBound = t + scale;
				double[] newRange = {lowerBound, upperBound, yLimits[0], yLimits[1]}; 
				line.updateRange(newRange);
			}

			public void zoomReset() {
				double t = currTime; // Current Time
				lowerBound = t - 5;
				upperBound = t + 5;
				double[] newRange = {lowerBound, upperBound, yLimits[0], yLimits[1]}; 
				line.updateRange(newRange);
			}
			
		}, true, true); //End of zoom listener
		
		RelativeLayout layout = (RelativeLayout) v.findViewById(R.id.chart_layout);		
		layout.addView(view);
		
		if(savedInstanceState != null) {
			//Read the saved data points and re-plot them
			double[] time1 = savedInstanceState.getDoubleArray(TIME_CH1);
			double[] time2 = savedInstanceState.getDoubleArray(TIME_CH2);
			double[] amp1 = savedInstanceState.getDoubleArray(AMP_CH1);
			double[] amp2 = savedInstanceState.getDoubleArray(AMP_CH2);
			for(int i = 0; i < time1.length; i++) {
				line.addNewPoint(new CustomPoint(time1[i], amp1[i]), dataset1);
			}
			for(int i = 0; i < time2.length; i++) {
				line.addNewPoint(new CustomPoint(time2[i], amp2[i]), dataset2);
			}
			if(savedInstanceState.getBoolean(ROTATION)) {
				rotateGraph();
			}
				
		}
		Log.d("RESUME", "Graph View Created");
		return v;
	}

	@Override
	public void onResume() {
		super.onResume();
		startGraph();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		runnable.stopThread();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	
	@Override
	public void onSaveInstanceState(Bundle out) {
		Log.d("RESUME", "DynamicGraphFragment Saving State");
		//Save the current data points.
		ArrayList<double[]> set1 = line.returnCurrentDataSet(dataset1);
		ArrayList<double[]> set2 = line.returnCurrentDataSet(dataset2);
		double[] time1 = new double[set1.size()];
		double[] amp1 = new double[set1.size()];
		double[] time2 = new double[set2.size()];
		double[] amp2 = new double[set2.size()];
		
		for(int i = 0; i < set1.size(); i++) {
			time1[i] = set1.get(i)[0];
			amp1[i] = set1.get(i)[1];
		}
		for(int i = 0; i < set2.size(); i++) {
			time2[i] = set2.get(i)[0];
			amp2[i] = set2.get(i)[1];
		}
		out.putDoubleArray(TIME_CH1, time1);
		out.putDoubleArray(TIME_CH2, time2);
		out.putDoubleArray(AMP_CH1, amp1);
		out.putDoubleArray(AMP_CH2, amp2);
		
		//Save the orientation of the graph
		out.putBoolean(ROTATION, line.isRotated());
		super.onSaveInstanceState(out);
	}

	/**
	 * Called by the higher level to start the thread.
	 */
	public void startGraph() {
		if (!runnable.getAlive()) {
			thread.start();
		}
		else {
			runnable.resumeThread();
		}
		changePauseTitle(false);
	}

	/**
	 * Called by the higher level to pause the thread
	 */
	public void pauseGraph() {
		try {
			runnable.pauseThread();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Log.d("PAUSING", "PAUSING GRAPH");
		//Clear the currently plotted data.
		currentData1 = line.returnCurrentDataSet(dataset1);
		currentData2 = line.returnCurrentDataSet(dataset2);
		line.clearData(dataset1);
		line.clearData(dataset2);
		//Get the all the data recorded
		ArrayList<double[]> data1 = line.returnEntireDataSet(dataset1);
		ArrayList<double[]> data2 = line.returnEntireDataSet(dataset2);
		changePauseTitle(true);
		updateGraph(data1, data2);
	}
	
	/**
	 * Clears the data stored and the display. Should be called before starting a new
	 * graph.
	 */
	public void restartGraph() {
		//Remove all previous data held.
		Log.d("RESUME", "RestartGraph");
		line.clearArrayListData(dataset1);
		line.clearArrayListData(dataset2);
		line.clearData(dataset1);
		line.clearData(dataset2);
		scale = 10;
		currTime = 0;
		lowerBound = 0;
		upperBound = scale;
		yLimits[0] = 4000;
		yLimits[1] = 0;
		double[] newRange = {lowerBound, upperBound, yLimits[0], yLimits[1]};
		line.updateRange(newRange);
		view.repaint();
		if (!runnable.getAlive()) {
			thread.start();
		}
		else {
			runnable.resumeThread();
		}
	}
	
	
	public void resumeGraph() {
		changePauseTitle(false);
		double[] range = {lowerBound, upperBound, yLimits[0], (yLimits[1] + 0.1*yLimits[1])};
		line.updateRange(range);
		updateGraph(currentData1, currentData2);
		if (!runnable.getAlive()) {
			thread.start();
		}
		else {
			runnable.resumeThread();
			
		}
	}
	
	private void updateGraph(ArrayList<double[]> data1, ArrayList<double[]> data2) {
		CustomPoint p;
		for (int i = 0; (i < data1.size()); i++) {
			p = new CustomPoint(data1.get(i)[0], data1.get(i)[1]);
			line.addNewPoint(p, dataset1, true);
		}
		for (int i = 0; (i < data2.size()); i++) {
			p = new CustomPoint(data2.get(i)[0], data2.get(i)[1]);
			line.addNewPoint(p, dataset2, true);
		}
		view.repaint(); //Note: need to give time for the fragment to be created.
		
	}
	
	/**
	 * Called by the upper level to write to the data buffer
	 */
	public void writeData(int data, double time, int set) {
		if (set == dataset1){
			runnable.writeQueue1(data, time);
		}else if(set == dataset2) {
			runnable.writeQueue2(data, time);
		}
	}
	
	/**
	 * Clear the buffer defended by set
	 * @param set
	 */
	public void clearBuffer(int set) {
		if (set == dataset1){
			runnable.clearQueue1();
		}else if(set == dataset2) {
			runnable.clearQueue2();
		}
	}
		
	/**
	 * Read the buffer defended by set
	 * @param set
	 * @return
	 */
	public ArrayList<double[]> readData(int set) {
		Log.d("SAVING_DATA", "Graph Called");
		return line.returnEntireDataSet(set);
	}
	
	/**
	 * Rotate the graph
	 */
	public void rotateGraph() {
		line.rotateGraph(getActivity().getResources().getConfiguration().orientation);
	}

	/**
	 * Changes the visibility of the pause title 
	 * @param state
	 */
	private void changePauseTitle(boolean state) {
		if(state) {
			pauseTitle.setVisibility(View.VISIBLE);
		} else {
			pauseTitle.setVisibility(View.INVISIBLE);
		}
	}
	
	/**
	 * Returns true if it is paused
	 * @return
	 */
	public boolean isPaused() {
		if (pauseTitle.getVisibility() == View.VISIBLE)
			return true;
		else 
			return false;
	}
	
	/**
	 * Thread class to implement the update of the graph. Includes pause of
	 * thread and resuming. (taken from:
	 * http://stackoverflow.com/questions/11989589
	 * /how-to-pause-and-resume-a-thread-in-java-from-another-thread)
	 * 
	 * @author ajl157
	 * 
	 */
	private class updateThread implements Runnable {

		private Object GUI_INITIALIZATION_MONITOR = new Object();
		private boolean pauseThreadFlag = false;
		private boolean run = true;
		private boolean alive = false;
		private ConcurrentLinkedQueue<Integer> queueData1;
		private ConcurrentLinkedQueue<Integer> queueData2;
		private ConcurrentLinkedQueue<Double> timeData1;
		private ConcurrentLinkedQueue<Double> timeData2;
		private Handler mhandle;
		
		private double expandPercent = 0.05;
		private double upperIncrease = 0.1;
		private double lowerIncrease = 0.15;
		
		
		public updateThread(Handler handle) {
			queueData1 = new ConcurrentLinkedQueue<Integer>();
			queueData2 = new ConcurrentLinkedQueue<Integer>();
			timeData1 = new ConcurrentLinkedQueue<Double>();
			timeData2 = new ConcurrentLinkedQueue<Double>();
			mhandle = handle;
		}
		
		public void run() {
			
	        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
			CustomPoint p = null;
			CustomPoint p2 = null;			
			
				
			while(!Thread.currentThread().isInterrupted() && run) {
				alive = true;
				checkForPaused();
				//Read from data buffer 1 and add the point to first graph dataset
				try {
					p = new CustomPoint(timeData1.remove(),queueData1.remove());
					line.addNewPoint(p, dataset1);
					currTime = p2.getX();
					double y1 = p.getY();
					// Test if the y-axis needs to be expanded.  
					if(y1 > (yLimits[1] - expandPercent*yLimits[1])) {
						yLimits[1] = y1 + upperIncrease*y1;
						line.updateRange(new double[] {lowerBound, upperBound,yLimits[0], yLimits[1]});
					}else if(y1 < (yLimits[0] + expandPercent*yLimits[0])) {
						//Currently keep the range starting at zero for Y-axis
						yLimits[0] = y1 - lowerIncrease*y1;
						line.updateRange(new double[] {lowerBound, upperBound, yLimits[0], yLimits[1]});
						
					}
				}catch(NoSuchElementException  e){
					Log.e(EXCP_LOG, "exception", e);
				}
				try { //Read from data buffer 2 and add the point to second graph dataset
					p2 = new CustomPoint(timeData2.remove(),queueData2.remove());
					line.addNewPoint(p2, dataset2);
					currTime = p2.getX();
					double y1 = p2.getY();
					// Test if the y-axis needs to be expanded.
					if(y1 > (yLimits[1] - expandPercent*yLimits[1])) {
						yLimits[1] = y1 + upperIncrease*y1;
						line.updateRange(new double[] {lowerBound, upperBound,yLimits[0], yLimits[1]});
					}else if(y1 < (yLimits[0] + expandPercent*yLimits[0])) {
						yLimits[0] = y1 - lowerIncrease*y1;
						line.updateRange(new double[] {lowerBound, upperBound, yLimits[0], yLimits[1]});
					}
				}catch(NoSuchElementException  e){
					Log.e(EXCP_LOG, "exception", e);
				}
							
				mhandle.obtainMessage(REDRAW).sendToTarget();
				
				if(currTime >= upperBound) {
					lowerBound = currTime;
					upperBound = upperBound + scale;
					yLimits[0] = 4000;
					yLimits[1] = 0;
					double[] range = {lowerBound, upperBound, yLimits[0], yLimits[1]};
					line.updateRange(range);
					line.clearData(dataset1);
					line.clearData(dataset2);
				} 
			}
			alive = false;
			Log.d("RESUME","Thread Graph Stop");
		}

		public void writeQueue1(int data, double time) {
			queueData1.offer(data);
			timeData1.offer(time);
		}
		
		public void writeQueue2(int data, double time) {
			queueData2.offer(data);
			timeData2.offer(time);
		}
		
		public void clearQueue1() {
			if (queueData1 != null)
				queueData1.clear();
		}
		
		public void clearQueue2() {
			if (queueData2 != null)
				queueData2.clear();
		}
		
		/**
		 * Called to check if thread is paused
		 */
		private void checkForPaused() {
			synchronized (GUI_INITIALIZATION_MONITOR) {
				while (pauseThreadFlag) {
					try {
						GUI_INITIALIZATION_MONITOR.wait();
					} catch (Exception e) {
					}
				}
			}
		}

		/**
		 * Called made to resume the thread if paused
		 */
		public void resumeThread() {
			synchronized (GUI_INITIALIZATION_MONITOR) {
				pauseThreadFlag = false;
				GUI_INITIALIZATION_MONITOR.notify();
			}
		}

		/**
		 * Call made to pause the thread if running
		 * 
		 * @throws InterruptedException
		 */
		public void pauseThread() throws InterruptedException {
			Log.d("RESUME", "pauseThread()");
			pauseThreadFlag = true;
		}
		
		public void stopThread() {
			run = false;
		}
		
		public boolean getAlive() {
			return alive;
		}
	}
	
	public static double min(Object[] o) {
        double m = Integer.MAX_VALUE;
        for (Object a : o) {
            m = Math.min(m, (Double) a);
        }
        return m;
    }

    public static double max(Object[] o) {
        double m = Integer.MIN_VALUE;
        for (Object a : o) {
            m = Math.max(m, (Double) a);
        }
        return m;
    }
    
    /**
     * Make a call to this when the graph needs to be repainted.
     * @author ajl157
     *
     */
    private static class RedrawGraphHandler extends Handler {
    	
    	@Override
    	public void handleMessage(Message msg) {
    		switch(msg.what){
    		case REDRAW:
    			view.repaint();    			
    		}
    	}
    }
    
}

