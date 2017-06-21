
package org.serenaz;

import org.jetbrains.annotations.NotNull;

// an edge on the Voronoi diagram
strictfp public class Edge {

	public Point start;
	public Point end;
	public InputPoint site_left;
	public InputPoint site_right;

	Edge neighbor; // the same edge, but pointing in the opposite direction
	
	public double slope;
	public double yint; // y = x * slope + y0
	public double samplePointY;


	/**
	 * change the edge to region [0, 1], or return false, if the edge is entirely outside
	 */
	public boolean clamp() {
	    if (start.x >= 0 && start.x <= 1 && end.x >= 0 && end.x <= 1
		 && start.y >= 0 && start.y <= 1 && end.y >= 0 && end.y <= 1
				) {
	    	return true;
		} else {
	        if (start.x != end.x) {
	            if (start.y != end.y) {
					// y = slope * x + y0
					// x = (y - y0) / slope

					System.out.println(start);
					System.out.println(end);
					// when x = 0
					double y0 = start.y - start.x * slope;
					if (y0 >= 0 && y0 <= 1) {
						if (start.x < 0) {
							start.x = 0;
							start.y = y0;
						}
						if (end.x < 0) {
							end.x = 0;
							end.y = y0;
						}
					}
					double y1 = slope + y0;
					if (y1 >= 0 && y1 <= 1) {
						if (start.x > 1) {
							start.x = 1;
							start.y = y1;
						}
						if (end.x > 1) {
							end.x = 1;
							end.y = y1;
						}
					}
					double x0 = - y0 / slope;
					if (x0 >= 0 && x0 <= 1) {
						if (start.y < 0) {
							start.y = 0;
							start.x = x0;
						}
						if (end.y < 0) {
							end.y = 0;
							end.x = x0;
						}
					}
					double x1 = (1 - y0) / slope;
					if (x1 >= 0 && x1 <= 1) {
						if (start.y > 1) {
							start.y = 1;
							start.x = x1;
						}
						if (end.y > 1) {
							end.y = 1;
							end.x = x1;
						}
					}
				} else {
					start.x = clamp(start.x);
					end.x = clamp(end.x);
				}
			} else {
	            start.y = clamp(start.y);
	            end.y = clamp(end.y);
			}
			return start.x != end.x || start.y != end.y;
		}
	}

	public static double clamp(double p) {
	    return p > 1 ? 1 : (p < 0 ? 0 : p);
	}

	public Edge (Point first, InputPoint left, InputPoint right) {
		start = first;
		site_left = left;
		site_right = right;
		end = null;
		slope = (right.x - left.x)/(left.y - right.y);
		Point mid = new Point ((right.x + left.x)/2, (left.y+right.y)/2);
		// y = x * slope + y0
		yint = mid.y - slope*mid.x;
	}
	
	public String toString() {
		return start + ", " + end;
	}


	public InputPoint otherSide(@NotNull InputPoint it) {
		if (it == site_left) {
			return site_right;
		}
		return site_left;
	}

}
