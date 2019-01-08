package eu.unitn.disi.db.resum.clustering.features;

import com.koloboke.collect.map.hash.HashIntObjMap;

/**
 *
 * @author bluecopper
 */
public abstract class FVCreator {
    
    public abstract HashIntObjMap<double[]> createFeatureVectors();
    
}
