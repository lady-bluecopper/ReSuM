package eu.unitn.disi.db.resum.clustering.features;

import com.koloboke.collect.map.hash.HashIntObjMap;
import com.koloboke.collect.map.hash.HashIntObjMaps;
import eu.unitn.disi.db.resum.utilities.Settings;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.IntStream;
import sa.edu.kaust.grami.dataStructures.MultiUserWeightedEdge;

/**
 *
 * @author bluecopper
 */
public class PathBasedFVCreator extends FVCreator {

    ArrayList<MultiUserWeightedEdge<Integer, Double, double[]>> edges;
    // nodeId -> list of edge ids
    HashIntObjMap<ArrayList<Integer>> neighbors;

    public PathBasedFVCreator() throws IOException {
        super();
        loadGraph();
    }

    private void loadGraph() throws FileNotFoundException, IOException {
        final BufferedReader rows = new BufferedReader(new FileReader(Paths.get(Settings.datasetsFolder, Settings.inputFileName).toFile()));
        edges = new ArrayList<MultiUserWeightedEdge<Integer, Double, double[]>>();
        neighbors = HashIntObjMaps.<ArrayList<Integer>>newUpdatableMap();

        String line = rows.readLine();
        int counter = 0;
        while (line != null) {
            if (line.startsWith("e")) {
                final String[] parts = line.split("\\s+");
                final int nodeA = Integer.parseInt(parts[1]);
                final int nodeB = Integer.parseInt(parts[2]);
                final double label = Double.parseDouble(parts[3]);
                neighbors.putIfAbsent(nodeA, new ArrayList<Integer>());
                neighbors.get(nodeA).add(counter);
                edges.add(counter, new MultiUserWeightedEdge<Integer, Double, double[]>(nodeB, label, edgeWeights.get(counter)));
                counter++;
            }
            line = rows.readLine();
        }
        rows.close();
    }

    public ArrayList<ArrayList<Double>> createFeatureVectors() {
        ArrayList<ArrayList<Double>> patternScores = new ArrayList<ArrayList<Double>>(Settings.numberOfFunctions);
        for (int u = 0; u < Settings.numberOfFunctions; u++) {
            patternScores.add(u, new ArrayList<Double>());
        }
        HashMap<String, Integer> patternIDs = new HashMap<String, Integer>();
        int patternCount = 0;
        for (int nodeA : neighbors.keySet()) {
            ArrayList<Integer> outEdges = neighbors.get(nodeA);
            for (int edgeId : outEdges) {
                MultiUserWeightedEdge<Integer, Double, double[]> edge = edges.get(edgeId);
                int nodeB = edge.getNodeID();
                String pathLabel = edge.getEdgeLabel().toString();
                double[] firstEdgeWeights = edge.getMaxWeights();
                if (patternIDs.putIfAbsent(pathLabel, patternCount) == null) {
                    patternCount++;
                    IntStream.range(0, Settings.numberOfFunctions).parallel().forEach(u
                            -> patternScores.get(u).add(patternIDs.get(pathLabel), 0.));
                }
                // compute scores of paths of length 1
                if (Settings.task < 4) {
                    IntStream.range(0, Settings.numberOfFunctions).parallel().forEach(user -> {
                        if (firstEdgeWeights[user] > Settings.relevance) {
                            ArrayList<Double> thisUser = patternScores.get(user);
                            int pID = patternIDs.get(pathLabel);
                            thisUser.set(pID, thisUser.get(pID) + 1);
                        }
                    });
                } else {
                    IntStream.range(0, Settings.numberOfFunctions).parallel().forEach(user -> {
                        ArrayList<Double> thisUser = patternScores.get(user);
                        int pID = patternIDs.get(pathLabel);
                        thisUser.set(pID, thisUser.get(pID) + firstEdgeWeights[user]);
                    });
                }
                // compute scores of paths of length 2
                ArrayList<Integer> secondOutEdges = neighbors.get(nodeB);
                if (secondOutEdges != null) {
                    for (int secondEdgeId : neighbors.get(nodeB)) {
                        MultiUserWeightedEdge<Integer, Double, double[]> secondEdge = edges.get(secondEdgeId);
                        String path2Label = pathLabel + "-" + secondEdge.getEdgeLabel().toString();
                        double[] secondEdgeWeights = secondEdge.getMaxWeights();
                        if (patternIDs.putIfAbsent(path2Label, patternCount) == null) {
                            patternCount++;
                            IntStream.range(0, Settings.numberOfFunctions).parallel().forEach(u
                                    -> patternScores.get(u).add(patternIDs.get(path2Label), 0.));
                        }
                        switch (Settings.task) {
                            case 1:
                                IntStream.range(0, Settings.numberOfFunctions).parallel().forEach(user -> {
                                    if (firstEdgeWeights[user] > Settings.relevance && secondEdgeWeights[user] > Settings.relevance) {
                                        ArrayList<Double> thisUser = patternScores.get(user);
                                        int pID = patternIDs.get(path2Label);
                                        thisUser.set(pID, thisUser.get(pID) + 1);
                                    }
                                });
                                break;
                            case 2:
                                IntStream.range(0, Settings.numberOfFunctions).parallel().forEach(user -> {
                                    if (firstEdgeWeights[user] > Settings.relevance || secondEdgeWeights[user] > Settings.relevance) {
                                        ArrayList<Double> thisUser = patternScores.get(user);
                                        int pID = patternIDs.get(path2Label);
                                        thisUser.set(pID, thisUser.get(pID) + 1);
                                    }
                                });
                                break;
                            case 3:
                                IntStream.range(0, Settings.numberOfFunctions).parallel().forEach(user -> {
                                    if (firstEdgeWeights[user] + secondEdgeWeights[user] > Settings.relevance) {
                                        ArrayList<Double> thisUser = patternScores.get(user);
                                        int pID = patternIDs.get(path2Label);
                                        thisUser.set(pID, thisUser.get(pID) + 1);
                                    }
                                });
                                break;
                            case 4:
                                IntStream.range(0, Settings.numberOfFunctions).parallel().forEach(user -> {
                                    ArrayList<Double> thisUser = patternScores.get(user);
                                    int pID = patternIDs.get(path2Label);
                                    thisUser.set(pID, thisUser.get(pID) + (firstEdgeWeights[user] + secondEdgeWeights[user]) / 2);
                                });
                                break;
                        }
                    }
                }
            }
        }

        return patternScores;
    }

}
