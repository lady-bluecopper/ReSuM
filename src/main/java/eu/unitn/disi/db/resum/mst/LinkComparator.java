package eu.unitn.disi.db.resum.mst;

import java.util.Comparator;

/**
 *
 * @author bluecopper
 */
public class LinkComparator implements Comparator<Link> {

    @Override
    public int compare(Link edge1, Link edge2) {
        if (edge1.distance < edge2.distance) {
            return -1;
        }
        if (edge1.distance > edge2.distance) {
            return 1;
        }
        return 0;
    }
}
