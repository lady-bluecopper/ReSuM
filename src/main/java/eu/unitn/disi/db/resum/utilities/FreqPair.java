package eu.unitn.disi.db.resum.utilities;

public class FreqPair<T> {
	private Integer freq;
	private Integer anyFreq;
	
	public FreqPair(Integer freq, Integer anyFreq) {
		this.freq = freq;
		this.anyFreq = anyFreq;
	}
	
	public Integer getFreq() {return freq;}
	public Integer getanyFreq() {return anyFreq;}

	@SuppressWarnings("rawtypes")
	@Override public boolean equals(Object other) {
		    //check for self-comparison
		    if ( this == other ) return true;
		    //actual comparison
		    if(this.freq == ((FreqPair) other).getFreq()) return true;
		    return false;
}
}
