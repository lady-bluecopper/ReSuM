package sa.edu.kaust.grami.und.dataStructures;

/**
 *
 * @author bluecopper
 * @param <NODE>
 * @param <EDGELABEL>
 */
public class UnEdge<NODE, EDGELABEL> implements Comparable {

    private final NODE n;
    private final EDGELABEL l;

    public UnEdge(NODE n, EDGELABEL l) {
        this.n = n;
        this.l = l;
    }

    public NODE getNodeID() {
        return n;
    }

    public EDGELABEL getEdgeLabel() {
        return l;
    }

    @Override
    public boolean equals(Object other) {
        //check for self-comparison
        if (this == other) {
            return true;
        }
        //actual comparison
        if (other instanceof UnEdge) {
            UnEdge<Integer, Double> edge = (UnEdge<Integer, Double>) other;

            if (((Integer) this.n).equals(edge.n)) {
                return (((Double) this.l).equals(edge.l));
            } else {
                return false;
            }
        } else {
            throw new IllegalArgumentException("Not comparable objects");
        }
    }

    @Override
    public int compareTo(Object o) {
        if (o instanceof UnEdge) {
            UnEdge<Integer, Double> edge = (UnEdge<Integer, Double>) o;
            if (((Integer) this.n).equals(edge.n)) {
                if (((Double) this.l).equals(edge.l)) {
                    return 0;
                }
                return -((Double) this.l).compareTo(edge.l);
            }
            return -((Integer) this.n).compareTo(edge.n);
        } else {
            throw new IllegalArgumentException("Not comparable objects");
        }
    }

    @Override
    public String toString() {
        return "(" + this.n.toString() + "," + this.l.toString() + ")";
    }

}
