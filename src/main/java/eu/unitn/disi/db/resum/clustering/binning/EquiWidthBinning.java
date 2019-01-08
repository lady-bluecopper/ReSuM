package eu.unitn.disi.db.resum.clustering.binning;

import com.koloboke.collect.map.hash.HashDoubleObjMap;
import com.koloboke.collect.map.hash.HashDoubleObjMaps;
import com.koloboke.collect.map.hash.HashIntObjMap;
import com.koloboke.collect.map.hash.HashIntObjMaps;
import eu.unitn.disi.db.resum.utilities.Settings;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.IntStream;
import sa.edu.kaust.grami.dataStructures.MultiUserWeightedEdge;

/**
 *
 * @author bluecopper
 */
public class EquiWidthBinning extends Binning {
    
    public EquiWidthBinning(int labelsNum) {
        super(labelsNum);
    }
    
    public HashIntObjMap<double[]> createFeatureVectors(HashDoubleObjMap<List<MultiUserWeightedEdge<Integer, Double, double[]>>> edgesByLabel) {

        HashDoubleObjMap<BucketList> binningMap = computeBinningMap(edgesByLabel);
        HashIntObjMap<double[]> featureVectors = HashIntObjMaps.newMutableMap();
        IntStream.range(0, Settings.numberOfEdgeWeights).forEach(u -> featureVectors.put(u, new double[labelsNum * Settings.bucketsNum]));
        int base = 0;
        for (Entry<Double, List<MultiUserWeightedEdge<Integer, Double, double[]>>> e : edgesByLabel.entrySet()) {
            BucketList bList = binningMap.get(e.getKey());
            final int fBase = base;
            e.getValue().stream().forEach(edge -> IntStream.range(0, Settings.numberOfEdgeWeights).forEach(i -> {
                        double[] newVector = featureVectors.get(i);
                        newVector[fBase * Settings.bucketsNum + bList.getBucket(edge.getMaxWeights()[i])] += 1;
                        featureVectors.put(i, newVector);
            }));
            base += 1;
        }
        return featureVectors;
    }
    
    protected HashDoubleObjMap<BucketList> computeBinningMap(HashDoubleObjMap<List<MultiUserWeightedEdge<Integer, Double, double[]>>> edgesByLabel) {
        HashDoubleObjMap<BucketList> binningMap = HashDoubleObjMaps.newMutableMap();
        edgesByLabel.keySet().stream().forEach(label -> binningMap.put(label, computeBucketList(null)));
        return binningMap;
    }
    
    protected BucketList computeBucketList(List<double[]> edgeWeights) {
        BucketList buckets = new BucketList(Settings.bucketsNum);
        double step = 1.0 / Settings.bucketsNum;
        double bound = step;
        for (int i = 0; i < Settings.bucketsNum; i++) {
            buckets.setBoundary(i, bound);
            bound += step;
        }
        buckets.setBoundary(Settings.bucketsNum - 1, 1);
        return buckets;
    }
    
}
