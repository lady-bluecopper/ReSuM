package eu.unitn.disi.db.resum.distance;

/**
 *
 * @author bluecopper
 * @param <T>
 */
public interface Distance<T> {
    
    public double distance(T first, T second);
    
}
