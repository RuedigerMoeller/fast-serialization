package de.ruedigermoeller.serialization.minbin;

/**
 * Created by ruedi on 02.05.14.
 */
public class MBRef {
    int streamPosition;

    public MBRef(int streamPosition) {
        this.streamPosition = streamPosition;
    }

    public int getStreamPosition() {
        return streamPosition;
    }

    public void setStreamPosition(int streamPosition) {
        this.streamPosition = streamPosition;
    }

    @Override
    public String toString() {
        return "MBRef("+streamPosition +')';
    }
}
