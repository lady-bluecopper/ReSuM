package eu.unitn.disi.db.resum.und.utilities;

public class Settings {

    public static boolean isAutomorphismOn = true;

    public static boolean CACHING = true;

    //datasets folder
    public static String datasetsFolder = "";
    //output folder
    public static String outputFolder = "";
    //the input filename
    public static String inputFileName = "";
    // graph weights filename
    public static String weightFileName = "";
    // num lines of weight file
    public static int weightFileSize = 0;
    //the output filename
    public static String outputFileName = "";
    
    // -- Mining parameters --
    // 1 = all, 2 = any, 3 = sum, 4 = avg
    public static int score = 1;

    public static double relevance = 0.05;

    public static int frequency = 100;
    
    public static int maxSize = 3;

    public static int actualNumOfEdgeWeights = 1;
    
    public static int numberOfEdgeWeights = 1;
    
    public static int multipleRuns = 0;
    
    public static int seed = 42;
    
    public static String clusteringType = "bucket";

    // -- Other Parameters
    public static int MAX_COMPUTATION_TIME = Integer.MAX_VALUE; 
    
    public static int focus = 50;

    public static Integer bucketsNum = 10;

}
