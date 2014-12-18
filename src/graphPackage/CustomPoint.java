package graphPackage;

/**
 * This class is used to store an x and y co-ordinate. Differs from the AChartEngine Point class
 * because it uses doubles over floats. 
 * @author ajl157
 *
 */

public class CustomPoint {
	
	private double x;
	private double y;

	public CustomPoint(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public void setX(double x) {
		this.x = x;
	}

	public void setY(double y) {
		this.y = y;
	}

}
