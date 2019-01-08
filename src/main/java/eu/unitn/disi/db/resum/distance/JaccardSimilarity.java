package eu.unitn.disi.db.resum.distance;

import eu.unitn.disi.db.resum.utilities.Util;
import java.util.Collection;

/**
 *
 * @author bluecopper
 */
public class JaccardSimilarity implements Distance<Collection<Integer>>{

    public double distance(Collection<Integer> first, Collection<Integer> second) {
        int unionSize = Util.unionSize(first, second);
        if (unionSize > 0) {
            return 1.0 - (double) Util.intersectionSize(first, second) / unionSize;
        } 
        return -1;
    }
    
    public double similarity(Collection<Integer> first, Collection<Integer> second) {
        int unionSize = Util.unionSize(first, second);
        if (unionSize > 0) {
            return (double) Util.intersectionSize(first, second) / unionSize;
        }
        return -1;
    }
}
