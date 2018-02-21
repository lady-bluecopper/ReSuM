package eu.unitn.disi.db.resum.distance;

import eu.unitn.disi.db.resum.utilities.MyTriplet;
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
        for (String p : realP) {
            sumED += findMINDistancePerPattern(p, approxP);
        }
        return sumED / realP.size();
    }
    
    public MyTriplet<Double, Double, Double> distanceWithPrints(Collection<String> realP, Collection<String> approxP) {
        double sumED = 0;
        double minED = Double.MAX_VALUE;
        double maxED = 0;
        for (String p : realP) {
            double currentED = findMINDistancePerPattern(p, approxP);
            sumED += currentED;
            minED = Math.min(minED, currentED);
            maxED = Math.max(maxED, currentED);
        }
        return new MyTriplet(sumED / realP.size(), minED, maxED);
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
        for (int i = 0; i < second.length; i++) {
            if (second[i].size() > 0) {
                realUsers ++;
                sumDistances += distance(first, second[i]);
            }
        }
        return (realUsers > 0) ? sumDistances / realUsers : 0;
    }
    
    public MyTriplet<Double, Double, Double> computeAVGDistancePerUsersWithPrints(Collection<String> first, Collection<String>[] second) {
        double sumDistances = 0;
        double minDistance = Double.MAX_VALUE;
        double maxDistance = 0;
        double realUsers = 0;
        for (int i = 0; i < second.length; i++) {
            if (second[i] != null) {
            if (second[i].size() > 0) {
                realUsers ++;
                MyTriplet<Double, Double, Double> currentDistances = distanceWithPrints(first, second[i]);
                sumDistances += currentDistances.getA();
                minDistance = Math.min(minDistance, currentDistances.getB());
                maxDistance = Math.max(maxDistance, currentDistances.getC());
            }
            }
        }
        return new MyTriplet<Double, Double, Double> ((realUsers > 0) ? sumDistances / realUsers : 0, minDistance, maxDistance);
    }

    public double findMINDistancePerPattern(String p, Collection<String> pSet) {
        double minED = 1;
        for (String p2 : pSet) {
            minED = Math.min(minED,lev.distance(p, p2));
        }
        return minED;
    }

}
