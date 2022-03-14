package deserialization_cache_bug.model;

import java.io.Serializable;

public class TrunkChannel implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private int trunk;
	private int channel;
	
	public TrunkChannel(int trunk, int channel) {
		this.trunk = trunk;
		this.channel = channel;
	}

	public int getTrunk() {
		return trunk;
	}

	public void setTrunk(int trunk) {
		this.trunk = trunk;
	}

	public int getChannel() {
		return channel;
	}

	public void setChannel(int channel) {
		this.channel = channel;
	}
}
