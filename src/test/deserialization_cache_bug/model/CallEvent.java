package deserialization_cache_bug.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CallEvent implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private long eventId;
	private CallEventType eventType;
	private String callingParty;
	private String receivingParty;
	private String group;
	private long start;
	private long end;
	
	private String tag;
	private List<TrunkChannel> trunkChannels;
	
	private List<CallRecording> recordings;

	public CallEvent(long eventId, CallEventType eventType, String callingParty, String receivingParty, String group,
			long start, long end, String tag, List<TrunkChannel> trunkChannels, List<CallRecording> recordings) {
		this.eventId = eventId;
		this.eventType = eventType;
		this.callingParty = callingParty;
		this.receivingParty = receivingParty;
		this.group = group;
		this.start = start;
		this.end = end;
		this.tag = tag;
		this.trunkChannels = trunkChannels;
		this.recordings = recordings;
	}

	public long getEventId() {
		return eventId;
	}

	public void setEventId(long eventId) {
		this.eventId = eventId;
	}

	public CallEventType getEventType() {
		return eventType;
	}

	public void setEventType(CallEventType eventType) {
		this.eventType = eventType;
	}

	public String getCallingParty() {
		return callingParty;
	}

	public void setCallingParty(String callingParty) {
		this.callingParty = callingParty;
	}

	public String getReceivingParty() {
		return receivingParty;
	}

	public void setReceivingParty(String receivingParty) {
		this.receivingParty = receivingParty;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public long getStart() {
		return start;
	}

	public void setStart(long start) {
		this.start = start;
	}

	public long getEnd() {
		return end;
	}

	public void setEnd(long end) {
		this.end = end;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public List<TrunkChannel> getTrunkChannels() {
		return trunkChannels;
	}

	public void setTrunkChannels(List<TrunkChannel> trunkChannels) {
		this.trunkChannels = trunkChannels;
	}

	public List<CallRecording> getRecordings() {
		return recordings;
	}

	public void setRecordings(List<CallRecording> recordings) {
		this.recordings = recordings;
	}

	public void addRecording(CallRecording recording) {
		if(recordings == null) {
			recordings = new ArrayList<>();
		}
		recordings.add(recording);
	}

	public void setRecordingsAsSaved() {
		if(recordings != null) {
			for(CallRecording recording : recordings) {
				recording.setStatus(RecordingStatus.SAVED);
			}
		}
	}
}
