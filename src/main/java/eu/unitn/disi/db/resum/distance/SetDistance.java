/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.unitn.disi.db.resum.distance;

import eu.unitn.disi.db.resum.utilities.Util;
import java.util.HashSet;

/**
 *
 * @author bluecopper
 */
public class SetDistance implements Distance<HashSet<Integer>> {

    public double distance(HashSet<Integer> first, HashSet<Integer> second) {
        return Util.differenceSize(first, second) + Util.differenceSize(second, first);
    }
    
    public double similarity(HashSet<Integer> first, HashSet<Integer> second) {
        return Util.intersectionSize(first, second);
    }
    
}
