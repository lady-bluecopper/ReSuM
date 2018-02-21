package eu.unitn.disi.db.resum.distance;

import java.util.ArrayList;

/**
 *
 * @author bluecopper
 * @param <T>
 */
public class CosineSimilarity<T> implements Distance<ArrayList<T>> {
   
    public double distance(ArrayList<T> first, ArrayList<T> second) {
        double sim = 0;
        double sqSum_f = 0;
        double sqSum_s = 0;
        if (first.get(0) instanceof Double) {
            for (int i = 0; i < first.size(); i++) {
                sim += (Double) first.get(i) * (Double) second.get(i);
                sqSum_f += Math.pow((Double) first.get(i), 2);
                sqSum_s += Math.pow((Double) second.get(i), 2);
            }
            if (Math.sqrt(sqSum_f) * Math.sqrt(sqSum_s) > 0) {
                return 1 - (sim / (Math.sqrt(sqSum_f) * Math.sqrt(sqSum_s)));
            }
            return 1;
        }
        return -1;
    }
    
    public double similarity(ArrayList<T> first, ArrayList<T> second) {
        double sim = 0;
        double sqSum_f = 0;
        double sqSum_s = 0;
        if (first.get(0) instanceof Double) {
            for (int i = 0; i < first.size(); i++) {
                sim += (Double) first.get(i) * (Double) second.get(i);
                sqSum_f += Math.pow((Double) first.get(i), 2);
                sqSum_s += Math.pow((Double) second.get(i), 2);
            }
            if (Math.sqrt(sqSum_f) * Math.sqrt(sqSum_s) > 0) {
                return sim / (Math.sqrt(sqSum_f) * Math.sqrt(sqSum_s));
            }
            return 0;
        }
        return -1;
    }
    
    public double similarity(T[] first, T[] second) {
        double sim = 0;
        double sqSum_f = 0;
        double sqSum_s = 0;
        if (first[0] instanceof Double) {
            for (int i = 0; i < first.length; i++) {
                sim += (Double) first[i] * (Double) second[i];
                sqSum_f += Math.pow((Double) first[i], 2);
                sqSum_s += Math.pow((Double) second[i], 2);
            }
            if (Math.sqrt(sqSum_f) * Math.sqrt(sqSum_s) > 0) {
                return sim / (Math.sqrt(sqSum_f) * Math.sqrt(sqSum_s));
            }
            return 0;
        }
        return -1;
    }
    
}
