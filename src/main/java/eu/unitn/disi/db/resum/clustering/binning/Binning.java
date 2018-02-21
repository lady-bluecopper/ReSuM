package eu.unitn.disi.db.resum.clustering.binning;

import com.koloboke.collect.map.hash.HashDoubleObjMap;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author bluecopper
 */
public abstract class Binning {

    protected final int labelsNum;

    public Binning(int labelsNum) {
        this.labelsNum = labelsNum;
    }
    
    protected abstract HashDoubleObjMap<BucketList> computeBinningMap(
            HashMap<Double, ArrayList<Integer>> edgesByLabel,
            ArrayList<double[]> edgeWeightsByIndex);
    
    protected abstract BucketList computeBucketList(
            ArrayList<double[]> edgeWeightsByIndex,
            ArrayList<Integer> edges);
    
    public abstract ArrayList<ArrayList<Double>> createFeatureVectors(
            HashMap<Double, ArrayList<Integer>> edgesByLabel,
            ArrayList<double[]> edgeWeightsByIndex);

}
