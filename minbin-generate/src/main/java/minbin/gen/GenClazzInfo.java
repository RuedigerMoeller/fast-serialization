package minbin.gen;

import org.nustaq.kontraktor.annotations.RemoteActorInterface;
import org.nustaq.serialization.FSTClazzInfo;

import java.util.List;

/**
 * Created by ruedi on 01.10.14.
 */
public class GenClazzInfo {
	FSTClazzInfo clzInfo;
    List<MsgInfo> msgs;

	public GenClazzInfo(FSTClazzInfo clzInfo) {
		this.clzInfo = clzInfo;
		init();
	}

	private void init() {

	}

    public FSTClazzInfo getClzInfo() {
        return clzInfo;
    }

    public void setMsgs(List<MsgInfo> msgs) {
        this.msgs = msgs;
    }

    public List<MsgInfo> getMsgs() {
        return msgs;
    }

    public boolean isActor() {
        return msgs != null;
    }

	// actor definition to be implemented on client
	public boolean isClientSide() {
		return clzInfo.getClazz().getAnnotation(RemoteActorInterface.class) != null;
	}
}
