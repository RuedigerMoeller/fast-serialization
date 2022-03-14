package deserialization_cache_bug.model;

import java.io.Serializable;

public class Timeframe implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private long start;
	private long end;
	
	public Timeframe(long start, long end) {
		this.start = start;
		this.end = end;
	}
	
	public long getStart() {
		return start;
	}
	
	public long getEnd() {
		return end;
	}
}
