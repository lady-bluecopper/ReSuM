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
package eu.unitn.disi.db.resum.utilities;

public class CommandLineParser {

    public static void parse(String[] args) {
        for (String arg : args) {
            String[] parts = arg.split("=");
            String key = parts[0];
            String value = parts[1];
            //get frequency
            if (key.equalsIgnoreCase("freq")) {
                Settings.frequency = Integer.parseInt(value);
            }
            //get relevance
            if (key.equalsIgnoreCase("rel")) {
                Settings.relevance = Double.parseDouble(value);
            }
            //get task
            if (key.equalsIgnoreCase("task")) {
                Settings.task = Integer.parseInt(value);
            }
            //get dataset filename
            if (key.equalsIgnoreCase("filename")) {
                Settings.inputFileName = value;
            }
            //get weight filename
            if (key.equalsIgnoreCase("weightFile")) {
                Settings.weightFileName = value;
            }
            //get pattern sets filename
            if (key.equalsIgnoreCase("patternFile")) {
                Settings.patternFileName = value;
            }
            //get output filename
            if (key.equalsIgnoreCase("outputFile")) {
                Settings.outputFileName = value;
            }
            //get exact pattern sets filename
            if (key.equalsIgnoreCase("exactFile")) {
                Settings.exactFileName = value;
            }
            //get approx pattern sets filename
            if (key.equalsIgnoreCase("approxFile")) {
                Settings.approxFileName = value;
            }
            //get clustering filename
            if (key.equalsIgnoreCase("clusteringFile")) {
                Settings.clusteringFileName = value;
            }
            //assign datasets folder
            if (key.equalsIgnoreCase("datasetFolder")) {
                Settings.datasetsFolder = value;
            }
            //assign output folder
            if (key.equalsIgnoreCase("outputFolder")) {
                Settings.outputFolder = value;
            }
            //automorphism
            if (key.equalsIgnoreCase("automorphism")) {
                Settings.isAutomorphismOn = (value.compareTo("true") == 0);
            }
            //caching substructures
            if (key.equalsIgnoreCase("caching")) {
                Settings.CACHING = (value.compareTo("true") == 0);
            }
            //number of buckets for clustering
            if (key.equalsIgnoreCase("buckets")) {
                Settings.bucketsNum = Integer.parseInt(value);
            }
            //number of weighting functions
            if (key.equalsIgnoreCase("functions")) {
                Settings.numberOfFunctions = Integer.parseInt(value);
            }
            //max iterations for k-means 
            if (key.equalsIgnoreCase("iterations")) {
                Settings.numberOfIterations = Integer.parseInt(value);
            }
            //smart selection of the initial centroids for k-means
            if (key.equalsIgnoreCase("smart")) {
                Settings.smart = (value.compareTo("true") == 0);
            }
            //relevance vectors are the weighting functions
            if (key.equalsIgnoreCase("clusteringType")) {
                Settings.clusteringType = value;
            }
            //structure size
            if (key.equalsIgnoreCase("structureSize")) {
                Settings.structureSize = Integer.parseInt(value);
            }
            //number of runs of k-means for each k
            if (key.equalsIgnoreCase("kMeanRuns")) {
                Settings.numberOfRuns = Integer.parseInt(value);
            }
            //max value of K for k-means
            if (key.equalsIgnoreCase("maxClusters")) {
                Settings.maxClustersNum = Integer.parseInt(value);
            }
            //find best clustering
            if (key.equalsIgnoreCase("bestC")) {
                Settings.best = (value.compareTo("true") == 0);
            }
            //random clustering
            if (key.equalsIgnoreCase("random")) {
                Settings.random = (value.compareTo("true") == 0);
            }
            //focus value for weight generation
            if (key.equalsIgnoreCase("focus")) {
                Settings.focus = Integer.parseInt(value);
            }
            //focus value for weight generation
            if (key.equalsIgnoreCase("edgeFocus")) {
                Settings.edge_focus = Integer.parseInt(value);
            }
            // Multiple runs
            if (key.equalsIgnoreCase("multipleRuns")) {
                Settings.multipleRuns = Integer.parseInt(value);
            }
            // Seed for Random
            if (key.equalsIgnoreCase("seed")) {
                Settings.seed = Integer.parseInt(value);
            }
            // Ignore labels on nodes
            if (key.equalsIgnoreCase("ignoreLabels")) {
                Settings.NOLABEL = value.equalsIgnoreCase("true");
            }

        }
        if (Settings.maxClustersNum == 0) {
            Settings.maxClustersNum = Settings.numberOfFunctions / 10;
        }
    }
}