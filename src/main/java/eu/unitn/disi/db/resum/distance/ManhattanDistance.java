package eu.unitn.disi.db.resum.distance;

/**
 *
 * @author bluecopper
 * @param <T>
 */
public class ManhattanDistance<T> implements Distance<T[]> {

    public double distance(T[] first, T[] second) {
        double dist = 0;
        if (first[0] instanceof Double) {
            for (int i = 0; i < first.length; i++) {
                dist += Math.abs((Double) first[i] - (Double) second[i]);
            }
            return dist;
        }
        return -1;
    }

}
