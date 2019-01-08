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
import java.util.List;
import java.util.stream.IntStream;
import sa.edu.kaust.grami.dataStructures.MultiUserWeightedEdge;

/**
 *
 * @author bluecopper
 */
public class PathBasedFVCreator extends FVCreator {

    List<MultiUserWeightedEdge<Integer, Double, double[]>> edges;
    // nodeId -> list of edge ids
    HashIntObjMap<List<Integer>> neighbors;

    public PathBasedFVCreator(HashIntObjMap<double[]> edgeWeights) throws IOException {
        loadGraph(edgeWeights);
    }

    private void loadGraph(HashIntObjMap<double[]> edgeWeights) throws IOException {
        final BufferedReader rows = new BufferedReader(new FileReader(Paths.get(Settings.datasetsFolder, Settings.inputFileName).toFile()));
        edges = new ArrayList<MultiUserWeightedEdge<Integer, Double, double[]>>();
        neighbors = HashIntObjMaps.newMutableMap();

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

    public HashIntObjMap<double[]> createFeatureVectors() {
        HashIntObjMap<double[]> patternScores = HashIntObjMaps.newMutableMap();
        IntStream.range(0, Settings.numberOfEdgeWeights).forEach(u -> patternScores.put(u, new double[10000]));

        HashMap<String, Integer> patternIDs = new HashMap<String, Integer>();
        int patternCount = 0;
        for (int nodeA : neighbors.keySet()) {
            List<Integer> outEdges = neighbors.getOrDefault(nodeA, new ArrayList<>());
            for (int edgeId : outEdges) {
                MultiUserWeightedEdge<Integer, Double, double[]> edge = edges.get(edgeId);
                int nodeB = edge.getNodeID();
                String pathLabel = edge.getEdgeLabel().toString();
                double[] firstEdgeWeights = edge.getMaxWeights();
                if (patternIDs.putIfAbsent(pathLabel, patternCount) == null) {
                    patternCount++;
                }
                // compute scores of paths of length 1
                if (Settings.score < 4) {
                    IntStream.range(0, Settings.numberOfEdgeWeights).parallel().forEach(user -> {
                        if (firstEdgeWeights[user] > Settings.relevance) {
                            double[] thisUser = patternScores.get(user);
                            thisUser[patternIDs.get(pathLabel)]++;
                            patternScores.put(user, thisUser);
                        }
                    });
                } else {
                    IntStream.range(0, Settings.numberOfEdgeWeights).parallel().forEach(user -> {
                        double[] thisUser = patternScores.get(user);
                        thisUser[patternIDs.get(pathLabel)] += firstEdgeWeights[user];
                        patternScores.put(user, thisUser);
                    });
                }
                // compute scores of paths of length 2
                List<Integer> secondOutEdges = neighbors.getOrDefault(nodeB, new ArrayList<Integer>());
                for (int secondEdgeId : secondOutEdges) {
                    MultiUserWeightedEdge<Integer, Double, double[]> secondEdge = edges.get(secondEdgeId);
                    String path2Label = pathLabel + "-" + secondEdge.getEdgeLabel().toString();
                    double[] secondEdgeWeights = secondEdge.getMaxWeights();
                    if (patternIDs.putIfAbsent(path2Label, patternCount) == null) {
                        patternCount++;
                    }
                    switch (Settings.score) {
                        case 1:
                            IntStream.range(0, Settings.numberOfEdgeWeights).parallel().forEach(user -> {
                                if (firstEdgeWeights[user] > Settings.relevance && secondEdgeWeights[user] > Settings.relevance) {
                                    double[] thisUser = patternScores.get(user);
                                    thisUser[patternIDs.get(path2Label)]++;
                                    patternScores.put(user, thisUser);
                                }
                            });
                            break;
                        case 2:
                            IntStream.range(0, Settings.numberOfEdgeWeights).parallel().forEach(user -> {
                                if (firstEdgeWeights[user] > Settings.relevance || secondEdgeWeights[user] > Settings.relevance) {
                                    double[] thisUser = patternScores.get(user);
                                    thisUser[patternIDs.get(path2Label)]++;
                                    patternScores.put(user, thisUser);
                                }
                            });
                            break;
                        case 3:
                            IntStream.range(0, Settings.numberOfEdgeWeights).parallel().forEach(user -> {
                                if (firstEdgeWeights[user] + secondEdgeWeights[user] > Settings.relevance) {
                                    double[] thisUser = patternScores.get(user);
                                    thisUser[patternIDs.get(path2Label)]++;
                                    patternScores.put(user, thisUser);
                                }
                            });
                            break;
                        case 4:
                            IntStream.range(0, Settings.numberOfEdgeWeights).parallel().forEach(user -> {
                                double[] thisUser = patternScores.get(user);
                                thisUser[patternIDs.get(path2Label)] += (firstEdgeWeights[user] + secondEdgeWeights[user]) / 2;
                                patternScores.put(user, thisUser);
                            });
                            break;
                    }
                }
            }
        }

        return patternScores;
    }

}
