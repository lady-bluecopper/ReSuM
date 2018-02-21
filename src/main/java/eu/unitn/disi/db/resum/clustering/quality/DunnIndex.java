package eu.unitn.disi.db.resum.clustering.quality;

import eu.unitn.disi.db.resum.distance.CompleteLinkDistance;
import eu.unitn.disi.db.resum.distance.SingleLinkDistance;

/**
 *
 * @author bluecopper
 */
public class DunnIndex extends QualityMeasure {
    
    SingleLinkDistance clusterDistance;
    CompleteLinkDistance intraClusterDistance;
    
    public DunnIndex(Double[][] adjacencyMatrix) {
        super(adjacencyMatrix);
        clusterDistance = new SingleLinkDistance(adjacencyMatrix);
        intraClusterDistance = new CompleteLinkDistance(adjacencyMatrix);
    }
    
    public double quality(int[] clustering, int clustersNum) {
        double dmin = clusterDistance.computeMinInterClusterDistance(clustering);
        double dmax = intraClusterDistance.computeMaxIntraClusterDistance(clustering);
        if (dmax > 0) {
            return dmin / dmax;
        }
        return 0;
    }
    
}
