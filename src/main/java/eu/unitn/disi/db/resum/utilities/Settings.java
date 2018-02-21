package eu.unitn.disi.db.resum.utilities;

public class Settings {

    public static boolean isAutomorphismOn = false;

    public static boolean CACHING = false;
    
    public static boolean NOLABEL = true;
    
    public static int MAX_COMPUTATION_TIME = Integer.MAX_VALUE; 

    //datasets folder
    public static String datasetsFolder = "/Users/bluecopper/Desktop/OutputData_ICDM/OutputData_AMAZON/OutputData_WSM/u1/frequency";
    //output folder
    public static String outputFolder = "/Users/bluecopper/Desktop/";
    //the input filename
    public static String inputFileName = "freebase-travel.lg";
    // graph weights filename
    public static String weightFileName = "electronics-2-pruned_50.w";
    // real pattern sets
    public static String patternFileName = "/Users/bluecopper/Desktop/OutputData_ICDM/BS";
    //the output filename
    public static String outputFileName = null;
    
    // -- Mining parameters --
    public static int task = 2;

    public static double relevance = -1;

    public static int frequency = 90;

    public static int structureSize = 50;
    
    public static int multipleRuns = 0;
    
    public static int seed = 42;

    // -- Clustering parameters --
    public static Integer bucketsNum = 25;

    public static boolean smart = false;

    public static int numberOfIterations = 100;

    public static int numberOfRuns = 100;

    public static int maxClustersNum = 1000;

    public static boolean best = false;
    
    public static boolean random = false;
    
    public static String clusteringType = "bucket";

    // -- Weight Generation Parameters --
    public static int focus = 50;

    public static int edge_focus = 100;
    
    public static int numberOfFunctions = 299;
    
    // -- Multi-threading Parameters
    public static int splitSize = 4;

    public static int maxSplitDepth = Integer.MAX_VALUE;

    public static int maxSplitCount = Integer.MAX_VALUE;
    
    public static int threadCount = 1;
    
    // -- Test Accuracy
    public static String clusteringFileName = "/Users/bluecopper/Desktop/OutputData_ICDM/OutputData_AMAZON/OutputData_MWSM-BUCK300";
             
    public static String exactFileName = "/Users/bluecopper/Desktop/OutputData_ICDM/OutputData_AMAZON/OutputData_MWSM";
    
    public static String approxFileName = "/Users/bluecopper/Desktop/OutputData_ICDM/OutputData_AMAZON/OutputData_MWSM-BUCK300";

}
