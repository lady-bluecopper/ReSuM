package eu.unitn.disi.db.resum.clustering.binning;

import com.koloboke.collect.map.hash.HashDoubleObjMap;
import com.koloboke.collect.map.hash.HashDoubleObjMaps;
import eu.unitn.disi.db.resum.utilities.Settings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

/**
 *
 * @author bluecopper
 */
public class EquiWidthBinning extends Binning {
    
    public EquiWidthBinning(int labelsNum) {
        super(labelsNum);
    }
    
    public ArrayList<ArrayList<Double>> createFeatureVectors(
            HashMap<Double, ArrayList<Integer>> edgesByLabel,
            ArrayList<double[]> edgeWeightsByIndex) {

        HashDoubleObjMap<BucketList> binningMap = computeBinningMap(edgesByLabel, null);
        ArrayList<ArrayList<Double>> featureVectors = new ArrayList<ArrayList<Double>>(Settings.numberOfFunctions);
        for (int u = 0; u < Settings.numberOfFunctions; u ++) {
            featureVectors.add(u, new ArrayList<Double>(Collections.nCopies(labelsNum * Settings.bucketsNum, 0.)));
        }
        int base = 0;
        for (Map.Entry e : edgesByLabel.entrySet()) {
            double edgeLabel = (Double) e.getKey();
            ArrayList<Integer> edges = (ArrayList<Integer>) e.getValue();
            BucketList bList = binningMap.get(edgeLabel);
            final int fBase = base;
            for (int edgeIndex : edges) {
                double[] edgeWeights = edgeWeightsByIndex.get(edgeIndex);
                IntStream.range(0, Settings.numberOfFunctions).parallel().forEach(i -> { 
                    double current = featureVectors.get(i).get(fBase * Settings.bucketsNum + bList.getBucket(edgeWeights[i]));
                    featureVectors.get(i).set(fBase * Settings.bucketsNum + bList.getBucket(edgeWeights[i]), current + 1);  
                            });
            }
            base += 1;
        }
        return featureVectors;
    }
    
    protected HashDoubleObjMap<BucketList> computeBinningMap(HashMap<Double, ArrayList<Integer>> edgesByLabel,
            ArrayList<double[]> edgeWeightsByIndex) {
        HashDoubleObjMap<BucketList> binningMap = HashDoubleObjMaps.<BucketList>newUpdatableMap();
        for (double label : edgesByLabel.keySet()) {
            binningMap.put(label, computeBucketList(null, null));
        }
        return binningMap;
    }
    
    protected BucketList computeBucketList(ArrayList<double[]> edgeWeightsByIndex,
            ArrayList<Integer> edges) {
        BucketList buckets = new BucketList(Settings.bucketsNum);
        double step = 1.0 / Settings.bucketsNum;
        double bound = step;
        for (int i = 0; i < Settings.bucketsNum; i++) {
            buckets.boundaries[i] = bound;
            bound += step;
        }
        buckets.boundaries[Settings.bucketsNum - 1] = 1;
        return buckets;
    }
    
}
