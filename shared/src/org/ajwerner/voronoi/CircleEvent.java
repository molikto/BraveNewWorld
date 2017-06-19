package org.ajwerner.voronoi;

/**
 * Created by ajwerner on 12/28/13.
 */
strictfp class CircleEvent extends Event {
    final Arc arc;
    final Point vert;

    CircleEvent(Arc a, Point p, Point vert) {
        super(p);
        this.arc = a;
        this.vert = vert;
    }
}
