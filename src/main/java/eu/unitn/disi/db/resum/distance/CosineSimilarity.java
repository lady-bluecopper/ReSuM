package eu.unitn.disi.db.resum.distance;

/**
 *
 * @author bluecopper
 */
public class CosineSimilarity implements Distance<double[]> {
   
    public double distance(double[] first, double[] second) {
        double sim = 0;
        double sqSum_f = 0;
        double sqSum_s = 0;
        for (int i = 0; i < first.length; i++) {
            sim += first[i] * second[i];
            sqSum_f += Math.pow(first[i], 2);
            sqSum_s += Math.pow(second[i], 2);
        }
        if (Math.sqrt(sqSum_f) * Math.sqrt(sqSum_s) > 0) {
            return 1 - (sim / (Math.sqrt(sqSum_f) * Math.sqrt(sqSum_s)));
        }
        return 1;
    }
    
    public double similarity(double[] first, double[] second) {
        double sim = 0;
        double sqSum_f = 0;
        double sqSum_s = 0;
        for (int i = 0; i < first.length; i++) {
            sim += first[i] * second[i];
            sqSum_f += Math.pow(first[i], 2);
            sqSum_s += Math.pow(second[i], 2);
        }
        if (Math.sqrt(sqSum_f) * Math.sqrt(sqSum_s) > 0) {
            return sim / (Math.sqrt(sqSum_f) * Math.sqrt(sqSum_s));
        }
        return 0;
    }
    
}
