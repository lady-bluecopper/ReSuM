package eu.unitn.disi.db.resum.clustering.features;

import eu.unitn.disi.db.resum.utilities.Settings;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 *
 * @author bluecopper
 */
public abstract class FVCreator {
    
    protected ArrayList<double[]> edgeWeights;
    
    public FVCreator() throws IOException {
        this.edgeWeights = readWeightFile();
    }
    
    public abstract ArrayList<ArrayList<Double>> createFeatureVectors();
    
    protected final ArrayList<double[]> readWeightFile() throws IOException {
        final BufferedReader rows = new BufferedReader(new FileReader(Paths.get(Settings.datasetsFolder, Settings.weightFileName).toFile()));
        ArrayList<double[]> weights = new ArrayList<double[]>();
        int user = 0;
        String line;
        String[] parts;

        line = rows.readLine();
        parts = line.split("\\s+");
        for (int e = 0; e < parts.length; e++) {
            weights.add(e, new double[Settings.numberOfFunctions]);
        }
        while (line != null && user < Settings.numberOfFunctions) {
            parts = line.split("\\s+");
            for (int e = 0; e < parts.length; e++) {
                weights.get(e)[user] = Double.parseDouble(parts[e]);
            }
            line = rows.readLine();
            user++;
        }
        rows.close();
        return weights;
    }
    
    public ArrayList<double[]> getEdgeWeights() {
        return edgeWeights;
    }
}
