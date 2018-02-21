package eu.unitn.disi.db.resum.mst;

/**
 *
 * @author bluecopper
 */
public class Link {

    int src;
    int dst;
    double distance;

    public Link(int src, int dst, double distance) {
        this.src = src;
        this.dst = dst;
        this.distance = distance;
    }

    public int getSrc() {
        return src;
    }

    public int getDst() {
        return dst;
    }

    public double getDistance() {
        return distance;
    }

    @Override
    public boolean equals(Object other) {
        //check for self-comparison
        if (this == other) {
            return true;
        }
        //actual comparison
        if (other instanceof Link) {
            Link edge = (Link) other;
            if (this.src == edge.src) {
                if (this.dst == edge.dst) {
                    return this.distance == edge.distance;
                }
                return false;
            } else {
                return false;
            }
        } else {
            throw new IllegalArgumentException("Not comparable objects");
        }
    }

}
