package eu.unitn.disi.db.resum.quality;

import eu.unitn.disi.db.resum.distance.SetEditDistance;
import eu.unitn.disi.db.resum.utilities.CommandLineParser;
import eu.unitn.disi.db.resum.utilities.MyTriplet;
import eu.unitn.disi.db.resum.utilities.Settings;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

/**
 *
 * @author bluecopper
 */
public class ComputePatternStatsWSM {
    
    static int[] TASKS = new int[]{1, 2, 3};
    static MyTriplet<String, int[], double[]>[] DATASETS = new MyTriplet[]{
//        new MyTriplet("freebase-travel", 
//                new int[]{90, 110, 140, 150, 160, 190, 220}, 
//                new double[]{0, 0.0001, 0.001, 0.01, 0.025, 0.05, 0.075, 0.1, 0.25, 0.5}),
//        new MyTriplet("freebase-computer", 
//                new int[]{150, 160, 170, 185, 190, 200, 210}, 
//                new double[]{0, 0.0001, 0.001, 0.01, 0.025, 0.05, 0.075, 0.1, 0.25, 0.5}),
        new MyTriplet("electronics-2-pruned", 
                new int[]{130, 133, 140, 180, 190}, 
                new double[]{0, 0.0001, 0.00025, 0.0005, 0.001, 0.01})};
//    static int[] USERS = new int[]{5519, 11248, 18093, 19970, 21505, 31130, 37525, 42763, 46918, 48884};
    static int[] USERS = new int[]{0, 4, 9, 52, 59, 106, 130, 136, 143, 194, 237, 250, 255, 260, 280};
    
    static SetEditDistance editDistance = new SetEditDistance();

    public static void main(String[] args) throws IOException {
        // parse the command line arguments
        CommandLineParser.parse(args);
        HashMap<String, Integer> freqs = new HashMap(){{put("freebase-computer" , 155); put("freebase-travel", 90); put("electronics-2-pruned", 130);}};
//        double rel = 0.05;
//        String rel = "1.0E-4";
        String rels[] = new String[]{"0.0", "1.0E-4", "2.5E-4", "5.0E-4", "0.001", "0.01"};
        int uID;
        for (MyTriplet<String, int[], double[]> dataset : DATASETS) {
            String datasetName = dataset.getA();
//            int[] freqs = dataset.getB();
//            double[] rels = dataset.getC();
//            for (int freq : freqs) {
            for (String rel : rels) {
                // read frequent patterns
//                HashSet<String> frequentPatterns = extractPatternSets(Settings.datasetsFolder + "/Patterns_" + datasetName + ".lg_F" + freq + "R-1.0T1C1RUN0.p");
                HashSet<String> frequentPatterns = extractPatternSets(Settings.datasetsFolder + "/Patterns_" + datasetName + ".lg_F" + freqs.get(datasetName) + "R-1.0T1C1RUN0.p");
                for (int t : TASKS) {
                    HashSet<String>[] relevantPatterns = new HashSet[USERS.length];
                    for (uID = 0; uID < USERS.length; uID ++) {
//                        HashMap<String, Double> patternsWithScores = extractPatternSetsWithScores(Settings.datasetsFolder + "/Patterns_" + datasetName + ".lg_F" + freq + "R" + rel + "T" + t + "C1RUN" + USERS[uID] + ".p");
                        HashMap<String, Double> patternsWithScores = extractPatternSetsWithScores(Settings.datasetsFolder + "/Patterns_" + datasetName + ".lg_F" + freqs.get(datasetName) + "R" + rel + "T" + t + "C1RUN" + USERS[uID] + ".p");
                        relevantPatterns[uID] = (patternsWithScores != null) ? new HashSet<String>(patternsWithScores.keySet()) : null;
                    }
                    if (relevantPatterns[0] != null) {
//                        String setting0 = String.format("%s\t%d\t%s\t%d", datasetName, freq, rel, t);
                        String setting0 = String.format("%s\t%d\t%s\t%d", datasetName, freqs.get(datasetName), rel, t);
                        writeStats(String.format("%s\t%s\t%s", 
                                setting0,
                                editDistance.computeAVGDistancePerUsersWithPrints(frequentPatterns, relevantPatterns).toString(),
                                computeAVGPatternSizeandCount(relevantPatterns)));
                    }
                }
            }
        }
    }

