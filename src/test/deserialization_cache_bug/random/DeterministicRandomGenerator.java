package deserialization_cache_bug.random;

import java.util.Random;
import java.util.UUID;

public class DeterministicRandomGenerator implements RandomGenerator {
	private IncrementingUUID nextUuid = new IncrementingUUID();
	private Random r = new Random(1337);
	
	@Override
	public int nextInt() {
		return r.nextInt();
	}
	
	@Override
	public int nextInt(int bound) {
		return r.nextInt(bound);
	}
	
	@Override
	public long nextLong() {
		return r.nextLong();
	}
	
	@Override
	public long nextLong(long bound) {
		return r.nextLong() % bound;
	}
	
	@Override
	public UUID nextUUID() {
		return nextUuid.getAndIncrement();
	}
}
