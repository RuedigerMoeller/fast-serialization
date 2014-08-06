package org.nustaq.serialization;

/**
 * Created by ruedi on 06.08.14.
 */
public interface VersionConflictListener {
    public void onOldVersionRead(Object newReadFromOldSerialized);
}
