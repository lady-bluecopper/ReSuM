package eu.unitn.disi.db.resum.clustering.features;

import eu.unitn.disi.db.resum.utilities.Settings;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.stream.IntStream;

/**
 *
 * @author bluecopper
 */
public class PatternBasedFVCreator extends FVCreator {

    public PatternBasedFVCreator() throws IOException {
        super();
    }
    
    public ArrayList<ArrayList<Double>> readPatternSets() {
        ArrayList<ArrayList<Double>> patternSets = new ArrayList<ArrayList<Double>>(Settings.numberOfFunctions);
        for(int u = 0; u < Settings.numberOfFunctions; u ++) {
            patternSets.add(u, new ArrayList<Double>());
        }
        int numPatterns = 0;
        try (BufferedReader rows = new BufferedReader(new FileReader(Paths.get(Settings.patternFileName).toFile()))) {
            String line = rows.readLine();
            while (line != null) {
                if (line.startsWith("{")) {
                    final String[] fIDs = line.substring(1, line.length() - 1).split(",");
                    double thisPattern = numPatterns;
                    IntStream.range(0, fIDs.length).parallel().forEach(i -> {
                        int fID = Integer.parseInt(fIDs[i].trim());
                        patternSets.get(fID).add(thisPattern);
                    });
                    numPatterns++;
                }
                line = rows.readLine();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return patternSets;
    }

    public ArrayList<ArrayList<Double>> createFeatureVectors() {
        return readPatternSets();
    }

}
