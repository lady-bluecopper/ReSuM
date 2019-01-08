package eu.unitn.disi.db.resum.utilities;

public class Settings {

    public static boolean isAutomorphismOn = false;

    public static boolean CACHING = false;
    
    public static boolean NOLABEL = true;
    
    public static int MAX_COMPUTATION_TIME = Integer.MAX_VALUE; 

    //datasets folder
    public static String datasetsFolder = "";
    //output folder
    public static String outputFolder = "";
    //the input filename
    public static String inputFileName = "";
    // graph weights filename
    public static String weightFileName = "";
    // num of lines of weight file
    public static int weightFileSize = 0;
    // real pattern sets
    public static String patternFileName = "";
    //the output filename
    public static String outputFileName = null;
    
    // -- Mining parameters --
    public static int score = 2;

    public static double relevance = -1;

    public static int frequency = 90;

    public static int actualNumOfEdgeWeights = 50;
    
    public static int multipleRuns = 0;
    
    public static int seed = 42;
    
    public static int maxSize = 4;

    // -- Clustering parameters --
    public static Integer bucketsNum = 25;

    public static boolean smart = false;

    public static int numberOfIterations = 100;

    public static int numberOfRuns = 100;

    public static int maxClustersNum = 1000;

    public static boolean best = false;
    
    public static boolean random = false;
    
    public static String clusteringType = "bucket";

    public static int focus = 50;

    public static int numberOfEdgeWeights = 299;
    
}
