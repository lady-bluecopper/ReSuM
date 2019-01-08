package eu.unitn.disi.db.resum.clustering.binning;

import com.koloboke.collect.map.hash.HashDoubleIntMap;
import com.koloboke.collect.map.hash.HashDoubleIntMaps;
import com.koloboke.collect.map.hash.HashDoubleObjMap;
import com.koloboke.collect.map.hash.HashDoubleObjMaps;
import com.koloboke.collect.map.hash.HashIntObjMap;
import com.koloboke.collect.map.hash.HashIntObjMaps;
import eu.unitn.disi.db.resum.utilities.Settings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.math3.util.Precision;
import sa.edu.kaust.grami.dataStructures.MultiUserWeightedEdge;

/**
 *
 * @author bluecopper
 */
public class EquiDepthBinning extends Binning {
    
    public EquiDepthBinning(int labelsNum) {
        super(labelsNum);
    }
    
    public HashIntObjMap<double[]> createFeatureVectors(HashDoubleObjMap<List<MultiUserWeightedEdge<Integer, Double, double[]>>> edgesByLabel) {
        HashDoubleObjMap<BucketList> binningMap = computeBinningMap(edgesByLabel);
        System.out.println("map computed");
        HashIntObjMap<double[]> featureVectors = HashIntObjMaps.newMutableMap();
        IntStream.range(0, Settings.numberOfEdgeWeights).forEach(u -> featureVectors.put(u, new double[labelsNum * Settings.bucketsNum]));
        int base = 0;
        for (Entry<Double, List<MultiUserWeightedEdge<Integer, Double, double[]>>> e : edgesByLabel.entrySet()) {
            BucketList bList = binningMap.get(e.getKey());
            final int fBase = base;
            e.getValue().stream().forEach(edge -> 
                    IntStream.range(0, Settings.numberOfEdgeWeights).forEach(i -> {
                        double[] newVector = featureVectors.get(i);
                        newVector[fBase * Settings.bucketsNum + bList.getBucket(edge.getMaxWeights()[i])] += 1;
                        featureVectors.put(i, newVector);
                    })
            );
            base += 1;
        }
        System.out.println("vectors created");
        return featureVectors;
    }

    protected HashDoubleObjMap<BucketList> computeBinningMap(HashDoubleObjMap<List<MultiUserWeightedEdge<Integer, Double, double[]>>> edgesByLabel) {
        HashDoubleObjMap<BucketList> binningMap = HashDoubleObjMaps.newMutableMap();
        edgesByLabel.entrySet().stream().forEach(e -> binningMap.put(e.getKey(), computeBucketList(e.getValue().stream().map(edge -> edge.getMaxWeights()).collect(Collectors.toList()))));
        return binningMap;
    }
    
    protected BucketList computeBucketList(List<double[]> edgeWeights) {
        HashDoubleIntMap edgeWeightsCounts = HashDoubleIntMaps.newMutableMap();
        edgeWeights.stream().forEach(vec -> {
            IntStream.range(0, vec.length).forEach(i -> {
                double thisW = Precision.round(vec[i], 3);
                edgeWeightsCounts.put(thisW, edgeWeightsCounts.getOrDefault(thisW, 0) + 1);
            });
        });
        List<Double> orderedEdgeWeightList = new ArrayList<>(edgeWeightsCounts.keySet());
        Collections.sort(orderedEdgeWeightList);
        BucketList buckets = new BucketList(Settings.bucketsNum);
        int adjust = 0;
        int i = 0;
        int currentBuck = 0;
        double threshold = -1;
        double avg = edgeWeights.size() * Settings.numberOfEdgeWeights * 1.0 / Settings.bucketsNum;
        for (double w : orderedEdgeWeightList) {
            int c = edgeWeightsCounts.get(w);
            if ((currentBuck + c < avg + adjust) || (currentBuck + c < avg)) {
                threshold = w;
                currentBuck += c;
            } else {
                if (currentBuck >= avg) {
                    buckets.setBoundary(i, threshold);
                    adjust -= currentBuck - avg;
                    currentBuck = c;
                } else {
                    buckets.setBoundary(i, w);
                    adjust -= currentBuck + c - avg;
                    currentBuck = 0;
                }
                threshold = -1;
                i++;
                if (i == Settings.bucketsNum) {
                    break;
                }
            }
        }
        if (threshold != -1 && i < Settings.bucketsNum - 1) {
            buckets.setBoundary(i, threshold);
            i++;
        }
        if (i < Settings.bucketsNum) {
            buckets.setBoundary(i, 1);
            i++;
        }
        return buckets;
    }
}