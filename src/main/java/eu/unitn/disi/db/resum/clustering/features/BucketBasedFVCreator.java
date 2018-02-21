/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.unitn.disi.db.resum.clustering.features;

import eu.unitn.disi.db.resum.clustering.binning.Binning;
import eu.unitn.disi.db.resum.clustering.binning.EquiDepthBinning;
import eu.unitn.disi.db.resum.utilities.Settings;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author bluecopper
 */
public class BucketBasedFVCreator extends FVCreator {

    private HashMap<Double, ArrayList<Integer>> edgesByLabel;
    
    private Binning binner;
    
    public BucketBasedFVCreator() throws IOException {
        super();
        loadGraph();
        binner = new EquiDepthBinning(edgesByLabel.size());
    }

    private void loadGraph() throws IOException {
        final BufferedReader rows = new BufferedReader(new FileReader(
                Paths.get(Settings.datasetsFolder, Settings.inputFileName).toFile()));
        edgesByLabel = new HashMap<Double, ArrayList<Integer>>();

        String line = rows.readLine();
        int counter = 0;
        while (line != null) {
            if (line.startsWith("e")) {
                final String[] parts = line.split("\\s+");
                final double label = Double.parseDouble(parts[3]);
                if (!edgesByLabel.containsKey(label)) {
                    edgesByLabel.put(label, new ArrayList<Integer>());
                }
                ArrayList<Integer> members = edgesByLabel.get(label);
                members.add(counter);
                edgesByLabel.put(label, members);
                counter++;
            }
            line = rows.readLine();
        }
        rows.close();
    }

    public ArrayList<ArrayList<Double>> createFeatureVectors() {
        return binner.createFeatureVectors(edgesByLabel, edgeWeights);
    }
    
}
