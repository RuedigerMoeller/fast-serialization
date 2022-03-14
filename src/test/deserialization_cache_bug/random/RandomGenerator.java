package deserialization_cache_bug.random;

import java.util.UUID;

public interface RandomGenerator {
	public int nextInt();
	public int nextInt(int bound);
	public long nextLong();
	public long nextLong(long bound);
	public UUID nextUUID();
}
