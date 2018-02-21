package eu.unitn.disi.db.resum.clustering;

import eu.unitn.disi.db.resum.clustering.features.BucketBasedFVCreator;
import eu.unitn.disi.db.resum.clustering.features.FVCreator;
import eu.unitn.disi.db.resum.clustering.features.FullFVCreator;
import eu.unitn.disi.db.resum.clustering.features.PathBasedFVCreator;
import eu.unitn.disi.db.resum.clustering.features.PatternBasedFVCreator;
import eu.unitn.disi.db.resum.distance.CosineSimilarity;
import eu.unitn.disi.db.resum.distance.JaccardSimilarity;
import eu.unitn.disi.db.resum.utilities.Settings;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import static java.lang.Integer.min;
import java.nio.file.Paths;
import java.util.ArrayList;
import eu.unitn.disi.db.resum.utilities.CommandLineParser;
import eu.unitn.disi.db.resum.utilities.StopWatch;
import eu.unitn.disi.db.resum.utilities.Util;

/**
 *
 * @author bluecopper
 */
public class KGraphGen {

    private static double[][] maxWeightsMap;

    private static double[][] minWeightsMap;

    public static void main(final String[] args)
            throws FileNotFoundException, IOException {

        CommandLineParser.parse(args);
        StopWatch watch = new StopWatch();
        FVCreator fvCreator;
        Clusterer clusterer;
        int[] clustering;

        watch.start();
        switch(Settings.clusteringType) {
            case "aposteriori":
                fvCreator = new PatternBasedFVCreator();
                clusterer = new KMedoids(fvCreator.createFeatureVectors(), new JaccardSimilarity());
                break;
            case "full":
                fvCreator = new FullFVCreator();
                clusterer = new KMeans(fvCreator.createFeatureVectors(), new CosineSimilarity());
                break;
            case "bucket":
                fvCreator = new BucketBasedFVCreator();
                clusterer = new KMeans(fvCreator.createFeatureVectors(), new CosineSimilarity());
                break;
            case "path":
                fvCreator = new PathBasedFVCreator();
                clusterer = new KMeans(fvCreator.createFeatureVectors(), new CosineSimilarity());
                break;
            default:
                fvCreator = new BucketBasedFVCreator();
                clusterer = new KMeans(fvCreator.createFeatureVectors(), new CosineSimilarity());
                break;
        }
        // Find Clusters
        if (Settings.best) {
            clustering = clusterer.findBestClustering(Settings.maxClustersNum);
        } else if (Settings.random) {
            clustering = clusterer.findRandomClustering(Settings.structureSize);
        } else {
            clustering = clusterer.findClustering(Settings.structureSize);
        }
        watch.stop();
        // Compute Aggregate Weights
        StopWatch watch2 = new StopWatch();
        watch2.start();
        generateAggregateWeights(fvCreator.getEdgeWeights(), clustering);
        watch2.stop();
        // Write Results
        writeKEdgeWeightLists(clustering);
        double[][] spread = computeMaxMinSpread();
        double[] avgSpread = new double[spread[0].length];
        for (double[] spread1 : spread) {
            for (int j = 0; j < spread1.length; j++) {
                avgSpread[j] += spread1[j];
            }
        }
        for (int j = 0; j < avgSpread.length; j++) {
            avgSpread[j] /= spread.length;
        }
        System.out.println("#AvgMaxMinSpread\n" + Util.toPrint(avgSpread));
        System.out.println("#Clustering_Time\n" + watch.getElapsedTime() / 1000.0);
        System.out.println("#Generation_Time\n" + watch2.getElapsedTime() / 1000.0);
    }

    private static void generateAggregateWeights(ArrayList<double[]> edgeWeightsByIndex, int[] clustering) {
        maxWeightsMap = new double[Settings.structureSize][edgeWeightsByIndex.size()];
        minWeightsMap = new double[Settings.structureSize][edgeWeightsByIndex.size()];
        for (int edge = 0; edge < edgeWeightsByIndex.size(); edge++) {
            double[] edgeWeights = edgeWeightsByIndex.get(edge);
            for (int j = 0; j < edgeWeights.length; j++) {
                int currentCluster = clustering[j];
                maxWeightsMap[currentCluster][edge]
                        = Math.max(maxWeightsMap[currentCluster][edge], edgeWeights[j]);
                minWeightsMap[currentCluster][edge]
                        = Math.min(minWeightsMap[currentCluster][edge], edgeWeights[j]);
            }
        }
    }

    private static double[][] computeMaxMinSpread() {
        // [0 - 25; 25 - 50; 50 - 75; 75 - 100]
        double[][] differences = new double[maxWeightsMap.length][4];
        for (int cluster = 0; cluster < maxWeightsMap.length; cluster++) {
            for (int edgeIdx = 0; edgeIdx < maxWeightsMap[0].length; edgeIdx++) {
                int bucket = min((int) ((maxWeightsMap[cluster][edgeIdx] - minWeightsMap[cluster][edgeIdx]) * 100) / 25, 3);
                differences[cluster][bucket] += 1;
            }
        }
        return differences;
    }

    private static void writeKEdgeWeightLists(int[] clustering)
            throws FileNotFoundException, IOException {

        FileWriter KWG = new FileWriter(Paths.get(Settings.outputFolder,
                Settings.inputFileName.substring(0, Settings.inputFileName.length() - 3)
                + "_" + Settings.focus
                + "_" + Settings.structureSize + ".cw").toFile());
        FileWriter clustMap = new FileWriter(Paths.get(Settings.outputFolder,
                Settings.inputFileName.substring(0, Settings.inputFileName.length() - 3)
                + "_" + Settings.focus
                + "_" + Settings.structureSize + ".cl").toFile());
        for (int i = 0; i < clustering.length; i++) {
            // function cluster
            clustMap.write(i + "\t" + clustering[i] + "\n");
        }
        clustMap.close();
        for (int i = 0; i < Settings.structureSize; i++) {
            String weights[] = new String[maxWeightsMap[i].length];
            for (int e = 0; e < maxWeightsMap[i].length; e++) {
                weights[e] = Double.toString(maxWeightsMap[i][e]);
            }
            KWG.write(String.join(" ", weights) + "\n");
        }
        KWG.close();
    }
}
