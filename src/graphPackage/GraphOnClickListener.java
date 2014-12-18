package graphPackage;

import org.achartengine.GraphicalView;
import org.achartengine.model.SeriesSelection;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

/**
 * Used to interpret an on screen click. Implementing onClickListener rather than an on
 * touch listener because it will allow me to use the zoom features (dragging and zoom buttons)
 * Reference: http://stackoverflow.com/questions/17878602/several-questions-to-achartengine
 * @author ajl157
 *
 */
public class GraphOnClickListener implements OnClickListener {

	TextView displayCoOrds;
	LineGraph line;
	int indicator;
	GraphicalView view;
	
	public GraphOnClickListener(TextView disp, LineGraph l, int indicatorSeries, GraphicalView v) {
		displayCoOrds = disp;
		line = l;
		indicator = indicatorSeries;
		view = v;
	}
	
	public void onClick(View v) {
		SeriesSelection seriesSelection = ((GraphicalView) v)
                .getCurrentSeriesAndPoint();
        if (seriesSelection == null) {
        } else {
        	line.clearData(indicator); //Remove previous point
        	displayCoOrds.setText("Selected Co-ord:" + "\n" + "x: " + seriesSelection.getXValue() + "y: " + seriesSelection.getValue());
        	CustomPoint p = new CustomPoint(seriesSelection.getXValue(), seriesSelection.getValue());
        	line.addNewPoint(p, indicator); //Add the point clicked on
        	view.repaint(); //Refresh screen
        }
	}			
}
