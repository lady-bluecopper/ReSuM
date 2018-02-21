package eu.unitn.disi.db.resum.clustering.binning;

import com.google.common.primitives.Doubles;
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
public class DynEquiDepthBinning extends Binning {
    
    public DynEquiDepthBinning(int labelsNum) {
        super(labelsNum);
    }
    
    public ArrayList<ArrayList<Double>> createFeatureVectors(
            HashMap<Double, ArrayList<Integer>> edgesByLabel,
            ArrayList<double[]> edgeWeightsByIndex) {

        HashDoubleObjMap<BucketList> binningMap = computeBinningMap(edgesByLabel, edgeWeightsByIndex);
        int size = 0;
        for (BucketList bList : binningMap.values()) {
            size += bList.boundaries.length;
        }
        final int vectorLen = size;
        ArrayList<ArrayList<Double>> featureVectors = new ArrayList<ArrayList<Double>>(Settings.numberOfFunctions);
        for (int u = 0; u < Settings.numberOfFunctions; u ++) {
            featureVectors.add(u, new ArrayList<Double>(Collections.nCopies(vectorLen, 0.)));
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
                    double current = featureVectors.get(i).get(fBase + bList.getBucket(edgeWeights[i]));
                    featureVectors.get(i).set(fBase + bList.getBucket(edgeWeights[i]), current + 1);   
                });
            }
            base += bList.boundaries.length;
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

        ArrayList<Double> allWeights = new ArrayList<Double>();
        for (int edgeIndex : edges) {
            allWeights.addAll(new ArrayList(Doubles.asList(edgeWeightsByIndex.get(edgeIndex))));
        }
        Collections.sort(allWeights);

        BucketList buckets = new BucketList(Settings.bucketsNum);
        int currentSize = 0;
        int currentBucket = 0;
        int i = 0;
        double share = allWeights.size() / Settings.bucketsNum.doubleValue();
        Double currentElement = allWeights.get(0);

        while (currentBucket < Settings.bucketsNum - 1 && i < allWeights.size() - 1) {
            while ((currentSize < share || currentElement.equals(allWeights.get(Math.max(0, i - 1)))) && i < allWeights.size() - 1) {
                currentSize++;
                i++;
                currentElement = allWeights.get(i);
            }
            buckets.boundaries[currentBucket] = allWeights.get(i - 1);
            currentBucket++;
            currentSize = 0;
        }
        buckets.boundaries[currentBucket] = 1;
        buckets.resizeBucketList(currentBucket);
        
        return buckets;
    }
    
}
