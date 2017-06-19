package org.ajwerner.voronoi;

strictfp public class BreakPoint {
    private final Voronoi v;
    final Point s1, s2;
    private VoronoiEdge e;
    private boolean isEdgeLeft;
    final Point edgeBegin;

    private float cacheSweepLoc;
    private Point cachePoint;

    BreakPoint(Point left, Point right, VoronoiEdge e, boolean isEdgeLeft, Voronoi v) {
        this.v = v;
        this.s1 = left;
        this.s2 = right;
        this.e = e;
        this.isEdgeLeft = isEdgeLeft;
        this.edgeBegin = this.getPoint();
    }

    private static float sq(float d) {
        return d * d;
    }

    void finish(Point vert) {
        if (isEdgeLeft) {
            this.e.p1 = vert;
        }
        else {
            this.e.p2 = vert;
        }
    }

    void finish() {
        Point p = this.getPoint();
        if (isEdgeLeft) {
            this.e.p1 = p;
        }
        else {
            this.e.p2 = p;
        }
    }

    public Point getPoint() {
        float l = v.getSweepLoc();
        if (l == cacheSweepLoc) {
            return cachePoint;
        }
        cacheSweepLoc = l;

        float x,y;
        // Handle the vertical line case
        if (s1.y == s2.y) {
            x = (s1.x + s2.x) / 2; // x coordinate is between the two sites
            // comes from parabola focus-directrix definition:
            y = (sq(x - s1.x) + sq(s1.y) - sq(l)) / (2* (s1.y - l));
        }
        else {
            // This method works by intersecting the line of the edge with the parabola of the higher point
            // I'm not sure why I chose the higher point, either should work
            float px = (s1.y > s2.y) ? s1.x : s2.x;
            float py = (s1.y > s2.y) ? s1.y : s2.y;
            float m = e.m;
            float b = e.b;

            float d = 2*(py - l);

            // Straight up quadratic formula
            float A = 1;
            float B = -2*px - d*m;
            float C = sq(px) + sq(py) - sq(l) - d*b;
            int sign = (s1.y > s2.y) ? -1 : 1;
            float det = sq(B) - 4 * A * C;
            // When rounding leads to a very very small negative determinant, fix it
            if (det <= 0) {
                x = -B / (2 * A);
            }
            else {
                x = (-B + sign * ((float) StrictMath.sqrt(det))) / (2 * A);
            }
            y = m*x + b;
        }
        cachePoint = new Point(x, y);
        return cachePoint;
    }

    public String toString() {
        return String.format("%s \ts1: %s\ts2: %s", this.getPoint(), this.s1, this.s2);
    }

    VoronoiEdge getEdge() {
        return this.e;
    }
}
