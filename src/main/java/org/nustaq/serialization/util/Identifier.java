package org.nustaq.serialization.util;

/**
 * Created by fabianterhorst on 24.09.16.
 */

import java.util.Arrays;

/**
 * <p>The <tt>Identifier</tt> is a 128bit unique identifier
 * which can be used to uniquely identify resources like
 * connections, messages or objects.</p>
 * <p>Identifiers should be created using the static methods
 * of this class and can probably be pooled for resource
 * efficient programming models.</p>
 */
public final class Identifier {

    private static final char[] CHARS = {48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 65, 66, 67, 68, 69, 70};

    private final byte[] data;

    private Identifier(byte[] data) {
        this.data = data;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(data);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Identifier other = (Identifier) obj;
        return Arrays.equals(data, other.data);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        char[] chars = new char[36];
        int index = 0;
        for (int i = 0; i < 16; i++) {
            if (i == 4 || i == 6 || i == 8 || i == 10) {
                chars[index++] = '-';
            }
            chars[index++] = CHARS[(data[i] & 0xF0) >>> 4];
            chars[index++] = CHARS[(data[i] & 0x0F)];
        }
        return new String(chars);
    }
}
