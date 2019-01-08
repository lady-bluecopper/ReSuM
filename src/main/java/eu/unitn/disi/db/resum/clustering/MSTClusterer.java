package eu.unitn.disi.db.resum.clustering;

import com.koloboke.collect.map.hash.HashIntObjMap;
import eu.unitn.disi.db.resum.distance.Distance;
import eu.unitn.disi.db.resum.distance.JaccardSimilarity;
import eu.unitn.disi.db.resum.mst.DistinctSet;
import eu.unitn.disi.db.resum.mst.Link;
import eu.unitn.disi.db.resum.mst.LinkComparator;
import eu.unitn.disi.db.resum.mst.KruskalAlgorithm;
import eu.unitn.disi.db.resum.utilities.Pair;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 *
 * @author bluecopper
 */
public class MSTClusterer extends Clusterer {

    private ArrayList<Link> edgeSet;
    
    private double alpha;
    
    private double step;
    

    public MSTClusterer(HashIntObjMap patternSets, int patternsNum, Distance distance) {
        this(patternSets, patternsNum, distance, 200, 1);
    }
    
    public MSTClusterer(HashIntObjMap patternSets, int patternsNum, Distance distance, double alpha, double step) {
        super(patternSets, patternsNum, distance);
        this.alpha = alpha;
        this.step = step;
        this.edgeSet = createInitialEdgeSet();
    }

    public Pair<int[], Integer> findClusteringWithAlpha(double thisAlpha) {
        // initialization
        DistinctSet temp = new DistinctSet();
        HashMap<Integer, Integer> clustered = new HashMap<Integer, Integer>();
        HashSet<Integer> toCluster = new HashSet<Integer>(IntStream.range(0, usersNum).boxed().collect(Collectors.toList()));
        int counter = 0;
        Queue<Integer> reachableNodes;
        int numClusters;
        // remove all the edges with weight above the current threshold
        for (Link e : edgeSet) {
            if (e.getDistance() <= thisAlpha) {
                temp.addLink(e);
            } else {
                counter++;
            }
        }
        numClusters = counter + 1;
        counter = 0;
        // find cluster mapping
        for (int node = 0; node < usersNum; node++) {
            if (toCluster.contains(node)) {
                reachableNodes = new LinkedList<Integer>();
                clustered.put(node, counter);
                toCluster.remove(node);
                ArrayList<Integer> toBeRemoved = new ArrayList<Integer>();
                for (int k : toCluster) {
                    // assign same cluster to neighbors
                    if (temp.containsLink(node, k)) {
                        clustered.put(k, counter);
                        toBeRemoved.add(k);
                        reachableNodes.add(k);
                    }
                }
                toCluster.removeAll(toBeRemoved);
                // iterate over neighbors of entity edgeRemoved[counter]
                toBeRemoved = new ArrayList<Integer>();
                while (!reachableNodes.isEmpty()) {
                    int currNode = reachableNodes.poll();
                    for (int k : toCluster) {
                        if (temp.containsLink(currNode, k)) {
                            clustered.put(k, counter);
                            toBeRemoved.add(k);
                            reachableNodes.add(k);
                        }
                    }
                    toCluster.removeAll(toBeRemoved);
                }
                counter ++;
            }
        }
        int[] clusterMapping = new int[usersNum];
        if (numClusters > 1) {
            clustered.keySet().stream().forEach(k -> clusterMapping[k] = clustered.get(k));
        } else {
            for (int k = 0; k < usersNum; k++) {
                clusterMapping[k] = 0;
            }
        }
        return new Pair<int[], Integer>(clusterMapping, numClusters);
    }
    
