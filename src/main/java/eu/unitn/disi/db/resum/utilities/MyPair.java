/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.unitn.disi.db.resum.utilities;

import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author bluecopper
 */
public class MyPair<A, B> {
    
    private A a;
    private B b;

    public MyPair(A a, B b) {
        this.a = a;
        this.b = b;
    }

    public A getA() {
        return a;
    }

    public B getB() {
        return b;
    }

    @Override
    public boolean equals(Object other) {
        //check for self-comparison
        if (this == other) {
            return true;
        }
        //actual comparison
        if (this.a instanceof String) {
            return ((String) this.a).equals((String) ((MyPair) other).getA());
        } else if (this.a instanceof Integer) {
            return (((Integer) this.a).intValue() == ((Integer) ((MyPair) other).getA()).intValue());
        } else if (this.a instanceof Double) {
            return (((Double) this.a).equals((Double) ((MyPair) other).getA()));
        }
        throw new UnsupportedOperationException("Unsupported Type Exception");
    }

    public boolean actualEquals(Object other) {
        //check for self-comparison
        if (this == other) {
            return true;
        }
        //actual comparison
        if (((Integer) this.a).intValue() == ((Integer) ((MyPair) other).getA()).intValue()) {
            return (((Double) this.b).equals(((Double) ((MyPair) other).getB())));
        }
        return false;
    }

    public static int getIndexOf(ArrayList<MyPair<Integer, Double>> arr, int a) {
        int i = 0;
        Iterator<MyPair<Integer, Double>> itr = arr.iterator();
        while (itr.hasNext()) {
            if (itr.next().getA() == a) {
                return i;
            }
            i++;
        }
        return -1;
    }

    @Override
    public String toString() {
        return this.a.toString() + " " + this.b.toString();
    }
}
