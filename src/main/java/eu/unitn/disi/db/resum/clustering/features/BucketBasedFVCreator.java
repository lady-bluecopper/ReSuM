package eu.unitn.disi.db.resum.clustering.features;

import com.koloboke.collect.map.hash.HashDoubleObjMap;
import com.koloboke.collect.map.hash.HashDoubleObjMaps;
import com.koloboke.collect.map.hash.HashIntObjMap;
import eu.unitn.disi.db.resum.clustering.binning.Binning;
import eu.unitn.disi.db.resum.clustering.binning.DynEquiDepthBinning;
import eu.unitn.disi.db.resum.clustering.binning.EquiDepthBinning;
import eu.unitn.disi.db.resum.utilities.Settings;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import sa.edu.kaust.grami.dataStructures.MultiUserWeightedEdge;

/**
 *
 * @author bluecopper
 */
public class BucketBasedFVCreator extends FVCreator {

    private HashDoubleObjMap<List<MultiUserWeightedEdge<Integer, Double, double[]>>> edgesByLabel;
    
    private Binning binner;
    
    public BucketBasedFVCreator(HashIntObjMap<double[]> edgeWeights) throws IOException {
        loadGraph(edgeWeights);
        System.out.println("graph loaded");
        this.binner = (Settings.clusteringType.equals("dyn")) ? new DynEquiDepthBinning(edgesByLabel.size()) : new EquiDepthBinning(edgesByLabel.size());
    }

    private void loadGraph(HashIntObjMap<double[]> edgeWeights) throws IOException {
        final BufferedReader rows = new BufferedReader(new FileReader(
                Paths.get(Settings.datasetsFolder, Settings.inputFileName).toFile()));
        edgesByLabel = HashDoubleObjMaps.newMutableMap();

        String line = rows.readLine();
        int counter = 0;
        while (line != null) {
            if (line.startsWith("e")) {
                final String[] parts = line.split("\\s+");
                final double label = Double.parseDouble(parts[3]);
                List<MultiUserWeightedEdge<Integer, Double, double[]>> members = edgesByLabel.getOrDefault(label, new ArrayList<>());
                members.add(new MultiUserWeightedEdge<>(counter, label, edgeWeights.get(counter)));
                edgesByLabel.put(label, members);
                counter++;
            }
            line = rows.readLine();
        }
        rows.close();
    }

    public HashIntObjMap<double[]> createFeatureVectors() {
        return binner.createFeatureVectors(edgesByLabel);
    }
    
}
