
package org.serenaz;

// an edge on the Voronoi diagram
strictfp public class Edge {

	public Point start;
	public Point end;
	public Point site_left;
	public Point site_right;
	public Point direction; // edge is really a vector normal to left and right points
	
	Edge neighbor; // the same edge, but pointing in the opposite direction
	
	float slope;
	float yint;
	
	public Edge (Point first, Point left, Point right) {
		start = first;
		site_left = left;
		site_right = right;
		direction = new Point(right.y - left.y, - (right.x - left.x));
		end = null;		
		slope = (right.x - left.x)/(left.y - right.y);
		Point mid = new Point ((right.x + left.x)/2, (left.y+right.y)/2);
		yint = mid.y - slope*mid.x;
	}
	
	public String toString() {
		return start + ", " + end;
	}


}
