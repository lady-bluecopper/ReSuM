/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.unitn.disi.db.resum.clustering.binning;

import java.util.Arrays;

/**
 *
 * @author bluecopper
 */
public class BucketList {

    public double[] boundaries;

    public BucketList(int numBuckets) {
        this.boundaries = new double[numBuckets];
        Arrays.fill(this.boundaries, 1);

    }

    public int getBucket(double edgeWeight) {
        for (int i = 0; i < boundaries.length; i++) {
            if (edgeWeight <= boundaries[i]) {
                return i;
            }
        }
        // should never happen
        return -1;
    }
    
    public void resizeBucketList(int newSize) {
        boundaries = Arrays.copyOf(boundaries, newSize + 1);
    }

}
