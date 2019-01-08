package eu.unitn.disi.db.resum.utilities;

import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author bluecopper
 */
public class Pair<A, B> {
    
    private A a;
    private B b;

    public Pair(A a, B b) {
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
            return ((String) this.a).equals((String) ((Pair) other).getA());
        } else if (this.a instanceof Integer) {
            return (((Integer) this.a).intValue() == ((Integer) ((Pair) other).getA()).intValue());
        } else if (this.a instanceof Double) {
            return (((Double) this.a).equals((Double) ((Pair) other).getA()));
        }
        throw new UnsupportedOperationException("Unsupported Type Exception");
    }

    public boolean actualEquals(Object other) {
        //check for self-comparison
        if (this == other) {
            return true;
        }
        //actual comparison
        if (((Integer) this.a).intValue() == ((Integer) ((Pair) other).getA()).intValue()) {
            return (((Double) this.b).equals(((Double) ((Pair) other).getB())));
        }
        return false;
    }

    public static int getIndexOf(ArrayList<Pair<Integer, Double>> arr, int a) {
        int i = 0;
        Iterator<Pair<Integer, Double>> itr = arr.iterator();
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
