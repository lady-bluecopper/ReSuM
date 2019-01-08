package eu.unitn.disi.db.resum.distance;

/**
 *
 * @author bluecopper
 */
public class ManhattanDistance implements Distance<double[]> {

    public double distance(double[] first, double[] second) {
        double dist = 0;
        for (int i = 0; i < first.length; i++) {
            dist += Math.abs(first[i] - second[i]);
        }
        return dist;
    }

}
