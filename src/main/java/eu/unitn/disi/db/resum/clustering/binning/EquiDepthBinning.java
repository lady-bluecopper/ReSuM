package eu.unitn.disi.db.resum.clustering.binning;

import com.koloboke.collect.map.hash.HashDoubleObjMap;
import com.koloboke.collect.map.hash.HashDoubleObjMaps;
import eu.unitn.disi.db.resum.utilities.Settings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.IntStream;

/**
 *
 * @author bluecopper
 */
public class EquiDepthBinning extends Binning {
    
    public EquiDepthBinning(int labelsNum) {
        super(labelsNum);
    }
    
    public ArrayList<ArrayList<Double>> createFeatureVectors(
            HashMap<Double, ArrayList<Integer>> edgesByLabel,
            ArrayList<double[]> edgeWeightsByIndex) {
        
        HashDoubleObjMap<BucketList> binningMap = computeBinningMap(edgesByLabel, edgeWeightsByIndex);
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

    protected HashDoubleObjMap<BucketList> computeBinningMap(
            HashMap<Double, ArrayList<Integer>> edgesByLabel,
            ArrayList<double[]> edgeWeightsByIndex) {

        HashDoubleObjMap<BucketList> binningMap = HashDoubleObjMaps.<BucketList>newUpdatableMap();
        for (Map.Entry e : edgesByLabel.entrySet()) {
            double edgeLabel = (Double) e.getKey();
            binningMap.put(edgeLabel, computeBucketList(edgeWeightsByIndex, (ArrayList<Integer>) e.getValue()));
        }
        return binningMap;
    }
    
    protected BucketList computeBucketList(
            ArrayList<double[]> edgeWeightsByIndex,
            ArrayList<Integer> edges) {

        TreeMap<Double, Integer> edgeWeightCount = new TreeMap<Double, Integer>();
        for (int edgeIndex : edges) {
            double[] edgeWeights = edgeWeightsByIndex.get(edgeIndex);
            for (double weight : edgeWeights) {
                if (!edgeWeightCount.containsKey(weight)) {
                    edgeWeightCount.put(weight, 1);
                } else {
                    edgeWeightCount.put(weight, edgeWeightCount.get(weight) + 1);
                }
            }
        }
        BucketList buckets = new BucketList(Settings.bucketsNum);
        int adjust = 0;
        int i = 0;
        int currentBuck = 0;
        double threshold = 0;
        double avg = (edges.size() * Settings.numberOfFunctions) / Settings.bucketsNum.doubleValue();
        for (Map.Entry e : edgeWeightCount.entrySet()) {
            double w = (double) e.getKey();
            int c = (int) e.getValue();
            if ((currentBuck + c <= avg + adjust) && (currentBuck >= avg - adjust)) {
                adjust -= avg - currentBuck - c;
                buckets.boundaries[i] = w;
                threshold = w;
                currentBuck = 0;
                i += 1;
            } else if (currentBuck + c < avg - adjust) {
                currentBuck += c;
                threshold = w;

            } else {
                adjust -= avg - currentBuck;
                buckets.boundaries[i] = threshold;
                threshold = w;
                currentBuck = c;
                i += 1;
            }
            if (i == Settings.bucketsNum) {
                buckets.boundaries[i - 1] = 1;
                break;
            }
        }
        return buckets;
    }

}
