/**
 * Copyright 2014 Mohammed Elseidy, Ehab Abdelhamid
 *
 * This file is part of Grami.
 *
 * Grami is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * Grami is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Grami.  If not, see <http://www.gnu.org/licenses/>.
 */
package sa.edu.kaust.grami.dijkstra;

import eu.unitn.disi.db.resum.search.Algorithm;
import eu.unitn.disi.db.resum.search.RecursiveStrategy;
import sa.edu.kaust.grami.dataStructures.DFScodeSerializer;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import eu.unitn.disi.db.resum.utilities.CommandLineParser;
import eu.unitn.disi.db.resum.utilities.Settings;
import eu.unitn.disi.db.resum.utilities.StopWatch;
import eu.unitn.disi.db.resum.utilities.Util;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeSet;
import sa.edu.kaust.grami.dataStructures.HPListGraph;

public class Main {

    public static StopWatch watch;

    public static void main(String[] args) throws InstantiationException,
            IllegalAccessException,
            ClassNotFoundException,
            ParseException,
            IOException,
            Exception {

        // parse the command line arguments
        CommandLineParser.parse(args);
        // load weights
        HashMap<Integer, ArrayList<double[]>> maxEdgeWeights = loadWeights();
        for (Entry<Integer, ArrayList<double[]>> e : maxEdgeWeights.entrySet()) {
            run(e.getKey(), e.getValue());
        }
    }

