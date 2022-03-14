package deserialization_cache_bug.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CallRecording implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private UUID id;
	private UUID callId;
	private long eventId;
	private String streamId;
	private RecordingStatus status;
	private String sipCallId;
	private long start;
	private long duration;
	private UUID locationId;
	private long size;
	private List<Timeframe> times;
	
	public CallRecording(UUID id, UUID callId, long eventId, String streamId, RecordingStatus status, String sipCallId,
			long start, long duration, UUID locationId, long size, List<Timeframe> times) {
		this.id = id;
		this.callId = callId;
		this.eventId = eventId;
		this.streamId = streamId;
		this.status = status;
		this.sipCallId = sipCallId;
		this.start = start;
		this.duration = duration;
		this.locationId = locationId;
		this.size = size;
		this.times = new ArrayList<>();
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public UUID getCallId() {
		return callId;
	}

	public void setCallId(UUID callId) {
		this.callId = callId;
	}

	public long getEventId() {
		return eventId;
	}

	public void setEventId(long eventId) {
		this.eventId = eventId;
	}

	public String getStreamId() {
		return streamId;
	}

	public void setStreamId(String streamId) {
		this.streamId = streamId;
	}

	public RecordingStatus getStatus() {
		return status;
	}

	public void setStatus(RecordingStatus status) {
		this.status = status;
	}

	public String getSipCallId() {
		return sipCallId;
	}

	public void setSipCallId(String sipCallId) {
		this.sipCallId = sipCallId;
	}

	public long getStart() {
		return start;
	}

	public void setStart(long start) {
		this.start = start;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public UUID getLocationId() {
		return locationId;
	}

	public void setLocationId(UUID locationId) {
		this.locationId = locationId;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}
	
	public List<Timeframe> getTimes() {
		return times;
	}
	
	public void setTimes(List<Timeframe> times) {
		this.times = times;
	}
}
