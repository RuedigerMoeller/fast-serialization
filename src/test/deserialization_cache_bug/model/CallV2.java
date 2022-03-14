package deserialization_cache_bug.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import deserialization_cache_bug.random.RandomGenerator;

public class CallV2 extends Call {
	private static final long serialVersionUID = 1L;
	
	private String subClassField;
	
	public CallV2(UUID id, CallDirection callDirection, String callerId, String callingParty, String receivingParty, String group, long start, long end, 
			String tag, List<String> accountCodes, List<Note> notes, List<CallEvent> events, String subClassField) {
		super(id, callDirection, callerId, callingParty, receivingParty, group, start, end, tag, accountCodes, notes, events);
		this.subClassField = subClassField;
	}

	public String getSubClassField() {
		return subClassField;
	}
	
	public static CallV2 generateBasicTestCall(RandomGenerator random) {
		UUID callId = random.nextUUID();
		CallDirection callDirection = CallDirection.OUTBOUND;
		String group = "";
		String tag = "";
		
		long callStart = random.nextInt(1_000_000);
		long ringStart = callStart + random.nextInt(30_000);
		long talkStart = ringStart + random.nextInt(30_000);
		long callEnd = talkStart + random.nextInt(30_000);
		
		String callingParty = "Calling Party " + random.nextInt(1000);
		String receivingParty = "Receiving Party " + random.nextInt(1000);
		String callerId = "Caller Id " + random.nextInt(1000);
		
		TrunkChannel trunkChannel = new TrunkChannel(random.nextInt(99), random.nextInt(24));
		List<TrunkChannel> trunkChannels = new ArrayList<>();
		trunkChannels.add(trunkChannel);
		
		List<String> accountCodes = null;
		List<Note> notes = null;
		List<CallRecording> recordings = null;
		
		List<CallEvent> events = new ArrayList<>();
		events.add(new CallEvent(random.nextLong(), CallEventType.DIALING, callingParty, receivingParty, group, callStart, ringStart, tag, trunkChannels, recordings));
		events.add(new CallEvent(random.nextLong(), CallEventType.RINGING, callingParty, receivingParty, group, ringStart, talkStart, tag, trunkChannels, recordings));
		events.add(new CallEvent(random.nextLong(), CallEventType.TALKING, callingParty, receivingParty, group, talkStart, callEnd, tag, trunkChannels, recordings));
		events.add(new CallEvent(random.nextLong(), CallEventType.DROP, callingParty, receivingParty, group, callEnd, callEnd, tag, trunkChannels, recordings));
		
		CallV2 call = new CallV2(callId, callDirection, callerId, callingParty, receivingParty, group, callStart, callEnd, tag, accountCodes, notes, events, ""+random.nextInt());
		return call;
	}
}
