package deserialization_cache_bug.random;

import java.util.UUID;

public class IncrementingUUID {
	private long mostSignificantBits;
	private long leastSignificantBits;
	
	public IncrementingUUID() {
		this.mostSignificantBits = 0;
		this.leastSignificantBits = 0;
	}
	
	public IncrementingUUID(long mostSignificantBits, long leastSignificantBits) {
		this.mostSignificantBits = mostSignificantBits;
		this.leastSignificantBits = leastSignificantBits;
	}

	public synchronized UUID get() {
		return new UUID(mostSignificantBits, leastSignificantBits);
	}
	
	public synchronized UUID getAndIncrement() {
		UUID uuid = get();
		increment();
		return uuid;
	}
	
	public synchronized UUID incrementAndGet() {
		increment();
		UUID uuid = get();
		return uuid;
	}
	
	private synchronized void increment() {
		if(leastSignificantBits == -1) {
			mostSignificantBits++;
		}
		leastSignificantBits++;
	}
	
	public synchronized void reset() {
		mostSignificantBits = 0;
		leastSignificantBits = 0;
	}
}
