package deserialization_cache_bug.model;

import java.io.Serializable;
import java.util.UUID;

public class Note implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private UUID id;
	private long time;
	private String user;
	private String note;
	private long timeCreated;
	
	public Note(UUID id, long time, String user, String note, long timeCreated) {
		this.id = id;
		this.time = time;
		this.user = user;
		this.note = note;
		this.timeCreated = timeCreated;
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public long getTimeCreated() {
		return timeCreated;
	}

	public void setTimeCreated(long timeCreated) {
		this.timeCreated = timeCreated;
	}
}
