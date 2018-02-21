package eu.unitn.disi.db.resum.utilities;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;

/**
 *
 * @author bluecopper
 */
public class UserWeightDistribution {

    public static void main(String[] args) throws IOException {
        CommandLineParser.parse(args);
        ArrayList<Double>[] weights = readWeightFile();
        computeWeightStats(weights);
    }

    protected static void computeWeightStats(ArrayList<Double>[] weights) {
        ArrayList<Double> MAXWPerUser = new ArrayList<Double>();
        ArrayList<Double> AVGWPerUser = new ArrayList<Double>();
        int[] weightDist = new int[1001];

        for (int u = 0; u < weights.length; u++) {
            ArrayList<Double> currentWeights = weights[u];
            double MAX = 0;
            double AVG = 0;
            for (double w : currentWeights) {
                MAX = Math.max(MAX, w);
                AVG += w;
                int index;
                if (w > 0.5) {
                    index = 1000;
                } else {
                    index = (int) (w * 2000);
                }
                weightDist[index]++;
            }
            MAXWPerUser.add(MAX);
            AVGWPerUser.add(AVG / currentWeights.size());
        }
        System.out.println("MAX WEIGHT\n" + Collections.max(MAXWPerUser));
        System.out.println("MAX U WEIGHT\n" + Util.toPrint(MAXWPerUser.toArray()));
        System.out.println("AVG U WEIGHT\n" + Util.toPrint(AVGWPerUser.toArray()));
        System.out.println("WEIGHT DIST\n" + Util.toPrint(weightDist));
    }

    protected static ArrayList<Double>[] readWeightFile() throws IOException {
        final BufferedReader rows = new BufferedReader(new FileReader(
                Paths.get(Settings.datasetsFolder, Settings.weightFileName).toFile()));
        ArrayList<Double>[] weights = new ArrayList[Settings.numberOfFunctions];
        for (int u = 0; u < Settings.numberOfFunctions; u++) {
            weights[u] = new ArrayList<Double>();
        }
        String line;
        String[] parts;
        int user = 0;

        line = rows.readLine();
        while (line != null && user < Settings.numberOfFunctions) {
            parts = line.split("\\s+");
            for (int e = 0; e < parts.length; e++) {
                weights[user].add(Double.parseDouble(parts[e]));
            }
            line = rows.readLine();
            user++;
        }
        rows.close();
        return weights;
    }
}
