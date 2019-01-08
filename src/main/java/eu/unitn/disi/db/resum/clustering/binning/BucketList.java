package eu.unitn.disi.db.resum.clustering.binning;

import java.util.Arrays;

/**
 *
 * @author bluecopper
 */
public class BucketList {

    private double[] boundaries;
    private int size;
    
    public BucketList(int numBuckets) {
        this.boundaries = new double[numBuckets];
        this.size = numBuckets;
        Arrays.fill(this.boundaries, 1);
    }
    
    public double[] getBoundaries() {
        return boundaries;
    }
    
    public int getSize() {
        return size;
    }
    
    public void setBoundary(int index, double value) {
        boundaries[index] = value;
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
        size = newSize;
    }
    
}
