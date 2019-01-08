package eu.unitn.disi.db.resum.distance;

/**
 *
 * @author bluecopper
 * @param <T>
 */
public class EuclideanDistance<T> implements Distance<T[]> {

    public double distance(T[] first, T[] second) {
        if (first[0] instanceof Double) {
            double distance = 0;
            for (int i = 0; i < first.length; i++) {
                distance += Math.pow((Double) first[i] - (Double) second[i], 2);
            }
            return Math.sqrt(distance);
        }
        return -1;
    }
    
    public double similarity(T[] first, T[] second) {
        return 1 / (1 + distance(first, second));
    }

}
