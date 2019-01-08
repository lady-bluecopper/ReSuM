package eu.unitn.disi.db.resum.und.utilities;

public class CommandLineParser {

    public static void parse(String[] args) {
        for (String arg : args) {
            String[] parts = arg.split("=");
            String key = parts[0];
            String value = parts[1];
            //get frequency
            if (key.compareTo("freq") == 0) {
                Settings.frequency = Integer.parseInt(value);
            }
            //get relevance
            else if (key.compareTo("rel") == 0) {
                Settings.relevance = Double.parseDouble(value);
            }
            //get task
            else if (key.compareTo("score") == 0) {
                Settings.score = Integer.parseInt(value);
            }
            //get dataset filename
            else if (key.compareTo("inputFileName") == 0) {
                Settings.inputFileName = value;
            }
            //get weight filename
            else if (key.compareTo("weightFileName") == 0) {
                Settings.weightFileName = value;
            }
            //get output filename
            else if (key.compareTo("outputFileName") == 0) {
                Settings.outputFileName = value;
            }
            //assign datasets folder
            else if (key.compareTo("datasetsFolder") == 0) {
                Settings.datasetsFolder = value;
            }
            //assign output folder
            else if (key.compareTo("outputFolder") == 0) {
                Settings.outputFolder = value;
            }
            else if (key.compareTo("weightFileSize") == 0) {
                Settings.weightFileSize = Integer.parseInt(value);
            }
            //automorphism
            else if (key.compareTo("automorphism") == 0) {
                Settings.isAutomorphismOn = (value.compareTo("true") == 0);
            }
            //caching substructures
            else if (key.compareTo("caching") == 0) {
                Settings.CACHING = (value.compareTo("true") == 0);
            }
            //number of buckets for clustering
            else if (key.compareTo("bucketsNum") == 0) {
                Settings.bucketsNum = Integer.parseInt(value);
            }
            //number of weighting functions
            else if (key.compareTo("numberOfEdgeWeights") == 0) {
                Settings.numberOfEdgeWeights = Integer.parseInt(value);
            }
            //structure size
            else if (key.compareTo("actualNumOfEdgeWeights") == 0) {
                Settings.actualNumOfEdgeWeights = Integer.parseInt(value);
            }
            //focus value for weight generation
            else if (key.compareTo("focus") == 0) {
                Settings.focus = Integer.parseInt(value);
            }
            // Multiple runs
            else if (key.compareTo("multipleRuns") == 0) {
                Settings.multipleRuns = Integer.parseInt(value);
            }
            // Seed for Random
            else if (key.compareTo("seed") == 0) {
                Settings.seed = Integer.parseInt(value);
            }
            // Max size of Patterns
            else if (key.compareTo("maxSize") == 0) {
                Settings.maxSize = Integer.parseInt(value);
            }
        }
    }
}
