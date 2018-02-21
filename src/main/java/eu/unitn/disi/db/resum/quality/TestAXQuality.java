package eu.unitn.disi.db.resum.quality;

import eu.unitn.disi.db.resum.distance.SetEditDistance;
import eu.unitn.disi.db.resum.utilities.CommandLineParser;
import eu.unitn.disi.db.resum.utilities.MyTriplet;
import eu.unitn.disi.db.resum.utilities.Settings;
import eu.unitn.disi.db.resum.utilities.Util;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;

/**
 *
 * @author bluecopper
 */
public class TestAXQuality {

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
            for (int u : USERS) {
                for (int t : TASKS) {
                    System.out.println(u + " " + t);
                    String clusteringFile = Settings.clusteringFileName + "/u" + u + "/clustering/" + dataset.getA() + "_50_" + (u / 10) + ".cl";
//                    String clusteringFile = Settings.clusteringFileName + "/u" + u + "/clustering/T" + t + "/" + dataset.getA() + "_50_" + (u / 10) + ".cl";
                    HashMap<Integer, Integer> clustering = extractClustering(clusteringFile);
                    if (clustering != null) {
                        String setting = String.format("%s\t%d\t%d", dataset.toString(), t, u);
                        String approxFile = Settings.approxFileName + "/u" + u + "/approx/Patterns_" + dataset.getA() + ".lg_F" + dataset.getB() + "R" + "1.0E-4" + "T" + t + "C" + (u / 10) + "RUN0.p";
                        String realFile = Settings.exactFileName + "/u" + u + "/scores/Patterns_" + dataset.getA() + ".lg_F" + dataset.getB() + "R" + "1.0E-4" + "T" + t + "C" + u + "RUN0.p";
                        HashSet<String>[] exactPatternSets = extractPatternSets(realFile, true, u);
                        HashSet<String>[] clusterPatternSets = extractPatternSets(approxFile, false, u);
                        if (exactPatternSets != null && clusterPatternSets != null) {
                            writeStats(String.format("%s\t%f\t%f",
                                    setting,
                                    computePrecision(exactPatternSets, clusterPatternSets, clustering, u),
                                    computePatternDistance(exactPatternSets, clusterPatternSets, clustering, u)));
                        }
                    }
                }
            }
        }
    }

    public static HashSet<String>[] extractPatternSets(String fileName, boolean exact, int u) throws IOException {
        HashSet<String>[] patternSets = new HashSet[exact ? u : (u / 10)];
        for (int i = 0; i < patternSets.length; i++) {
            patternSets[i] = new HashSet<String>();
        }
        try (BufferedReader rows = new BufferedReader(new FileReader(Paths.get(fileName).toFile()))) {

            String line = rows.readLine();
            while (line != null) {
                if (line.startsWith("#P")) {
                    line = rows.readLine().trim();
                    String[] list = line.substring(1, line.length() - 1).split(",");
                    String pattern = rows.readLine().trim();
                    if (pattern.split(" ").length == 1) {
                        pattern = rows.readLine().trim();
                    }
                    for (String el : list) {
                        int elID = Integer.parseInt(el.trim());
                        patternSets[elID].add(pattern);
                    }
                }
                line = rows.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return patternSets;
    }

    public static HashMap<Integer, Integer> extractClustering(String fileName) {
        HashMap<Integer, Integer> clustering = new HashMap<Integer, Integer>();
        try (BufferedReader rows = new BufferedReader(new FileReader(Paths.get(fileName).toFile()))) {
            String line = rows.readLine();

            while (line != null) {
                String[] parts = line.trim().split("\t");
                clustering.put(Integer.parseInt(parts[0].trim()), Integer.parseInt(parts[1].trim()));
                line = rows.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return clustering;
    }

    public static double computePrecision(HashSet<String>[] exactPatternSets, HashSet<String>[] clusterPatternSets, HashMap<Integer, Integer> clustering, int u) throws IOException {
        HashSet<String>[] approxPatternSets = new HashSet[u];
        int i;
        for (i = 0; i < u; i++) {
            approxPatternSets[i] = clusterPatternSets[clustering.get(i)];
        }
        double cumulativePrecision = 0;
        int realNumUsers = 0;
        for (i = 0; i < u; i++) {
            if (exactPatternSets[i].size() > 0) {
                realNumUsers++;
                if (approxPatternSets[i].size() > 0) {
                    cumulativePrecision += Util.intersectionSize(exactPatternSets[i], approxPatternSets[i]) / (double) approxPatternSets[i].size();
                }
            }
        }
        return cumulativePrecision / realNumUsers;
    }

    public static double computePatternDistance(HashSet<String>[] exactPatternSets, HashSet<String>[] clusterPatternSets, HashMap<Integer, Integer> clustering, int u) throws IOException {
        HashSet<String>[] approxPatternSets = new HashSet[u];
        for (int i = 0; i < u; i++) {
            approxPatternSets[i] = clusterPatternSets[clustering.get(i)];
        }
        return new SetEditDistance().computeAVGDistancePerUsers(approxPatternSets, exactPatternSets);
    }

    protected static void writeStats(String setting) throws IOException {
        FileWriter fw = null;
        try {
            String fName;
            if (Settings.outputFileName == null) {
                fName = "ax_quality.csv";
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