    public final static <NodeType, EdgeType> void run(int u, ArrayList<double[]> weights)
            throws InstantiationException, IllegalAccessException,
            ClassNotFoundException, ParseException, IOException {

        ArrayList<HPListGraph<NodeType, EdgeType>> result = null;
        ArrayList<BitSet> retMask = null;
        ArrayList<double[]> scores = null;
        watch = new StopWatch();

        try {
            if (Settings.inputFileName == null) {
                System.out.println("You have to specify a filename");
                System.exit(1);
            } else {
                watch.start();
                Algorithm<NodeType, EdgeType> algo = new Algorithm<NodeType, EdgeType>(weights);
                algo.initialize();
                System.out.print("Starting Recursive Strategy...\n");
                RecursiveStrategy<NodeType, EdgeType> rs = new RecursiveStrategy<NodeType, EdgeType>();
                result = (ArrayList<HPListGraph<NodeType, EdgeType>>) rs.search(algo);
                retMask = rs.getRetMask();
                scores = rs.getScores();
            }
            System.out.printf("Finding patterns in %s, "
                    + "freq=%d, "
                    + "rel=%f, "
                    + "task=%d, "
                    + "num_functions=%d, "
                    + "num_edge_weights_considered=%d, "
                    + "user=%d\n",
                    Settings.inputFileName,
                    Settings.frequency,
                    Settings.relevance,
                    Settings.task,
                    Settings.numberOfFunctions,
                    Settings.structureSize,
                    u);
            watch.stop();
            System.out.printf("Found %d patterns, in %dms\n", result.size(), watch.getElapsedTime());
            // Write Patterns 
            writeResults(result, retMask, scores, u);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static <NodeType, EdgeType> void writeResults(
            ArrayList<HPListGraph<NodeType, EdgeType>> result,
            ArrayList<BitSet> retMask,
            ArrayList<double[]> scores,
            int u) throws IOException {

        FileWriter fw = null;
        try {
            String fName;
            if (Settings.outputFileName == null) {
                fName = "statistics.csv";
            } else {
                fName = Settings.outputFileName;
            }
            Path path = Paths.get(Settings.outputFolder, fName);
            fw = new FileWriter(path.toFile(), true);
            if (Settings.numberOfFunctions > Settings.structureSize) {
                fw.write(String.format("%s\t%s\t%f\t%d\t%d\t%f\t%d\t%d\t%d\t%d\t%s\t%d\n",
                        Settings.inputFileName,
                        new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date()),
                        watch.getElapsedTime() / 1000.0D,
                        result.size(),
                        Settings.frequency,
                        Settings.relevance,
                        Settings.task,
                        Settings.numberOfFunctions,
                        Settings.structureSize,
                        Settings.clusteringType.equals("bucket") ? Settings.bucketsNum : 0,
                        Settings.clusteringType,
                        Settings.focus));
            } else {
                fw.write(String.format("%s\t%s\t%f\t%d\t%d\t%f\t%d\t%d\t%d\t%d\t%s\t%d\n",
                        Settings.inputFileName,
                        new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date()),
                        watch.getElapsedTime() / 1000.0D,
                        result.size(),
                        Settings.frequency,
                        Settings.relevance,
                        Settings.task,
                        Settings.numberOfFunctions,
                        Settings.structureSize,
                        0, "",
                        Settings.focus));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fw != null) {
                fw.close();
            }
        }
        try {
            String fName = "Patterns_" + Settings.inputFileName + "_F" + Settings.frequency + "R"
                    + Settings.relevance + "T" + Settings.task + "C" + Settings.structureSize + "RUN" + u + ".p";
            Path path = Paths.get(Settings.outputFolder, new String[]{fName});
            FileWriter fwP = new FileWriter(path.toFile());
            for (int i = 0; i < result.size(); i++) {
                fwP.write("#P" + i + "\n");
                fwP.write((retMask.get(i)).toString() + "\n");
                fwP.write(Util.toPrint(scores.get(i)) + "\n");
                fwP.write(DFScodeSerializer.compactSerialization(result.get(i)) + "\n");
            }
            fwP.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static <NodeType, EdgeType> void writeResults(
            ArrayList<HPListGraph<NodeType, EdgeType>> result,
            ArrayList<BitSet> retMask,
            int u) throws IOException {

        FileWriter fw = null;
        try {
            String fName;
            if (Settings.outputFileName == null) {
                fName = "statistics.csv";
            } else {
                fName = Settings.outputFileName;
            }
            Path path = Paths.get(Settings.outputFolder, fName);
            fw = new FileWriter(path.toFile(), true);
            if (Settings.numberOfFunctions > Settings.structureSize) {
                fw.write(String.format("%s\t%s\t%f\t%d\t%d\t%f\t%d\t%d\t%d\t%d\t%s\t%d\n",
                        Settings.inputFileName,
                        new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date()),
                        watch.getElapsedTime() / 1000.0D,
                        result.size(),
                        Settings.frequency,
                        Settings.relevance,
                        Settings.task,
                        Settings.numberOfFunctions,
                        Settings.structureSize,
                        Settings.clusteringType.equals("bucket") ? Settings.bucketsNum : 0,
                        Settings.clusteringType,
                        Settings.focus));
            } else {
                fw.write(String.format("%s\t%s\t%f\t%d\t%d\t%f\t%d\t%d\t%d\t%d\t%s\t%d\n",
                        Settings.inputFileName,
                        new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date()),
                        watch.getElapsedTime() / 1000.0D,
                        result.size(),
                        Settings.frequency,
                        Settings.relevance,
                        Settings.task,
                        Settings.numberOfFunctions,
                        Settings.structureSize,
                        0, "",
                        Settings.focus));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fw != null) {
                fw.close();
            }
        }
        try {
            String fName = "Patterns_" + Settings.inputFileName + "_F" + Settings.frequency + "R"
                    + Settings.relevance + "T" + Settings.task + "C" + Settings.structureSize + "RUN" + u + ".p";
            Path path = Paths.get(Settings.outputFolder, new String[]{fName});
            FileWriter fwP = new FileWriter(path.toFile());
            for (int i = 0; i < result.size(); i++) {
                fwP.write("#P" + i + "\n");
                fwP.write((retMask.get(i)).toString() + "\n");
                fwP.write(DFScodeSerializer.compactSerialization(result.get(i)) + "\n");
            }
            fwP.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static HashMap<Integer, ArrayList<double[]>> loadWeights() throws Exception {
        HashMap<Integer, ArrayList<double[]>> maxEdgeWeights = new HashMap<Integer, ArrayList<double[]>>();
        File weightFile = Paths.get(Settings.datasetsFolder, Settings.weightFileName).toFile();

        if (!weightFile.exists() || !weightFile.canRead()) {
            maxEdgeWeights.put(0, null);
            return maxEdgeWeights;
        }
        try (BufferedReader rows = new BufferedReader(new FileReader(weightFile))) {
            ArrayList<double[]> currWeightList;
            int counter = 0;
            String line = rows.readLine();

            if (Settings.multipleRuns > 1) {
                Random generator = new Random(Settings.seed);
                TreeSet<Integer> users = new TreeSet<Integer>();
                while (users.size() < Settings.multipleRuns) {
                    users.add(generator.nextInt(50000));
                }
                while (users.size() > 0) {
                    int currUser = users.pollFirst();
                    while (counter != currUser) {
                        line = rows.readLine();
                        counter++;
                    }
                    String[] currWeights = line.split("\\s+");
                    currWeightList = new ArrayList();
                    for (int e = 0; e < currWeights.length; e++) {
                        currWeightList.add(e, new double[]{Double.parseDouble(currWeights[e])});
                    }
                    maxEdgeWeights.put(currUser, currWeightList);
                }

            } else {
                currWeightList = new ArrayList();
                if (line != null) {
                    int numEdges = line.split("\\s+").length;
                    for (int e = 0; e < numEdges; e++) {
                        currWeightList.add(e, new double[Settings.structureSize]);
                    }
                }
                while (line != null && counter < Settings.structureSize) {
                    final String[] parts = line.split("\\s+");
                    for (int e = 0; e < parts.length; e++) {
                        currWeightList.get(e)[counter] = Double.parseDouble(parts[e]);
                    }
                    line = rows.readLine();
                    counter++;
                }
                maxEdgeWeights.put(0, currWeightList);
            }
            rows.close();
            return maxEdgeWeights;
        } catch (IOException exc) {
            System.err.println("Weight File contains error " + exc.getMessage());
            System.exit(1);
            exc.printStackTrace();
        }
        return null;
    }
}
