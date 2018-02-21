package eu.unitn.disi.db.resum.utilities;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Random;

/**
 *
 * @author bluecopper
 */
public class FocusWeightGenerator {

    private static ArrayList<MyTriplet<Integer, Integer, Double>> edges;
    private static HashMap<Double, Integer> numEdgesPerLabel;
    private static Double[] labelList;
    private static final Random rand = new Random();

    public static void main(final String[] args) throws IOException {
        CommandLineParser.parse(args);
        // read graph
        readFile(Settings.inputFileName);
        // METHOD 1: a line per edge
//        HashMap<Double, ArrayList<Integer>> labelsSelected = selectUsersPerLabel();
//        generateKWeightsPerEdge(labelsSelected);
        // METHOD 2: a line per user
        HashMap<Integer, HashSet<Double>> labelsPerUSer = selectLabelsPerUser();
        generateKEdgeWeightLists(labelsPerUSer);
    }

    private static void readFile(String fileName) throws FileNotFoundException, IOException {

        final BufferedReader rows = new BufferedReader(new FileReader(
                Paths.get(Settings.datasetsFolder, fileName).toFile()));

        edges = new ArrayList<MyTriplet<Integer, Integer, Double>>();
        HashSet<Double> labels = new HashSet<Double>();
        numEdgesPerLabel = new HashMap<Double, Integer>();
        String line = rows.readLine();

        while (line != null) {
            if (line.startsWith("e")) {
                final String[] parts = line.split("\\s+");
                final int index1 = Integer.parseInt(parts[1]);
                final int index2 = Integer.parseInt(parts[2]);
                final double label = Double.parseDouble(parts[3]);

                edges.add(new MyTriplet<Integer, Integer, Double>(index1, index2, label));
                labels.add(label);
                numEdgesPerLabel.put(label, numEdgesPerLabel.getOrDefault(label, 0) + 1);
            }
            line = rows.readLine();
        }
        rows.close();
        labelList = labels.toArray(new Double[labels.size()]);
    }

    private static HashMap<Double, ArrayList<Integer>> selectUsersPerLabel() {
        int labelsToSelect = Math.max(Settings.focus * labelList.length / 100, 1);
        HashMap<Double, ArrayList<Integer>> labelsSelected = new HashMap<Double, ArrayList<Integer>>();
        HashSet<Double> selected;
        for (int u = 0; u < Settings.numberOfFunctions; u++) {
            selected = new HashSet<Double>();
            while (selected.size() != labelsToSelect) {
                selected.add(labelList[rand.nextInt(labelList.length)]);
            }
            for (double l : selected) {
                labelsSelected.putIfAbsent(l, new ArrayList<Integer>());
                labelsSelected.get(l).add(u);
            }
        }
        return labelsSelected;
    }

    private static HashMap<Integer, HashSet<Double>> selectLabelsPerUser() {
        int labelsToSelect = Math.max(Settings.focus * labelList.length / 100, 1);
        HashMap<Integer, HashSet<Double>> labelsSelected = new HashMap<Integer, HashSet<Double>>();
        HashSet<Double> selected;
        for (int u = 0; u < Settings.numberOfFunctions; u++) {
            selected = new HashSet<Double>();
            while (selected.size() != labelsToSelect) {
                selected.add(labelList[rand.nextInt(labelList.length)]);
            }
            labelsSelected.put(u, selected);
        }
        return labelsSelected;
    }

    private static void generateKWeightsPerEdge(HashMap<Double, ArrayList<Integer>> labelsSelected)
            throws FileNotFoundException, IOException {

        int flush = 0;
        BufferedWriter WGraphWriter = new BufferedWriter(new FileWriter(Paths.get(
                Settings.outputFolder,
                Settings.inputFileName.substring(0, Settings.inputFileName.length() - 3)
                + "_" + Settings.focus + ".w").toFile()));
        // initialize structures for edge weight generation
        HashMap<Double, int[]> edgesToSelectPerLabel = new HashMap<Double, int[]>();
        for (Entry e : labelsSelected.entrySet()) {
            double label = (Double) e.getKey();
            ArrayList<Integer> usersPerLabel = (ArrayList<Integer>) e.getValue();
            // determine how many edges should be weighted for each user
            int edgesToWeight = Settings.edge_focus * numEdgesPerLabel.get(label) / 100;
            int[] edgesToSelect = new int[Settings.numberOfFunctions];
            for (int u : usersPerLabel) {
                edgesToSelect[u] = edgesToWeight;
            }
            edgesToSelectPerLabel.put(label, edgesToSelect);
        }
        // generate weights for each user for each edge
        for (MyTriplet<Integer, Integer, Double> edge : edges) {
            Double label = edge.getC();
            String[] randomWeights = generateGaussianWeights(edgesToSelectPerLabel.get(label));
            WGraphWriter.write(String.join(" ", randomWeights) + "\n");
            if (flush++ % 200 == 0) {
                WGraphWriter.flush();
            }
        }
        WGraphWriter.flush();
        WGraphWriter.close();
    }

    private static void generateKEdgeWeightLists(HashMap<Integer, HashSet<Double>> labelsSelected)
            throws FileNotFoundException, IOException {

        int flush = 0;
        BufferedWriter WGraphWriter = new BufferedWriter(new FileWriter(Paths.get(
                Settings.outputFolder,
                Settings.inputFileName.substring(0, Settings.inputFileName.length() - 3)
                + "_" + Settings.focus + ".w").toFile()));
        // initialize structures for edge weight generation
        for (int u = 0; u < Settings.numberOfFunctions; u++) {
            HashMap<Double, Integer> edgesToSelect = new HashMap<Double, Integer>();
            for (double l : labelList) {
                if (labelsSelected.get(u).contains(l)) {
                    edgesToSelect.put(l, Settings.edge_focus * numEdgesPerLabel.get(l) / 100);
                } else {
                    edgesToSelect.put(l, 0);
                }
            }
            String[] gaussianWeights = new String[edges.size()];
            int i = 0;
            for (MyTriplet<Integer, Integer, Double> edge : edges) {
                gaussianWeights[i] = generateUserGaussianWeight(edge.getC(), edgesToSelect);
                i ++;
            }
            WGraphWriter.write(String.join(" ", gaussianWeights) + "\n");
            if (flush++ % 200 == 0) {
                WGraphWriter.flush();
            }
        }
        WGraphWriter.flush();
        WGraphWriter.close();
    }

    private static String[] generateGaussianWeights(int[] edgesToSelect) {
        String[] thisWeights = new String[Settings.numberOfFunctions];
        for (int u = 0; u < Settings.numberOfFunctions; u++) {
            if (edgesToSelect[u] > 0) {
                double gaussianWeight = Math.max(0, Math.min(1, rand.nextGaussian() * 1 / 4 + 1 / 2));
                thisWeights[u] = String.valueOf(Util.truncate(gaussianWeight, 3));
                edgesToSelect[u]--;
            } else {
                thisWeights[u] = String.valueOf(0);
            }
        }
        return thisWeights;
    }

    private static String generateUserGaussianWeight(double l, HashMap<Double, Integer> edgesToSelect) {
        if (edgesToSelect.get(l) > 0) {
            double gaussianWeight = Math.max(0, Math.min(1, rand.nextGaussian() * 1 / 4 + 1 / 2));
            edgesToSelect.put(l, edgesToSelect.get(l) - 1);
            return String.valueOf(Util.truncate(gaussianWeight, 3));
        } else {
            return String.valueOf(0);
        }
    }
}
