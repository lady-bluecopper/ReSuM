/**
 * Copyright 2014 Mohammed Elseidy, Ehab Abdelhamid
 *
 * This file is part of Grami.
 *
 * Grami is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * Grami is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Grami.  If not, see <http://www.gnu.org/licenses/>.
 */
package eu.unitn.disi.db.resum.und.utilities;

import com.koloboke.collect.map.hash.HashIntObjMap;
import com.koloboke.collect.map.hash.HashIntObjMaps;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;

import sa.edu.kaust.grami.und.csp.VariableCandidates;

public class Util {

    public static ArrayList<Pair<HashSet<Integer>, MultiUserWeightedEdge<Integer, Double, double[]>>> getIntersection(
            ArrayList<Pair<Integer, ArrayList<MultiUserWeightedEdge<Integer, Double, double[]>>>> sets) {
        
        ArrayList<Pair<HashSet<Integer>, MultiUserWeightedEdge<Integer, Double, double[]>>> inter = 
                new ArrayList<Pair<HashSet<Integer>, MultiUserWeightedEdge<Integer, Double, double[]>>>();
        if (sets.size() == 0) {
            return inter;
        }
        Pair<Integer, ArrayList<MultiUserWeightedEdge<Integer, Double, double[]>>> firstSet = sets.get(0);
        if (firstSet == null) {
            return new ArrayList<Pair<HashSet<Integer>, MultiUserWeightedEdge<Integer, Double, double[]>>>();
        }
        for (int i = 0; i < firstSet.getB().size(); i++) {
            MultiUserWeightedEdge<Integer, Double, double[]> element = firstSet.getB().get(i);
            boolean doesIntersect = true;
            HashSet<Integer> otherNodes = new HashSet<Integer>();
            otherNodes.add(firstSet.getA());
            for (int j = 1; j < sets.size(); j++) {
                Pair<Integer, ArrayList<MultiUserWeightedEdge<Integer, Double, double[]>>> otherset = sets.get(j);
                if (otherset == null) {
                    return new ArrayList<Pair<HashSet<Integer>, MultiUserWeightedEdge<Integer, Double, double[]>>>();
                }
                if (MultiUserWeightedEdge.getIndexOf(otherset.getB(), element.getNodeID(), element.getEdgeLabel()) == -1) {
                    doesIntersect = false;
                    break;
                } else {
                    otherNodes.add(otherset.getA());
                }
            }
            if (doesIntersect) {
                inter.add(new Pair<HashSet<Integer>, MultiUserWeightedEdge<Integer, Double, double[]>>(otherNodes, element));
            }
        }
        return inter;
    }

    public static HashSet<MultiUserWeightedEdge<Integer, Double, double[]>> getUnion(
            ArrayList<ArrayList<MultiUserWeightedEdge<Integer, Double, double[]>>> sets) {
        HashSet<MultiUserWeightedEdge<Integer, Double, double[]>> union = new HashSet<MultiUserWeightedEdge<Integer, Double, double[]>>();
        if (sets.size() == 0) {
            return union;
        }
        for (int j = 1; j < sets.size(); j++) {
            ArrayList<MultiUserWeightedEdge<Integer, Double, double[]>> theSet = sets.get(j);
            if (theSet == null) {
                continue;
            }
            for (int i = 0; i < theSet.size(); i++) {
                union.add(theSet.get(i));
            }
        }
        return union;
    }

    public static ArrayList<MultiUserWeightedEdge<Integer, Double, double[]>> getDifference(
            HashSet<MultiUserWeightedEdge<Integer, Double, double[]>> firstSet,
            ArrayList<MultiUserWeightedEdge<Integer, Double, double[]>> secondSet) {
        ArrayList<MultiUserWeightedEdge<Integer, Double, double[]>> diff = new ArrayList<MultiUserWeightedEdge<Integer, Double, double[]>>();

        for (MultiUserWeightedEdge<Integer, Double, double[]> element : firstSet) {
            if (MultiUserWeightedEdge.getIndexOf(secondSet, element.getNodeID(), element.getEdgeLabel()) == -1) {
                diff.add(element);
            }
        }
        return diff;
    }

    public static ArrayList<Integer> getIntersection(ArrayList<Integer> set1, ArrayList<Integer> set2) {
        ArrayList<Integer> inter = new ArrayList<Integer>();
        ArrayList<Integer> firstSet = set1;
        if (firstSet == null) {
            return new ArrayList<Integer>();
        }
        for (int i = 0; i < firstSet.size(); i++) {
            int element = firstSet.get(i);
            if (set2.contains(element)) {
                inter.add(element);
            }
        }
        return inter;
    }

    public static ArrayList<Point> getZerosIntersectionIndices(ArrayList<VariableCandidates> sets) {
        ArrayList<Point> points = new ArrayList<Point>();

        for (int i = 0; i < sets.size(); i++) {
            VariableCandidates firstVariableCandidate = sets.get(i);
            ArrayList<MultiUserWeightedEdge<Integer, Double, double[]>> firstElement = firstVariableCandidate.getCandidates();
            if (firstElement == null) {
                continue;
            }
            for (int j = i + 1; j < sets.size(); j++) {
                VariableCandidates secondVariableCandidate = sets.get(j);
                ArrayList<MultiUserWeightedEdge<Integer, Double, double[]>> secondElement = secondVariableCandidate.getCandidates();
                if (secondElement == null) {
                    continue;
                }
                boolean doesIntersect = false;
                for (int k = 0; k < firstElement.size(); k++) {
                    if (MultiUserWeightedEdge.getIndexOf(secondElement, firstElement.get(k).getNodeID(), firstElement.get(k).getEdgeLabel()) != -1) {
                        doesIntersect = true;
                        break;
                    }
                }
                if (doesIntersect == false) {
                    points.add(new Point(firstVariableCandidate.getVariableID(), secondVariableCandidate.getVariableID()));
                }
            }
        }
        return points;
    }

    public static HashIntObjMap<HashSet<Integer>> clone(HashIntObjMap<HashSet<Integer>> in) {
        HashIntObjMap<HashSet<Integer>> out = HashIntObjMaps.<HashSet<Integer>>newUpdatableMap();

        for (Iterator<Entry<Integer, HashSet<Integer>>> iterator = in.entrySet().iterator(); iterator.hasNext();) {
            Entry<Integer, HashSet<Integer>> entry = iterator.next();
            out.put(entry.getKey(), (HashSet<Integer>) entry.getValue().clone());
        }

        return out;
    }

    public static double minFraction(double[] a, int[] b) {
        double min = Double.MAX_VALUE;
        for (int i = 0; i < a.length; i++) {
            if (b[i] > 0) {
                if (a[i] / b[i] < min) {
                    min = a[i] / b[i];
                }
            }
        }
        return min;
    }

    public double[] incrementalSum(double[] a, double[] b) {
        double[] c = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            c[i] = a[i] + b[i];
        }
        return c;
    }
    
    public static String toPrint(Object a[]) {
        String print = "";
        for (Object el : a) {
            print += ";" + el.toString();
        }
        return print.substring(1);
    }
    
    public static String toPrint(double a[]) {
        StringBuilder builder = new StringBuilder();
        for (double el : a) {
            builder.append(String.format(";%f", el));
        }
        String print = builder.toString();
        if (print.length() > 0) {
            return print.substring(1);
        }
        return "";
    }

}