    protected static HashMap<String, Double> extractPatternSetsWithScores(String fileName) throws IOException {
        HashMap<String, Double> patternSets = new HashMap<String, Double>();
        try (BufferedReader rows = new BufferedReader(new FileReader(Paths.get(fileName).toFile()))) {
            String line = rows.readLine();
            while (line != null) {
                if (line.startsWith("#P")) {
                    // read user id
                    line = rows.readLine();
                    // read pattern score
                    Double score = Double.parseDouble(rows.readLine().trim());
                    // read pattern
                    line = rows.readLine();
                    patternSets.put(line.trim(), score);
                }
                line = rows.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return patternSets;
    }
    
    protected static HashSet<String> extractPatternSets(String fileName) throws IOException {
        HashSet<String> patternSets = new HashSet<String>();
        try (BufferedReader rows = new BufferedReader(new FileReader(Paths.get(fileName).toFile()))) {
            String line = rows.readLine();
            while (line != null) {
                if (line.startsWith("#P")) {
                    // read user id
                    line = rows.readLine();
                    // read pattern score
                    rows.readLine().trim();
                    // read pattern
                    line = rows.readLine();
                    patternSets.add(line.trim());
                }
                line = rows.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return patternSets;
    }
    
    protected static void writeStats(String stats) throws IOException {
        FileWriter fw = null;
        try {
            String fName;
            if (Settings.outputFileName == null) {
                fName = "quality_statistics.csv";
            } else {
                fName = Settings.outputFileName;
            }
            Path path = Paths.get(Settings.outputFolder, fName);
            fw = new FileWriter(path.toFile(), true);
            fw.write(String.format("%s\n", stats));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fw != null) {
                fw.close();
            }
        }
    }
   
    
    protected static String computeAVGPatternSizeandCount(HashSet<String>[] patternsPerUser) {
        double AVGPatternCount = 0;
        double MINPatternCount = Double.MAX_VALUE;
        double MAXPatternCount = 0;
        ArrayList<Integer> PatternCounts = new ArrayList<Integer>();
        ArrayList<Double> AVGPatternSizes = new ArrayList<Double>();
        ArrayList<Double> MINPatternSizes = new ArrayList<Double>();
        ArrayList<Double> MAXPatternSizes = new ArrayList<Double>();
        
        for (HashSet<String> patternList : patternsPerUser) {
            if (patternList != null) {
            int numPatterns = patternList.size();
            AVGPatternCount += numPatterns;
            MINPatternCount = Math.min(MINPatternCount, numPatterns);
            MAXPatternCount = Math.max(MAXPatternCount, numPatterns);
            PatternCounts.add(numPatterns);
            double AVGPatternSize = 0;
            double MinPatternSize = Double.MAX_VALUE;
            double MaxPatternSize = 0;
            
            if (numPatterns > 0) {
                for (String pattern : patternList) {
                    String[] patternEl = pattern.split(" ");
                    int el = 2;
                    while (Integer.parseInt(patternEl[el]) > Integer.parseInt(patternEl[el - 2])) {
                        el += 2;
                    }
                    double currentSize = (patternEl.length - el) / 3.;
                    AVGPatternSize += currentSize;
                    MinPatternSize = Math.min(MinPatternSize, currentSize);
                    MaxPatternSize = Math.max(MaxPatternSize, currentSize);
                }
                AVGPatternSizes.add(AVGPatternSize / numPatterns);
                MINPatternSizes.add(MinPatternSize);
                MAXPatternSizes.add(MaxPatternSize);
            }
            }
        }
        // Stats on number of patterns
        AVGPatternCount /= patternsPerUser.length;
        if (Double.compare(MINPatternCount, Double.MAX_VALUE) == 0) {
            MINPatternCount = 0;
        }
        Collections.sort(PatternCounts);
        double MEDIANPatternCount = (PatternCounts.size() % 2 == 0)
                ? (PatternCounts.get(PatternCounts.size() / 2) + PatternCounts.get(PatternCounts.size() / 2 - 1)) / 2
                : PatternCounts.get(PatternCounts.size() / 2);
        // Stats on pattern sizes
        double AVGAVGPatternSize = 0;
        double MINMINPatternSize = 0;
        double MAXMAXPatternSize = 0;
        double MEDIANPatternSize = 0;
        if (AVGPatternSizes.size() > 0) {
            for (double size : AVGPatternSizes) {
                AVGAVGPatternSize += size;
            }
            AVGAVGPatternSize /= AVGPatternSizes.size();
            MINMINPatternSize = Collections.min(MINPatternSizes);
            MAXMAXPatternSize = Collections.max(MAXPatternSizes);
            Collections.sort(AVGPatternSizes);
            MEDIANPatternSize = (AVGPatternSizes.size() % 2 == 0)
                    ? (AVGPatternSizes.get(AVGPatternSizes.size() / 2) + AVGPatternSizes.get(AVGPatternSizes.size() / 2 - 1)) / 2
                    : AVGPatternSizes.get(AVGPatternSizes.size() / 2);
        }
        return String.format("%f\t%f\t%f\t%f\t%f\t%f\t%f\t%f",
                AVGPatternCount, 
                MINPatternCount,
                MAXPatternCount, 
                MEDIANPatternCount,
                AVGAVGPatternSize,
                MINMINPatternSize,
                MAXMAXPatternSize, 
                MEDIANPatternSize);

    }
    
}
