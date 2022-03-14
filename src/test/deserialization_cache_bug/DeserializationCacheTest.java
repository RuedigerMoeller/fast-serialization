package deserialization_cache_bug;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.nustaq.serialization.FSTConfiguration;

import deserialization_cache_bug.model.Call;
import deserialization_cache_bug.model.CallEvent;
import deserialization_cache_bug.model.CallRecording;
import deserialization_cache_bug.model.CallV2;
import deserialization_cache_bug.model.Note;
import deserialization_cache_bug.model.RecordingStatus;
import deserialization_cache_bug.model.Timeframe;
import deserialization_cache_bug.random.DeterministicRandomGenerator;
import deserialization_cache_bug.random.RandomGenerator;

public class DeserializationCacheTest {
	private RandomGenerator random= new DeterministicRandomGenerator();
	
	@Test
	public void testDeserializationCacheIssue() {
		int testCount = 30;
		int mutationCountPerTest = 15;
		int callCountPerTest = 100;
		
		FSTConfiguration serializer = FSTConfiguration.createDefaultConfiguration();
		
		for(int i = 0; i < testCount; i++) {
			try {
				testSerializationAndMutationOfTestCalls(i, mutationCountPerTest, callCountPerTest, serializer);
			} catch (Exception e) {
				e.printStackTrace();
				fail();
			}
		}
	}

	private void testSerializationAndMutationOfTestCalls(int testIndex, int mutationCountPerTest, int callCountPerTest, FSTConfiguration serializer) throws Exception {
		ArrayList<byte[]> currentCallsAsBytes = generateTestCallsAsBytes(callCountPerTest, serializer);
		ArrayList<byte[]> mutatedCallsAsBytes = new ArrayList<>();
		
		for(int i = 0; i < mutationCountPerTest; i++) {
			for(int j = 0; j < currentCallsAsBytes.size(); j++) {
				System.out.println(testIndex + "-" + i + "-" + j);
				Call originalCall = (Call) serializer.asObject(currentCallsAsBytes.get(j));
				
				ObjectValidation.validateFieldValueTypesForObject(originalCall);
				
				mutateCall(originalCall);
				
				byte[] mutatedBytes = serializer.asByteArray(originalCall);
				
				mutatedCallsAsBytes.add(mutatedBytes);
			}
			
			currentCallsAsBytes = mutatedCallsAsBytes;
			mutatedCallsAsBytes = new ArrayList<>();
		}
	}
	
	private ArrayList<byte[]> generateTestCallsAsBytes(int callCountPerTest, FSTConfiguration serializer) throws Exception {
		ArrayList<byte[]> testCallsAsBytes = new ArrayList<>();
		for(int i = 0; i < callCountPerTest; i++) {
			Call call = CallV2.generateBasicTestCall(random);
			testCallsAsBytes.add(serializer.asByteArray(call));
		}
		return testCallsAsBytes;
	}

	private void mutateCall(Call call) {
		switch(random.nextInt(4)) {
		case 0:
			addNote(call);
			break;
		case 1:
			addRecording(call);
			break;
		case 2:
			addAccountCode(call);
			break;
		case 3:
			setRecordingAsSaved(call);
			break;
		}
	}

	private void addNote(Call call) {
		Note note = new Note(random.nextUUID(), call.getStart(), "Derek Johnson(210)", "This is a rad note!", System.currentTimeMillis());
		call.addNote(note);
	}

	private void addRecording(Call call) {
		CallEvent firstEvent = call.getEvents().get(0);
		
		List<Timeframe> times = new ArrayList<>();
		times.add(new Timeframe(firstEvent.getStart(), firstEvent.getEnd()));
		
		CallRecording recording = new CallRecording(random.nextUUID(), call.getId(), firstEvent.getEventId(), "Stream " + random.nextInt(), RecordingStatus.PENDING, "SIP-ID" + random.nextInt(), call.getStart(), call.getEnd() - call.getStart(), random.nextUUID(), 1_000_000, times);
		call.addRecording(firstEvent.getEventId(), recording);
	}

	private void addAccountCode(Call call) {
		List<String> accountCodes = call.getAccountCodes();
		if(accountCodes == null) {
			accountCodes = new ArrayList<>();
		}
		accountCodes.add("My new account code" + random.nextInt(1000));
		call.setAccountCodes(accountCodes);
	}

	private void setRecordingAsSaved(Call call) {
		long eventId = call.getEvents().get(0).getEventId();
		call.setRecordingsOnEventAsSaved(eventId);
	}
}
