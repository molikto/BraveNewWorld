
package org.serenaz;

// a point in 2D, sorted by y-coordinate
strictfp public class Point implements Comparable <Point> {
	
	public double x;
	public double y;
	
	public Point(double x0, double y0) {
		x = x0;
		y = y0;
	}
	
	public int compareTo (Point other) {
		if (this.y == other.y) {
			if (this.x == other.x) return 0;
			else if (this.x > other.x) return 1;
			else return -1;
		}
		else if (this.y > other.y) {
			return 1;
		}
		else {
			return -1;
		}
	}

	@Override
	public boolean equals(Object obj) {
	    if (obj instanceof Point) {
	        return compareTo((Point) obj) == 0;
		}
		return false;
	}

	public String toString() {
		return "(" + x + ", " + y + ")";
	}
}
