package eu.unitn.disi.db.resum.distance;

import eu.unitn.disi.db.resum.utilities.Triplet;
import info.debatty.java.stringsimilarity.NormalizedLevenshtein;
import java.util.Collection;

/**
 *
 * @author bluecopper
 */
public class SetEditDistance implements Distance<Collection<String>> {
    
    NormalizedLevenshtein lev;
    
    public SetEditDistance() {
        this.lev = new NormalizedLevenshtein();
    }

    public double distance(Collection<String> realP, Collection<String> approxP) {
        double sumED = 0;
        sumED = realP.stream()
                .map(p -> findMINDistancePerPattern(p, approxP))
                .reduce(sumED, (accumulator, _item) -> accumulator + _item);
        return sumED / realP.size();
    }
    
    public Triplet<Double, Double, Double> distanceWithPrints(Collection<String> realP, Collection<String> approxP) {
        double sumED = 0;
        double minED = Double.MAX_VALUE;
        double maxED = 0;
        for (String p : realP) {
            double currentED = findMINDistancePerPattern(p, approxP);
            sumED += currentED;
            minED = Math.min(minED, currentED);
            maxED = Math.max(maxED, currentED);
        }
        return new Triplet(sumED / realP.size(), minED, maxED);
    }

    public double computeAVGDistancePerUsers(Collection<String>[] realSets, Collection<String>[] approxSets) {
        double sumDistances = 0;
        double realUsers = 0;
        for (int i = 0; i < realSets.length; i++) {
            if (realSets[i].size() > 0) {
                realUsers ++;
                sumDistances += distance(realSets[i], approxSets[i]);
            }
        }
        return sumDistances / realUsers;
    }
    
    public double computeAVGDistancePerUsers(Collection<String> first, Collection<String>[] second) {
        double sumDistances = 0;
        double realUsers = 0;
        for (Collection<String> second1 : second) {
            if (second1.size() > 0) {
                realUsers ++;
                sumDistances += distance(first, second1);
            }
        }
        return (realUsers > 0) ? sumDistances / realUsers : 0;
    }
    
    public Triplet<Double, Double, Double> computeAVGDistancePerUsersWithPrints(Collection<String> first, Collection<String>[] second) {
        double sumDistances = 0;
        double minDistance = Double.MAX_VALUE;
        double maxDistance = 0;
        double realUsers = 0;
        for (Collection<String> second1 : second) {
            if (second1 != null) {
                if (second1.size() > 0) {
                    realUsers ++;
                    Triplet<Double, Double, Double> currentDistances = distanceWithPrints(first, second1);
                    sumDistances += currentDistances.getA();
                    minDistance = Math.min(minDistance, currentDistances.getB());
                    maxDistance = Math.max(maxDistance, currentDistances.getC());
                }
            }
        }
        return new Triplet<Double, Double, Double> ((realUsers > 0) ? sumDistances / realUsers : 0, minDistance, maxDistance);
    }

    public double findMINDistancePerPattern(String p, Collection<String> pSet) {
        double minED = 1;
        for (String p2 : pSet) {
            minED = Math.min(minED,lev.distance(p, p2));
        }
        return minED;
    }

}
