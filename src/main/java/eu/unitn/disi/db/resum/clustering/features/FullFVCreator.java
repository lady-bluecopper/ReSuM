package eu.unitn.disi.db.resum.clustering.features;

import com.koloboke.collect.map.hash.HashIntObjMap;
import com.koloboke.collect.map.hash.HashIntObjMaps;
import eu.unitn.disi.db.resum.utilities.Settings;
import java.util.stream.IntStream;

/**
 *
 * @author bluecopper
 */
public class FullFVCreator extends FVCreator {
    
    HashIntObjMap<double[]> edgeWeights;

    public FullFVCreator(HashIntObjMap<double[]> edgeWeights) {
        this.edgeWeights = edgeWeights;
    }

    public HashIntObjMap<double[]> createFeatureVectors() {
        HashIntObjMap<double[]> featureVectors = HashIntObjMaps.newMutableMap();
        IntStream.range(0, Settings.numberOfEdgeWeights).parallel().forEach(u -> {
            double[] current = new double[edgeWeights.size()];
            for (int e = 0; e < edgeWeights.size(); e++) {
                current[e] = edgeWeights.get(e)[u];
            }
            featureVectors.put(u, current);
        });
        return featureVectors;
    }

}
