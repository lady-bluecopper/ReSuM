package eu.unitn.disi.db.resum.mst;

import com.koloboke.collect.map.hash.HashIntObjMap;
import eu.unitn.disi.db.resum.distance.Distance;
import java.util.*;

public class KruskalAlgorithm {

    private final int usersNum;
    private final ArrayList<Link> edgeSet;
    private final ArrayList<Link> MSTree;

    public KruskalAlgorithm(ArrayList<Link> edgeSet, int usersNum) {
        this.usersNum = usersNum;
        this.edgeSet = edgeSet;
        this.MSTree = new ArrayList<Link>();
    }
    
    // A class to represent a subset for union-find
    class Subset {
        int parent;
        int rank;
    };

    // A function to find set of an element i
    private int find(Subset subsets[], int i) {
        // find root and make root as parent of i (path compression)
        if (subsets[i].parent != i) {
            subsets[i].parent = find(subsets, subsets[i].parent);
        }
        return subsets[i].parent;
    }
    // A function that does union of two sets of x and y
    private void union(Subset subsets[], int x, int y) {
        int xroot = find(subsets, x);
        int yroot = find(subsets, y);

        // Attach smaller rank tree under root of high rank tree
        if (subsets[xroot].rank < subsets[yroot].rank) {
            subsets[xroot].parent = yroot;
        } else if (subsets[xroot].rank > subsets[yroot].rank) {
            subsets[yroot].parent = xroot;
        } // If ranks are same, then make one as root and increment
        // its rank by one
        else {
            subsets[yroot].parent = xroot;
            subsets[xroot].rank++;
        }
    }

    public void findMST() {
        int taken = 0;
        int next = 0;
        // Sort all the edges in non-decreasing order of weight 
        Collections.sort(edgeSet, new LinkComparator());
        // Allocate memory for creating V subsets
        Subset subsets[] = new Subset[usersNum];
        for (int i = 0; i < usersNum; i++) {
            subsets[i] = new Subset();
            subsets[i].parent = i;
            subsets[i].rank = 0;
        }
        // Number of edges to be taken is equal to V-1
        while (taken < usersNum - 1) {
            // Pick the smallest edge
            Link next_edge = edgeSet.get(next);
            next ++;
            int x = find(subsets, next_edge.src);
            int y = find(subsets, next_edge.dst);
            // If including this edge does't cause cycle, include it in result
            if (x != y) {
                MSTree.add(next_edge);
                union(subsets, x, y);
                taken ++;
            }
        }
    }
    
    public ArrayList<Link> getMST() {
        return MSTree;
    }
    
    private static ArrayList<Link> createEdgeSet(HashIntObjMap patternSets, int usersNum, Distance distance) {
        ArrayList<Link> edges = new ArrayList<Link>();
        for (int i = 0; i < usersNum - 1; i++) {
            for (int j = i + 1; j < usersNum; j++) {
                double dist = distance.distance(patternSets.get(i), patternSets.get(j));
                edges.add(new Link(i, j, dist));
                edges.add(new Link(j, i, dist));
            }
        }
        return edges;
    }
    
    public static KruskalAlgorithm istantiateFromPatternSets(HashIntObjMap patternSet, int usersNum, Distance distance) {
        return new KruskalAlgorithm(createEdgeSet(patternSet, usersNum, distance), usersNum);
    }
    
}
