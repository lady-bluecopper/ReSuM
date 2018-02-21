package eu.unitn.disi.db.resum.mst;

import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author bluecopper
 */
public class DistinctSet<T> {
    
    private ArrayList<Link> edges;
    
    public ArrayList<Link> getEdges() {
        return edges;
    }
    
    public DistinctSet() {
        this.edges = new ArrayList<Link>();
    }
    
    public DistinctSet(ArrayList<Link> edges) {
        this.edges = edges;
    }
    
    public boolean containsLink(Link e) {
        return edges.contains(e);
    }
    
    public boolean containsLink(int src, int dst) {
        for (Link e : edges) {
            if ((e.src == src && e.dst == dst) || (e.dst == src && e.src == dst)) {
                return true;
            }
        }
        return false;
    }
    
    public boolean removeLink(Link e) {
        return edges.remove(e);
    }
    
    public boolean removeLink(int src, int dst) {
        for (Link e : edges) {
            if ((e.src == src && e.dst == dst) || (e.dst == src && e.src == dst)) {
                edges.remove(e);
                return true;
            }
        }
        return false;
    }
    
    public boolean addLink(Link e) {
        return edges.add(e);
    }
    
    public boolean addLink(int src, int dst) {
        return edges.add(new Link(src, dst, 0));
    }
    
    public boolean addAllLinks(Collection<Link> links) {
        return edges.addAll(links);
    }
    
}
