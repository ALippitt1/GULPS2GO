package graphPackage;

import java.util.ArrayList;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.LineChart;
import org.achartengine.chart.PointStyle;
import org.achartengine.chart.ScatterChart;
import org.achartengine.chart.XYChart;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYValueSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer.Orientation;
import org.achartengine.renderer.XYSeriesRenderer;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.util.Log;

public class LineGraph {

	private GraphicalView view;
	private XYChart chart;
	private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();
	private XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
	private ArrayList<String> typesList = new ArrayList<String>();

	private static final int ARRAY_SIZE = 10;

	private double[] range = { 0, ARRAY_SIZE, -1, 12 };

	private ArrayList<GraphingData> dataArray = new ArrayList<GraphingData>();

	public LineGraph() {
		initGraphHoriz();
		this.setInitialRange(range);
	}

	/**
	 * Initialize the Graph dimensions, title, colours and zoom
	 */
	private void initGraphHoriz() {
		mRenderer.setZoomButtonsVisible(true);
		mRenderer.setXTitle("Time (sec)");
		mRenderer.setYTitle("Amplitude");  
		mRenderer.setYAxisMin(0);
		mRenderer.setXAxisMin(0);
		mRenderer.setZoomEnabled(true, true);
		mRenderer.setPointSize(10);
		mRenderer.setApplyBackgroundColor(true);
		mRenderer.setBackgroundColor(Color.TRANSPARENT);
		mRenderer.setMarginsColor(Color.argb(0x00, 0x01, 0x01, 0x01)); //Set the margins to be transparent
		mRenderer.setAxesColor(Color.BLACK);
		mRenderer.setLabelsColor(Color.BLACK);
		mRenderer.setXLabelsColor(Color.BLACK);
		mRenderer.setYLabelsColor(0, Color.BLACK);
		mRenderer.setYLabelsPadding(8);
		mRenderer.setClickEnabled(true); //Enable on screen clicking
		mRenderer.setSelectableBuffer(20); //Set the radius of detection around the point clicked
		mRenderer.setMargins(new int[] {20,30,10,20}); //Default Margin spacing	}
		mRenderer.setAntialiasing(false); //Disable antialiasing, ment to increase performance
	}	
	/**
	 * Adds a dataset to the array, needs to be sent a name and a line colour.
	 * Pretty default line settings,
	 * 
	 * @param name, colour
	 * @return index to that time series
	 */
	public int addDataSet(String name, int colour) {
		XYSeriesRenderer renderer = new XYSeriesRenderer(); // Used to customize
															// the line
		//Note:Point Style isn't set for these datasets.
		renderer.setColor(colour);
		renderer.setFillPoints(true);

		dataArray.add(new GraphingData(new XYValueSeries(name), renderer));

		int index = dataArray.size() - 1;

		mDataset.addSeries(dataArray.get(index).getSeries());
		
		mRenderer.addSeriesRenderer(dataArray.get(index).getRender());
		
		typesList.add(LineChart.TYPE);

		return index;
	}
	
	/**
	 * Used to add the indicator dataset of where the user has touched the screen
	 * @param name The name of the data set
	 * @param colour The colour of the point
	 * @return
	 */
	public int addPointIndicator(String name, int colour) {
		XYSeriesRenderer renderer = new XYSeriesRenderer(); // Used to customize
															// the line
		renderer.setShowLegendItem(false); //hide the legend for this series
		renderer.setColor(colour);
		renderer.setFillPoints(true);
		renderer.setPointStyle(PointStyle.X);
		dataArray.add(new GraphingData(new XYValueSeries(name), renderer));
		int index = dataArray.size() - 1;
		mDataset.addSeries(dataArray.get(index).getSeries());
		mRenderer.addSeriesRenderer(dataArray.get(index).getRender());
		typesList.add(ScatterChart.TYPE);
		return index;
	}

	/**
	 * Used to return the XYChart to access its methods e.g. toScreenPoint
	 * @return
	 */
	public XYChart getChart() {
		chart = new LineChart(mDataset, mRenderer);
		return chart;
	}
	
	/**
	 * Returns the Graphical view, used to draw the graph
	 * @param context
	 * @return
	 */
	public GraphicalView getView(Context context) {
		String[] types = new String[typesList.size()];
		for (int i = 0; i < typesList.size(); i++) {
			types[i] = typesList.get(i);
		}
		view = ChartFactory.getCombinedXYChartView(context, mDataset, mRenderer, types);
		return view;
	}
	
