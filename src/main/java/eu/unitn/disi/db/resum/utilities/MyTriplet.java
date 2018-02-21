package eu.unitn.disi.db.resum.utilities;

import java.util.ArrayList;
import java.util.Iterator;

public class MyTriplet<A, B, C> {

    private A a;
    private B b;
    private C c;

    public MyTriplet(A a, B b, C c) {
        this.a = a;
        this.b = b;
        this.c = c;
    }

    public A getA() {
        return a;
    }

    public B getB() {
        return b;
    }

    public C getC() {
        return c;
    }

    @Override
    public boolean equals(Object other) {
        //check for self-comparison
        if (this == other) {
            return true;
        }
        //actual comparison
        return (((Integer) this.a).intValue() == ((Integer) ((MyTriplet) other).getA()).intValue());
    }

    public boolean actualEquals(Object other) {
        //check for self-comparison
        if (this == other) {
            return true;
        }
        //actual comparison
        if (((Integer) this.a).intValue() == ((Integer) ((MyTriplet) other).getA()).intValue()) {
            if (((Double) this.b).equals(((Double) ((MyTriplet) other).getB()))) {
                return (((double[]) this.c).equals(((double[]) ((MyTriplet) other).getC())));
            }
            return false;
        }
        return false;
    }

    public static int getIndexOf(ArrayList<MyTriplet<Integer, Double, double[]>> arr, int a) {
        int i = 0;
        Iterator<MyTriplet<Integer, Double, double[]>> itr = arr.iterator();
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
        return this.a.toString() + "\t" + this.b.toString() + "\t" + this.c.toString();
    }

}
