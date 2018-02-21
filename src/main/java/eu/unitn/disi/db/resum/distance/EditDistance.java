package eu.unitn.disi.db.resum.distance;

import eu.unitn.disi.db.resum.utilities.StopWatch;

/**
 *
 * @author bluecopper
 */
public class EditDistance {

    public static int wagnerFischerEditDistance(String lhs, String rhs) {
        int len0 = lhs.length() + 1;
        int len1 = rhs.length() + 1;

        // the array of distances                                                       
        int[] cost = new int[len0];
        int[] newcost = new int[len0];

        // initial cost of skipping prefix in String s0                                 
        for (int i = 0; i < len0; i++) {
            cost[i] = i;
        }
        // transformation cost for each letter in s1                                    
        for (int j = 1; j < len1; j++) {
            // initial cost of skipping prefix in String s1                             
            newcost[0] = j;
            // transformation cost for each letter in s0                                
            for (int i = 1; i < len0; i++) {
                // matching current letters in both strings                             
                int match = (lhs.charAt(i - 1) == rhs.charAt(j - 1)) ? 0 : 1;
                // keep minimum cost                                                    
                newcost[i] = Math.min(Math.min(cost[i] + 1, newcost[i - 1] + 1), cost[i - 1] + match);
            }
            // swap cost/newcost arrays                                                 
            int[] swap = cost;
            cost = newcost;
            newcost = swap;
        }
        // the distance is the cost for transforming all letters in both strings        
        return cost[len0 - 1];
    }
    
    public static double normalizedWagnerFischerEditDistance(String s1, String s2) {
        return wagnerFischerEditDistance(s1, s2) / (double) s1.length();
    }

    public static void main(String[] args) {
        String s1 = "ciaonvilvbfdajvbkfjvbdfjkvbfjkbvkjvbkjsbvjksf";
        String s2 = "cisdsnldknlkdskldsndlkandklandlksandlksandlkasndlasksbvjksf";
        StopWatch w1 = new StopWatch();
        w1.start();
        System.out.println(EditDistance.wagnerFischerEditDistance(s1, s2) / (double) s1.length());
        w1.stop();
        System.out.println(w1.getElapsedTimeSecs());
   }

}
