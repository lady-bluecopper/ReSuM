package eu.unitn.disi.db.resum.clustering;

import com.koloboke.collect.map.hash.HashIntObjMap;
import eu.unitn.disi.db.resum.distance.Distance;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Greedy clustering algorithm. This algorithm goes through the graph and if it
 * is advantageous to add the current node to an existing cluster, it adds it to
 * the cluster which best satisfies its cost function. If not, it creates a new
 * cluster containing that node. The cost function is the number of edges
 * entering that cluster from the node.
 *
 * @author Michael Shilman (michaels@eecs.berkeley.edu)
 * @version $Id$
 */
public class GreedyClusterer extends Clusterer {

    static Random s_rand = new Random(System.currentTimeMillis());
    // Determines whether it is advantageous to add a node to a cluster
    public double m_thresh = 0.0f;

    public GreedyClusterer(HashIntObjMap adjacencyMatrix, Distance distance) {
        super(adjacencyMatrix, distance);
    }

    public GreedyClusterer(HashIntObjMap adjacencyMatrix, double thresh, Distance distance) {
        super(adjacencyMatrix, distance);
        m_thresh = thresh;
    }

    public int[] findClustering(int dummy) {
        List<Integer> nodes = IntStream.range(0, usersNum).boxed().collect(Collectors.toList());
        ArrayList<ArrayList<Integer>> dest = new ArrayList<ArrayList<Integer>>();
        while (nodes.size() > 0) {
            //Choose a random node
            int randId = (int) (s_rand.nextFloat() * nodes.size());
            int nId = nodes.get(randId);
            //Try placing this node into existing clusters
            ArrayList<Integer> maxSub = null;
            double maxGain = m_thresh;
            for (ArrayList<Integer> cl : dest) {
                double gain = calcGain(nId, cl);
                if (gain > maxGain) {
                    maxGain = gain;
                    maxSub = cl;
                }
            }
            if (maxSub != null) {
                nodes.remove(nId);
                maxSub.add(nId);
            } else {
                nodes.remove(nId);
                maxSub.add(nId);
                dest.add(maxSub);
            }
        }
        int[] clustering = new int[usersNum];
        for (int i = 0; i < dest.size(); i ++) {
            ArrayList<Integer> current = dest.get(i);
            for (int el : current) {
                clustering[el] = i;
            }
        }
        return clustering;
    }

    /**
     * Calculate the gain incurred by adding the specified node to the specified
     * graph.
     *
     * @param n	Node to be added.
     * @param c	Graph to which the node is added.
     * @return	The gain of the graph, based on the sum total of the edges
     */
    private double calcGain(int n, ArrayList<Integer> c) {
        double gain = 0;
        gain = c.stream()
                .map((el) -> distance.distance(featureMap.get(el), featureMap.get(n)))
                .filter((current) -> (current > -1))
                .map((current) -> current)
                .reduce(gain, (accumulator, _item) -> accumulator + _item);
        return gain;
    }
}
