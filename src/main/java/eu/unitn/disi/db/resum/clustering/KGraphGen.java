package eu.unitn.disi.db.resum.clustering;

import com.koloboke.collect.map.hash.HashIntObjMap;
import com.koloboke.collect.map.hash.HashIntObjMaps;
import eu.unitn.disi.db.resum.clustering.features.BucketBasedFVCreator;
import eu.unitn.disi.db.resum.clustering.features.FVCreator;
import eu.unitn.disi.db.resum.clustering.features.FullFVCreator;
import eu.unitn.disi.db.resum.clustering.features.PathBasedFVCreator;
import eu.unitn.disi.db.resum.clustering.features.PatternBasedFVCreator;
import eu.unitn.disi.db.resum.distance.CosineSimilarity;
import eu.unitn.disi.db.resum.distance.EuclideanDistance;
import eu.unitn.disi.db.resum.utilities.Settings;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import eu.unitn.disi.db.resum.utilities.CommandLineParser;
import eu.unitn.disi.db.resum.utilities.StopWatch;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.stream.IntStream;

/**
 *
 * @author bluecopper
 */
public class KGraphGen {

    public static void main(final String[] args)
            throws FileNotFoundException, IOException {

        CommandLineParser.parse(args);
        StopWatch watch = new StopWatch();
        HashIntObjMap<double[]> edgeWeights = readWeightFile();
        FVCreator fvCreator;
        Clusterer clusterer;
        int[] clustering;

        watch.start();
        switch(Settings.clusteringType) {
            case "aposteriori":
                fvCreator = new PatternBasedFVCreator();
                clusterer = new KMedoids(fvCreator.createFeatureVectors(), new EuclideanDistance());
                break;
            case "full":
                fvCreator = new FullFVCreator(edgeWeights);
                clusterer = new KMeans(fvCreator.createFeatureVectors(), new CosineSimilarity());
                break;
            case "path":
                fvCreator = new PathBasedFVCreator(edgeWeights);
                clusterer = new KMeans(fvCreator.createFeatureVectors(), new CosineSimilarity());
                break;
            case "bucket":
            case "dyn":
            default:
                fvCreator = new BucketBasedFVCreator(edgeWeights);
                clusterer = new KMeans(fvCreator.createFeatureVectors(), new CosineSimilarity());
                break;
        }
        // Find Clusters
        if (Settings.best) {
            clustering = clusterer.findBestClustering(Settings.maxClustersNum);
        } else if (Settings.random) {
            clustering = clusterer.findRandomClustering(Settings.actualNumOfEdgeWeights);
        } else {
            clustering = clusterer.findClustering(Settings.actualNumOfEdgeWeights);
        }
        watch.stop();
        // Compute Aggregate Weights
        StopWatch watch2 = new StopWatch();
        watch2.start();
        double[][] maxWeightsMap = generateAggregateWeights(edgeWeights, clustering);
        watch2.stop();
        // Write Results
        writeKEdgeWeightLists(maxWeightsMap, clustering);
        System.out.println("#Clustering_Time\n" + watch.getElapsedTime() / 1000.0);
        System.out.println("#Generation_Time\n" + watch2.getElapsedTime() / 1000.0);
    }
    
    protected static HashIntObjMap<double[]> readWeightFile() throws IOException {
        final BufferedReader rows = new BufferedReader(new FileReader(Paths.get(Settings.datasetsFolder, Settings.weightFileName).toFile()));
        HashIntObjMap<double[]> weights = HashIntObjMaps.newMutableMap();
        int user = 0;
        String line;

        line = rows.readLine();
        IntStream.range(0, line.split("\\s+").length).forEach(e -> weights.put(e, new double[Settings.numberOfEdgeWeights]));
        while (line != null && user < Settings.numberOfEdgeWeights) {
            final String[] parts = line.split("\\s+");
            final int thisU = user;
            IntStream.range(0, parts.length).forEach(e -> {
                double[] curr = weights.get(e);
                curr[thisU] = Double.parseDouble(parts[e]);
                weights.put(e, curr);
            });
            line = rows.readLine();
            user++;
        }
        rows.close();
        return weights;
    }

    private static double[][] generateAggregateWeights(HashIntObjMap<double[]> edgeWeights, int[] clustering) {
        double[][] maxWeightsMap = new double[Settings.actualNumOfEdgeWeights][edgeWeights.size()];
        for (int edge = 0; edge < edgeWeights.size(); edge++) {
            double[] eWeights = edgeWeights.get(edge);
            final int thisEdge = edge;
            IntStream.range(0, eWeights.length).forEach(j -> maxWeightsMap[clustering[j]][thisEdge] = Math.max(maxWeightsMap[clustering[j]][thisEdge], eWeights[j]));
        }
        return maxWeightsMap;
    }

    private static void writeKEdgeWeightLists(double[][] maxWeightsMap, int[] clustering)
            throws IOException {

        FileWriter KWG = new FileWriter(Paths.get(Settings.outputFolder,
                Settings.inputFileName.substring(0, Settings.inputFileName.length() - 3)
                + "_" + Settings.focus
                + "_" + Settings.actualNumOfEdgeWeights + ".cw").toFile());
        FileWriter clustMap = new FileWriter(Paths.get(Settings.outputFolder,
                Settings.inputFileName.substring(0, Settings.inputFileName.length() - 3)
                + "_" + Settings.focus
                + "_" + Settings.actualNumOfEdgeWeights + ".cl").toFile());
        for (int i = 0; i < clustering.length; i++) {
            // function cluster
            clustMap.write(i + "\t" + clustering[i] + "\n");
        }
        clustMap.close();
        StringBuilder builder;
        for (int i = 0; i < Settings.actualNumOfEdgeWeights; i++) {
            builder = new StringBuilder();
            final int cl = i;
            for (double w : maxWeightsMap[cl]) {
                builder.append(" " + w);
            }
            KWG.write(builder.deleteCharAt(0).toString() + "\n");
        }
        KWG.close();
    }
}
