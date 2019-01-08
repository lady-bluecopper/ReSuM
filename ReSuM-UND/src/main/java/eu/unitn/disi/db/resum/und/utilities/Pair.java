package eu.unitn.disi.db.resum.und.utilities;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;

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
    public boolean equals(Object aThat) {
        //check for self-comparison
        if ( this == aThat ) {
            return true;
        }
        //actual comparison
        if (this.a instanceof Integer) {
            return ((Integer)this.a).equals((Integer)((Pair)aThat).getA());
        }
        if (this.a instanceof String) {
            return ((String)this.a).equals((String)((Pair)aThat).getA());
        }
        throw new UnsupportedOperationException("Unrecognized Type");
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 13 * hash + Objects.hashCode(this.a);
        return hash;
    }
	  
    public static int getIndexOf(ArrayList<Pair<Integer, Double>> arr, int a) {
        int i = 0;
        Iterator<Pair<Integer, Double>> itr = arr.iterator(); 
        while(itr.hasNext()) {
            if(itr.next().getA()==a) {
		return i;
            }
            i++;
        } 
        return -1;
    }

}