	/**
	 * This method is used if you have already created a XYChart using the
	 * getView(Context context) method
	 * @param context
	 * @param ch
	 * @return
	 */
	public GraphicalView getView(Context context, XYChart ch) {
		view = new GraphicalView(context, ch); 
		return view;		
	}

	/**
	 * Adds a new point to the arraylist. 
	 * @param p
	 * @param index relates to the dataset number
	 */
	public void addNewPoint(CustomPoint p, int index) {
		dataArray.get(index).addNewPoint(p);
	}
	
	/**
	 * Used to add bubble co-ordinates
	 * Adds a new point to the arraylist. 
	 * @param p
	 * @param index relates to the dataset number 
	 * @param value represents radius
	 */
	public void addNewPoint(CustomPoint p, int index, double value) {
		dataArray.get(index).addNewPoint(p,value);
	}
	
	/**
	 * Use this method when adding repeating data, e.g. when the graph is paused.
	 * @param p
	 * @param index
	 * @param pause
	 */
	public void addNewPoint(CustomPoint p, int index, boolean pause) {
		dataArray.get(index).addNewPoint(p,pause);
	}
	
	/**
	 * Returns the data that is currently being displayed. 
	 * Row 0 = x axis
	 * Row 1 = y axis
	 * @param dataset
	 * @return
	 */
	public ArrayList<double[]> returnCurrentDataSet(int dataset) {
		ArrayList<double[]> data = new ArrayList<double[]>();
		data = dataArray.get(dataset).getDisplayedData();
		return data;
	}
	
	/**
	 * Returns the data that has been added to the arraylist in the GraphingData.java class 
	 * This is all the data that has been added to the dataset,
	 * Even if it has been cleared from the dataset by using the clearData() call. 
	 * @param dataset
	 * @return
	 */
	public ArrayList<double[]> returnEntireDataSet(int dataset) {
		ArrayList<double[]> data = new ArrayList<double[]>();
		data = dataArray.get(dataset).getAllData();
		Log.d("SAVING_DATA", "Line Called");
		return data;
	}
	
	/**
	 * Removes the data in the specified dataset, but not the arraylist
	 * @param index the dataset to be cleared
	 */
	public void clearData(int index) {
		dataArray.get(index).clearData();
	}
	
	/**
	 * Removes the data stored in the Arraylist, this will permanently delete the data
	 * stored in this dataset
	 * @param index
	 */
	public void clearArrayListData(int index) {
		dataArray.get(index).clearArrayListData();
	}

	/**
	 * Set the initial range
	 * @param initRange in the format [xmin,xmax,ymin,ymax]
	 */
	public void setInitialRange(double[] initRange) {
		range = initRange;
		mRenderer.setRange(range);
	}
	
	/**
	 * Modify the range set.
	 * @param newRange in the format [xmin,xmax,ymin,ymax]
	 */
	public void updateRange(double[] newRange) {
		range = newRange;
		mRenderer.setRange(range);
	}
	
	/**
	 * Function to rotate the graph. 
	 * @param screenOrientation The orientation of the screen/tablet
	 */
	public void rotateGraph(int screenOrientation) {
		Orientation o = mRenderer.getOrientation();
		// Checks if graph is currently horizontal, if it is rotates it and 
		// adjusts the display.
		if(o == Orientation.HORIZONTAL) {
			mRenderer.setOrientation(Orientation.VERTICAL);
			mRenderer.setYTitle("");
			mRenderer.setDisplayValues(false);
			if(screenOrientation == Configuration.ORIENTATION_LANDSCAPE) {
				mRenderer.setMargins(new int[] {0,0,15,80});
			} else if(screenOrientation == Configuration.ORIENTATION_PORTRAIT) {
				mRenderer.setMargins(new int[] {0,0,15,-45});
			}
		}
		else {
			mRenderer.setOrientation(Orientation.HORIZONTAL);
			initGraphHoriz();
		}
		view.repaint();
	}
	
	/**
	 * Checks if the graph is rotated
	 * @return true if horizontal, false if vertical
	 */
	public boolean isRotated() {
		Orientation o = mRenderer.getOrientation();
		if (o == Orientation.HORIZONTAL) {
			return false;
		}else
			return true;
	}
}
