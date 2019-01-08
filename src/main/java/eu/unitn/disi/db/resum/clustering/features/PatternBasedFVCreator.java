package eu.unitn.disi.db.resum.clustering.features;

import com.koloboke.collect.map.hash.HashIntObjMap;
import com.koloboke.collect.map.hash.HashIntObjMaps;
import eu.unitn.disi.db.resum.utilities.Settings;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.stream.IntStream;

/**
 *
 * @author bluecopper
 */
public class PatternBasedFVCreator extends FVCreator {
    
    // Assumes the first line in the file is # num_patterns
    public HashIntObjMap<double[]> readPatternSets() {
        HashIntObjMap<double[]> patternSets = HashIntObjMaps.newMutableMap();
        
        int curPattern = 0;
        try (BufferedReader rows = new BufferedReader(new FileReader(Paths.get(Settings.patternFileName).toFile()))) {
            String line = rows.readLine();
            final int numPatterns = (line != null && line.startsWith("#")) ? Integer.parseInt(line.trim().split(" ")[1]) : 10000;
            IntStream.range(0, Settings.numberOfEdgeWeights).forEach(u -> patternSets.put(u, new double[numPatterns]));
            
            while (line != null) {
                if (line.startsWith("{")) {
                    final String[] fIDs = line.substring(1, line.length() - 1).split(",");
                    final int thisPattern = curPattern;
                    IntStream.range(0, fIDs.length).parallel().forEach(i -> {
                        int fID = Integer.parseInt(fIDs[i].trim());
                        double[] curr = patternSets.get(fID);
                        curr[thisPattern] = 1.0;
                        patternSets.put(fID, curr);
                    });
                    curPattern++;
                }
                line = rows.readLine();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return patternSets;
    }

    public HashIntObjMap<double[]> createFeatureVectors() {
        return readPatternSets();
    }

}
