package eu.unitn.disi.db.resum.clustering.binning;

import com.koloboke.collect.map.hash.HashDoubleObjMap;
import com.koloboke.collect.map.hash.HashIntObjMap;
import java.util.List;
import sa.edu.kaust.grami.dataStructures.MultiUserWeightedEdge;

/**
 * @author bluecopper
 */
public abstract class Binning {

    protected final int labelsNum;

    public Binning(int labelsNum) {
        this.labelsNum = labelsNum;
    }
    
    protected abstract HashDoubleObjMap<BucketList> computeBinningMap(HashDoubleObjMap<List<MultiUserWeightedEdge<Integer, Double, double[]>>> edgesByLabel);
    
    protected abstract BucketList computeBucketList(List<double[]> edgeWeights);
    
    public abstract HashIntObjMap<double[]> createFeatureVectors(HashDoubleObjMap<List<MultiUserWeightedEdge<Integer, Double, double[]>>> edgesByLabel);

}
