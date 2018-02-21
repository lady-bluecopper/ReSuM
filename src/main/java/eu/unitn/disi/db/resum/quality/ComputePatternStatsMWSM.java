package eu.unitn.disi.db.resum.quality;

import eu.unitn.disi.db.resum.distance.SetEditDistance;
import eu.unitn.disi.db.resum.utilities.CommandLineParser;
import eu.unitn.disi.db.resum.utilities.MyPair;
import eu.unitn.disi.db.resum.utilities.MyTriplet;
import eu.unitn.disi.db.resum.utilities.Settings;
import eu.unitn.disi.db.resum.utilities.Util;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.TreeSet;

/**
 *
 * @author bluecopper
 */
public class ComputePatternStatsMWSM {

    static int[] TASKS = new int[]{1, 2, 3, 4};
    static MyTriplet<String, Integer, Double>[] DATASETS = new MyTriplet[]{
        new MyTriplet("freebase-travel", 90, 0.05),
        new MyTriplet("freebase-computer", 155, 0.05)};
    static int[] USERS = new int[]{50, 500, 5000, 50000};

    static SetEditDistance editDistance = new SetEditDistance();

    public static void main(String[] args) throws IOException {
        // parse the command line arguments
        CommandLineParser.parse(args);
        for (MyTriplet<String, Integer, Double> dataset : DATASETS) {
            String datasetName = dataset.getA();
            for (int t : TASKS) {
                for (int user : USERS) {
                    // MultiRun-AX
//                    String fileName = Settings.datasetsFolder + "/u" + user + "/approx/Patterns_" + datasetName + ".lg_F" + dataset.getB() + "R" + dataset.getC() + "T" + t + "C" + (user / 10) + "RUN0.p";
                    String fileName = Settings.datasetsFolder + "/u" + user + "/scores/Patterns_" + datasetName + ".lg_F" + dataset.getB() + "R" + dataset.getC() +  "T" + t + "C" + user + "RUN0.p";
                    // SingleRun
                    MyPair<TreeSet<MyPair<String, Double>>[], HashMap<Integer, Double>> patternsWithScores = extractPatternSetsWithScores(fileName, user);
                    if (patternsWithScores != null) {
                        String setting = String.format("%s\t%d\t%f\t%d\t%d", datasetName, dataset.getB(), dataset.getC(), t, user);
                        writeStats(setting, computeStats(patternsWithScores));
                        ArrayList<String>[] topk = findTopKPatterns(patternsWithScores.getA(), 10);
                        for (ArrayList<String> patternList : topk) {
                            writeTopKPatterns(String.format("%s\t%s", setting, Util.toPrint(patternList.toArray())));
                        }
                    }
                    
                }
            }
        }
    }

    
    protected static MyPair<TreeSet<MyPair<String, Double>>[], HashMap<Integer, Double>> extractPatternSetsWithScores(String fileName, int u) throws IOException {
        TreeSet<MyPair<String, Double>>[] patternSets = new TreeSet[u];
        HashMap<Integer, Double> patternSizes = new HashMap<Integer, Double>();
        for (int i = 0; i < patternSets.length; i++) {
            patternSets[i] = new TreeSet<MyPair<String, Double>>(
                (MyPair<String, Double> o1, MyPair<String, Double> o2) -> - Double.compare(o1.getB(), o2.getB()));  
        }
        int counter = 0;
        try (BufferedReader rows = new BufferedReader(new FileReader(Paths.get(fileName).toFile()))) {
            String line = rows.readLine();
            while (line != null) {
                if (line.startsWith("#P")) {
                    // read user id
                    line = rows.readLine().trim();
                    String[] users = line.substring(1, line.length() - 1).split(",");
                    // read pattern
                    String patternString = rows.readLine().trim();
                    String[] pattern = patternString.split(" ");
                    String[] scores = new String[users.length];
                    if (pattern.length == 1) {
                        scores = pattern[0].split(";");
                        patternString = rows.readLine().trim();
                        pattern = patternString.split(" ");
                    } else {
                        for (int i = 0; i < scores.length;  i++) {
                            scores[i] = "-1";
                        }
                    }
                    int el = 2;
                    while (Integer.parseInt(pattern[el]) > Integer.parseInt(pattern[el - 2])) {
                        el += 2;
                    }
                    patternSizes.put(counter, (pattern.length - el) / 3.);
                    for (int user = 0; user < users.length; user ++) {
                        int elID = Integer.parseInt(users[user].trim());
                        patternSets[elID].add(new MyPair(patternString, Double.parseDouble(scores[user])));
                    }
                    counter++;
                }
                line = rows.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return new MyPair(patternSets, patternSizes);
    }
    
    protected static ArrayList<String>[] findTopKPatterns(TreeSet<MyPair<String, Double>>[] patternsWithScores, int k) {
        ArrayList<String>[] topk = new ArrayList[patternsWithScores.length];
        for (int user = 0; user < patternsWithScores.length; user ++) {
            topk[user] = new ArrayList<String>();
            for (int i = 0; i < k; i ++) {
                MyPair<String, Double> thisPair = patternsWithScores[user].pollFirst();
                if (thisPair != null) {
                    topk[user].add(thisPair.getA());
                } else {
                    break;
                }
            }
        }
        return topk;
    }

    protected static String computeStats(MyPair<TreeSet<MyPair<String, Double>>[], HashMap<Integer, Double>> patternsAndSizes) {
        TreeSet<MyPair<String, Double>>[] patterns = patternsAndSizes.getA();
        int[] numPatterns = new int[patterns.length];
        for (int el = 0; el < numPatterns.length; el++) {
            numPatterns[el] = patterns[el].size();
        }
        Arrays.sort(numPatterns);
        HashMap<Integer, Double> sizes = patternsAndSizes.getB();
        // number of patterns
        double AVGPatternsPerUser = 0;
        double MINPatternsPerUser = Double.MAX_VALUE;
        double MAXPatternsPerUser = 0;
        for (int size : numPatterns) {
            AVGPatternsPerUser += size;
            MINPatternsPerUser = Math.min(MINPatternsPerUser, size);
            MAXPatternsPerUser = Math.max(MAXPatternsPerUser, size);
        }
        AVGPatternsPerUser /= patterns.length;
        // median number of patterns
        double MEDIANPatternsPerUser = (numPatterns.length % 2 == 0)
                ? (numPatterns[numPatterns.length / 2] + numPatterns[numPatterns.length / 2 - 1]) / 2
                : numPatterns[numPatterns.length / 2];
        // pattern size
        double AVGPatternSize = 0;
        double MINPatternSize = 0;
        double MAXPatternSize = 0;
        double MEDIANPatternSize = 0;
        if (sizes.size() > 0) {
            MINPatternSize = Double.MAX_VALUE;
            for (double size : sizes.values()) {
                AVGPatternSize += size;
                MINPatternSize = Math.min(MINPatternSize, size);
                MAXPatternSize = Math.max(MAXPatternSize, size);
            }
            AVGPatternSize /= sizes.size();
        
            double[] sortedSizes = new double[sizes.size()];
            int index = 0;
            for (int pID : sizes.keySet()) {
                sortedSizes[index] = sizes.get(pID);
                index += 1;
            }
            Arrays.sort(sortedSizes);
            MEDIANPatternSize = (sortedSizes.length % 2 == 0)
                    ? (sortedSizes[sortedSizes.length / 2] + sortedSizes[sortedSizes.length / 2 - 1]) / 2
                    : sortedSizes[sortedSizes.length / 2];
        }
        return String.format("%f\t%f\t%f\t%f\t%f\t%f\t%f\t%f",
                AVGPatternSize, 
                MEDIANPatternSize,
                MINPatternSize, MAXPatternSize,
                AVGPatternsPerUser, MEDIANPatternsPerUser,
                MINPatternsPerUser, MAXPatternsPerUser
        );

    }
    
    protected static void writeTopKPatterns(String topk) throws IOException {
        FileWriter fw = null;
        try {
            Path path = Paths.get(Settings.outputFolder, "top_pattern_statistics.csv");
            fw = new FileWriter(path.toFile(), true);
            fw.write(String.format("%s\n", topk));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fw != null) {
                fw.close();
            }
        }
    }

    protected static void writeStats(String setting, Object value) throws IOException {
        FileWriter fw = null;
        try {
            Path path = Paths.get(Settings.outputFolder, "pattern_statistics.csv");
            fw = new FileWriter(path.toFile(), true);
            fw.write(String.format("%s\t%s\n",
                    setting, value.toString()));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fw != null) {
                fw.close();
            }
        }
    }
}
