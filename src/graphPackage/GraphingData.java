package graphPackage;

import java.util.ArrayList;

import org.achartengine.model.XYValueSeries;
import org.achartengine.renderer.XYSeriesRenderer;


/**
 * Really basic graph data class. Holds data, renderer and timeseries. Designed
 * to be used in a sweeping style graph. 
 * 
 * @author ajl157
 * 
 */
public class GraphingData {

	private XYValueSeries dataset; //Uses XYValueSeries so bubbleChart can be used
	private XYSeriesRenderer renderer;
	private ArrayList<CustomPoint> points = new ArrayList<CustomPoint>();

	public GraphingData(XYValueSeries series, XYSeriesRenderer r) {
		this.dataset = series;
		this.renderer = r;
	}
	/**
	 * Add new data point
	 * @param p
	 */
	public void addNewPoint(CustomPoint p) {
		dataset.add(p.getX(), p.getY());
		points.add(p);
	}
	
	/**
	 * Used for add bubble coordinates. The value represents the radius.
	 * @param p
	 * @param value
	 */
	public void addNewPoint(CustomPoint p, double value) {
		dataset.add(p.getX(),p.getY(), value);
	}
	
	/**
	 * Use this method when adding repeated data. E.g. redrawing the graph when paused
	 * @param p
	 * @param pause
	 */
	public void addNewPoint(CustomPoint p, boolean pause) {
		if (pause == true) {
			dataset.add(p.getX(), p.getY());
		}
	}
	
	/**
	 * get the dataset series
	 * @return
	 */
	public XYValueSeries getSeries() {
		return this.dataset;
	}
	
	/**
	 * get all the data being displayed
	 * @return
	 */
	public ArrayList<double[]> getDisplayedData() {
		ArrayList<double[]> data = new ArrayList<double[]>();
		int index = dataset.getItemCount();
		
		for(int i = 0; i < index; i++) {
			double[] temp = {dataset.getX(i), dataset.getY(i)};
			data.add(temp);
		}
		
		return data;
	}
	
	/**
	 * get all the data written to the graph
	 * @return
	 */
	public ArrayList<double[]> getAllData() {
		ArrayList<double[]> data = new ArrayList<double[]>();
		int index = points.size();
		for(int i = 0; i < index; i++) {
			double[] temp = {points.get(i).getX(), points.get(i).getY()};
			data.add(temp);
			
		}		
		return data;
	}

	/**
	 * 
	 * @return
	 */
	public XYSeriesRenderer getRender() {
		return this.renderer;
	}
	
	/**
	 * Clear the data in the dataset
	 */
	public void clearData() {
		dataset.clear();
	}

	/**
	 * Clear the array list
	 */
	public void clearArrayListData() {
		points.clear();
	}
	
}