    public int[] findClustering_BIS(int clustersNum) {
        // initialization
        DistinctSet temp = new DistinctSet();
        HashMap<Integer, Integer> clustered = new HashMap<Integer, Integer>();
        int counter = 0;
        int node;
        int k;
        Queue<Integer> reachableNodes;
        Collections.sort(edgeSet, new LinkComparator().reversed());
        // remove all the edges with weight above the current threshold
        temp.addAllLinks(edgeSet.subList(clustersNum - 1, edgeSet.size()));
        // find cluster mapping
        for (node = 0; node < usersNum; node++) {
            if (clustered.get(node) == null) { 
                reachableNodes = new LinkedList<Integer>();
                clustered.put(node, counter);
                for (k = node + 1; k < usersNum; k ++) {
                    // assign same cluster to neighbors
                    if (clustered.get(k) == null && temp.containsLink(node, k)) {
                        clustered.put(k, counter);
                        reachableNodes.add(k);
                    }
                }
                while (!reachableNodes.isEmpty()) {
                    int currNode = reachableNodes.poll();
                    for (k = node + 1; k < usersNum; k ++) {
                        if (clustered.get(k) == null && temp.containsLink(currNode, k)) {
                            clustered.put(k, counter);
                            reachableNodes.add(k);
                        }
                    }
                }
                counter ++;
            }
        }
        int[] clusterMapping = new int[usersNum];
        if (clustersNum > 1) {
            clustered.keySet().stream().forEach(n -> clusterMapping[n] = clustered.get(n));
        } else {
            for (k = 0; k < usersNum; k++) {
                clusterMapping[k] = 0;
            }
        }
        return clusterMapping;
    }
    
    public int[] findClustering(int clustersNum) {
        // initialization
        DistinctSet temp = new DistinctSet();
        HashMap<Integer, Integer> clustered = new HashMap<Integer, Integer>();
        HashSet<Integer> toCluster = new HashSet<Integer>(IntStream.range(0, usersNum).boxed().collect(Collectors.toList()));
        int counter = 0;
        Queue<Integer> reachableNodes;
        Collections.sort(edgeSet, new LinkComparator().reversed());
        // remove all the edges with weight above the current threshold
        temp.addAllLinks(edgeSet.subList(clustersNum - 1, edgeSet.size()));
        // find cluster mapping
        for (int node = 0; node < usersNum; node++) {
            if (toCluster.contains(node)) { 
                reachableNodes = new LinkedList<Integer>();
                clustered.put(node, counter);
                toCluster.remove(node);
                ArrayList<Integer> toBeRemoved = new ArrayList<Integer>();
                for (int k : toCluster) {
                    // assign same cluster to neighbors
                    if (temp.containsLink(node, k)) {
                        clustered.put(k, counter);
                        toBeRemoved.add(k);
                        reachableNodes.add(k);
                    }
                }
                toCluster.removeAll(toBeRemoved);
                // iterate over neighbors of entity edgeRemoved[counter]
                toBeRemoved = new ArrayList<Integer>();
                while (!reachableNodes.isEmpty()) {
                    int currNode = reachableNodes.poll();
                    for (int k : toCluster) {
                        if (temp.containsLink(currNode, k)) {
                            clustered.put(k, counter);
                            toBeRemoved.add(k);
                            reachableNodes.add(k);
                        }
                    }
                    toCluster.removeAll(toBeRemoved);
                }
                counter ++;
            }
        }
        int[] clusterMapping = new int[usersNum];
        if (clustersNum > 1) {
            clustered.keySet().stream().forEach(k -> clusterMapping[k] = clustered.get(k));
        } else {
            for (int k = 0; k < usersNum; k++) {
                clusterMapping[k] = 0;
            }
        }
        return clusterMapping;
    }

    @Override
    public int[] findBestClustering(int dummy) {
        // initialization
        boolean stop = false;
        ArrayList<int[]> clustering = new ArrayList<int[]>();
        double maxQual = 0;
        int bestC = -1;

        while (!stop) {
            Pair<int[], Integer> currentClustering = findClusteringWithAlpha(alpha);
            int[] clusterMapping = currentClustering.getA();
            double currQual = qualityMeasure.quality(clusterMapping, currentClustering.getB());
            if (currQual >= maxQual) {
                clustering.add(clusterMapping);
                maxQual = currQual;
                bestC = clustering.indexOf(clusterMapping);
            }
            if (currentClustering.getB() >= usersNum) {
                stop = true;
            }
            alpha -= step;
        }
        return clustering.get(bestC);
    }
    
    protected ArrayList<Link> createInitialEdgeSet() {
        KruskalAlgorithm MSTFinder = KruskalAlgorithm.istantiateFromPatternSets(featureMap, usersNum, new JaccardSimilarity());
        MSTFinder.findMST();
        return MSTFinder.getMST();
    }
}