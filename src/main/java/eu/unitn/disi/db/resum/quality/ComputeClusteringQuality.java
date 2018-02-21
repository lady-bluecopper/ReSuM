package eu.unitn.disi.db.resum.quality;

import eu.unitn.disi.db.resum.clustering.quality.SilhouetteCoefficient;
import eu.unitn.disi.db.resum.distance.Distance;
import eu.unitn.disi.db.resum.distance.JaccardSimilarity;
import eu.unitn.disi.db.resum.utilities.CommandLineParser;
import eu.unitn.disi.db.resum.utilities.MyTriplet;
import eu.unitn.disi.db.resum.utilities.Settings;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.stream.IntStream;

/**
 *
 * @author bluecopper
 */
public class ComputeClusteringQuality {
    
    static int[] TASKS = new int[]{1, 2, 3};
    static MyTriplet<String, Integer, Double>[] DATASETS = new MyTriplet[]{
//        new MyTriplet("freebase-travel", 90, 0.05),
//        new MyTriplet("freebase-computer", 155, 0.05),
        new MyTriplet("electronics-2-pruned", 130, 0.001)};
//    static int[] USERS = new int[]{50, 500, 5000, 50000};
    static int[] USERS = new int[]{30, 299};

    public static void main(String[] args) throws IOException {
        // parse the command line arguments
        CommandLineParser.parse(args);
        for (MyTriplet<String, Integer, Double> dataset : DATASETS) {
            for (int t : TASKS) {
                for (int u : USERS) {
                    String setting = String.format("%s\t%d\t%d", dataset.toString(), t, u);
//                    String clusteringFile = Settings.clusteringFileName + "/u" + u + "/clustering/" + dataset.getA() + "_50_" + (u / 10) + ".cl";
                    String clusteringFile = Settings.clusteringFileName + "/u" + u + "/clustering/T" + t + "/" + dataset.getA() + "_50_" + (u / 10) + ".cl";
                    String patternFile = Settings.exactFileName + "/u" + u + "/scores/Patterns_" + dataset.getA() + ".lg_F" + dataset.getB() + "R" + dataset.getC() + "T" + t + "C" + u + "RUN0.p";
                    // extract a-posteriori clustering
                    int[] clustering = extractClustering(clusteringFile, u);
                    // extract pattern sets
                    Double[][] adjacencyMatrix = createAdjacencyMatrix(patternFile, u);
                    
                    SilhouetteCoefficient silhouette = new SilhouetteCoefficient(adjacencyMatrix);
                    
                    if (adjacencyMatrix != null && clustering != null) {
                        writeStats(String.format("%s\t%f", 
                            setting, silhouette.quality(clustering, u)));
                    }
                }
            } 
        }
        
        
    }

    protected static HashSet<Integer>[] extractPatternSets(String fileName, int u) throws IOException {
        HashSet<Integer>[] patternSets = new HashSet[u];
        for (int i = 0; i < patternSets.length; i++) {
            patternSets[i] = new HashSet<Integer>();
        }
        try (BufferedReader rows = new BufferedReader(new FileReader(Paths.get(fileName).toFile()))) {
            int counter = 0;
            String line = rows.readLine();
            while (line != null) {
                if (line.startsWith("#P")) {
                    line = rows.readLine().trim();
                    String[] list = line.substring(1, line.length() - 1).split(",");
                    for (String el : list) {
                        int elID = Integer.parseInt(el.trim());
                        patternSets[elID].add(counter);
                    }
                    counter ++;
                }
                line = rows.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return patternSets;
    }

    protected static int[] extractClustering(String fileName, int u) {
        int[] clustering = new int[u];
        try (BufferedReader rows = new BufferedReader(new FileReader(Paths.get(fileName).toFile()))) {
            String line = rows.readLine();

            while (line != null) {
                String[] parts = line.trim().split("\t");
                clustering[Integer.parseInt(parts[0].trim())] = Integer.parseInt(parts[1].trim());
                line = rows.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return clustering;
    }
    
    protected static Double[][] createAdjacencyMatrix(String fileName, int u) throws IOException {
        HashSet<Integer>[] realPatternSets = extractPatternSets(fileName, u);
        if (realPatternSets != null) {
            Double[][] adjacencyMatrix = new Double[u][u];
            Distance distance = new JaccardSimilarity();
            IntStream.range(0, u - 1).parallel().forEach(i -> {
                for (int j = i + 1; j < u; j++) {
                    double dist = distance.distance(realPatternSets[i], realPatternSets[j]);
                    adjacencyMatrix[i][j] = dist;
//                    adjacencyMatrix[j][i] = dist;
                }
            });
            return adjacencyMatrix;
        }
        return null;
    }
    
    protected static double computeQuality(Double[][] adjacencyMatrix, int[] clustering, int u) {
        int clustersNum = u / 10;
        double[][] distanceMatrix = new double[clustersNum][clustersNum];
        int[][] pairs = new int[clustersNum][clustersNum];
        double maxIntraDistance = -1;
        double minInterDistance = Double.MAX_VALUE;
        
        for (int i = 0; i < u - 1; i++) {
            for (int j = i + 1; j < u; j++) {
                if (adjacencyMatrix[i][j] >= 0) {
                    distanceMatrix[clustering[i]][clustering[j]] += adjacencyMatrix[i][j];
                    pairs[clustering[i]][clustering[j]] ++;
                    if (clustering[i] != clustering[j]) {
                        pairs[clustering[j]][clustering[i]] ++;
                        distanceMatrix[clustering[j]][clustering[i]] += adjacencyMatrix[i][j];
                    }
                }
            }
        }
        for (int i = 0; i < clustersNum - 1; i ++) {
            if (pairs[i][i] > 0) {
                maxIntraDistance = Math.max(maxIntraDistance, distanceMatrix[i][i] / pairs[i][i]);
            }
            for (int j = i + 1; j < clustersNum; j ++) {
                if (pairs[i][j] > 0) {
                    minInterDistance = Math.min(minInterDistance, distanceMatrix[i][j] / pairs[i][j]);
                }
            }
        }
        if (maxIntraDistance <= 0) {
            return minInterDistance;
        }
        return minInterDistance / maxIntraDistance;
    }
    
    protected static void writeStats(String setting) throws IOException {
        FileWriter fw = null;
        try {
            String fName;
            if (Settings.outputFileName == null) {
                fName = "clustering_quality.csv";
            } else {
                fName = Settings.outputFileName;
            }
            Path path = Paths.get(Settings.outputFolder, fName);
            fw = new FileWriter(path.toFile(), true);
            fw.write(String.format("%s\n", setting));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fw != null) {
                fw.close();
            }
        }
    }
    
}
