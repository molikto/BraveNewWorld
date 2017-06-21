package org.serenaz;

import java.util.ArrayList;

/**
 * Created by molikto on 19/06/2017.
 */
public class InputPoint extends Point {

    public static class Attachment {
        public boolean isSea = false;
        public boolean isBeach = false;
        public int height = -1;
        public ArrayList<Edge> edges = new ArrayList<>();

        public boolean nearSea() {
            for (Edge e : edges) {
                if (e.site_left.attachment.isSea || e.site_right.attachment.isSea) {
                    return true;
                }
            }
            return false;
        }
    }

    public int edgeCount;

    public Attachment attachment = null;
    public InputPoint(double x0, double y0) {
        super(x0, y0);
    }
}
