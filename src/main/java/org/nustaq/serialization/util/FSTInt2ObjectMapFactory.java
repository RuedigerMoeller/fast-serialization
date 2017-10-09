package org.nustaq.serialization.util;

public interface FSTInt2ObjectMapFactory {
    <V> FSTInt2ObjectMap<V> createMap(int size);
}