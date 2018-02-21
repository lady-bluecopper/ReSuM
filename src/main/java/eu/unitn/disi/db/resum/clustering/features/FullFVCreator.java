package eu.unitn.disi.db.resum.clustering.features;

import eu.unitn.disi.db.resum.utilities.Settings;
import java.io.IOException;
import java.util.ArrayList;
import java.util.stream.IntStream;

/**
 *
 * @author bluecopper
 */
public class FullFVCreator extends FVCreator {

    public FullFVCreator() throws IOException {
        super();
    }

    public ArrayList<ArrayList<Double>> createFeatureVectors() {
        ArrayList<ArrayList<Double>> featureVectors = new ArrayList<ArrayList<Double>>(Settings.numberOfFunctions);
        for (int u = 0; u < Settings.numberOfFunctions; u ++) {
            featureVectors.add(u, new ArrayList<Double>(edgeWeights.size()));
        }
        IntStream.range(0, Settings.numberOfFunctions).parallel().forEach(j -> {
            ArrayList<Double> currentList = featureVectors.get(j);
            for (final double[] edgeWeight : edgeWeights) {
                currentList.add(edgeWeight[j]);
            }
        });
        return featureVectors;
    }

}
