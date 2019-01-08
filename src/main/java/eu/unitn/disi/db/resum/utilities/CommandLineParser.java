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
            else if (key.equalsIgnoreCase("rel")) {
                Settings.relevance = Double.parseDouble(value);
            }
            //get task
            else if (key.equalsIgnoreCase("score")) {
                Settings.score = Integer.parseInt(value);
            }
            //get dataset filename
            else if (key.equalsIgnoreCase("inputFileName")) {
                Settings.inputFileName = value;
            }
            //get weight filename
            else if (key.equalsIgnoreCase("weightFileName")) {
                Settings.weightFileName = value;
            }
            //get pattern sets filename
            else if (key.equalsIgnoreCase("patternFileName")) {
                Settings.patternFileName = value;
            }
            //get output filename
            else if (key.equalsIgnoreCase("outputFileName")) {
                Settings.outputFileName = value;
            }
            //assign datasets folder
            else if (key.equalsIgnoreCase("datasetsFolder")) {
                Settings.datasetsFolder = value;
            }
            //assign output folder
            else if (key.equalsIgnoreCase("outputFolder")) {
                Settings.outputFolder = value;
            }
            else if (key.compareTo("weightFileSize") == 0) {
                Settings.weightFileSize = Integer.parseInt(value);
            }
            //automorphism
            else if (key.equalsIgnoreCase("automorphism")) {
                Settings.isAutomorphismOn = (value.compareTo("true") == 0);
            }
            //caching substructures
            else if (key.equalsIgnoreCase("caching")) {
                Settings.CACHING = (value.compareTo("true") == 0);
            }
            //number of buckets for clustering
            else if (key.equalsIgnoreCase("bucketsNum")) {
                Settings.bucketsNum = Integer.parseInt(value);
            }
            //number of weighting functions
            else if (key.equalsIgnoreCase("numberOfEdgeWeights")) {
                Settings.numberOfEdgeWeights = Integer.parseInt(value);
            }
            //max iterations for k-means 
            else if (key.equalsIgnoreCase("iterations")) {
                Settings.numberOfIterations = Integer.parseInt(value);
            }
            //smart selection of the initial centroids for k-means
            else if (key.equalsIgnoreCase("smart")) {
                Settings.smart = (value.compareTo("true") == 0);
            }
            //relevance vectors are the weighting functions
            else if (key.equalsIgnoreCase("clusteringType")) {
                Settings.clusteringType = value;
            }
            //structure size
            else if (key.equalsIgnoreCase("actualNumOfEdgeWeights")) {
                Settings.actualNumOfEdgeWeights = Integer.parseInt(value);
            }
            //number of runs of k-means for each k
            else if (key.equalsIgnoreCase("kMeanRuns")) {
                Settings.numberOfRuns = Integer.parseInt(value);
            }
            //max value of K for k-means
            else if (key.equalsIgnoreCase("maxClusters")) {
                Settings.maxClustersNum = Integer.parseInt(value);
            }
            //find best clustering
            else if (key.equalsIgnoreCase("bestC")) {
                Settings.best = (value.compareTo("true") == 0);
            }
            //random clustering
            else if (key.equalsIgnoreCase("random")) {
                Settings.random = (value.compareTo("true") == 0);
            }
            //focus value for weight generation
            else if (key.equalsIgnoreCase("focus")) {
                Settings.focus = Integer.parseInt(value);
            }
            // Multiple runs
            else if (key.equalsIgnoreCase("multipleRuns")) {
                Settings.multipleRuns = Integer.parseInt(value);
            }
            // Seed for Random
            else if (key.equalsIgnoreCase("seed")) {
                Settings.seed = Integer.parseInt(value);
            }
            // Ignore labels on nodes
            else if (key.equalsIgnoreCase("ignoreLabels")) {
                Settings.NOLABEL = value.equalsIgnoreCase("true");
            }
            else if (key.compareTo("maxSize") == 0) {
                Settings.maxSize = Integer.parseInt(value);
            }

        }
        if (Settings.maxClustersNum == 0) {
            Settings.maxClustersNum = Settings.numberOfEdgeWeights / 10;
        }
    }
}