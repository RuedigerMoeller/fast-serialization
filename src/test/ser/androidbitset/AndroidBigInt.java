package ser.androidbitset;

/**
 * Created by ruedi on 08/02/15.
 */

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Random;

/**
 * An immutable signed integer of arbitrary magnitude.
 *
 * <h3>Fast Cryptography</h3>
 * This implementation is efficient for operations traditionally used in
 * cryptography, such as the generation of large prime numbers and computation
 * of the modular inverse.
 *
 * <h3>Slow Two's Complement Bitwise Operations</h3>
 * This API includes operations for bitwise operations in two's complement
 * representation. Two's complement is not the internal representation used by
 * this implementation, so such methods may be inefficient. Use {@link
 * java.util.BitSet} for high-performance bitwise operations on
 * arbitrarily-large sequences of bits.
 */
public class AndroidBigInt extends Number implements Serializable {

    /** sign field, used for serialization. */
    private int signum;

    /** absolute value field, used for serialization */
    private byte[] magnitude;

    public AndroidBigInt(int signum, byte[] magnitude) {
        this.signum = signum;
        this.magnitude = magnitude;
    }

    /**
     * Assigns all transient fields upon deserialization of a {@code AndroidBigInt}
     * instance.
     */
    private void readObject(ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        in.defaultReadObject();
//        BigInt bigInt = new BigInt();
//        bigInt.putBigEndian(magnitude, signum < 0);
//        setBigInt(bigInt);
    }

    /**
     * Prepares this {@code AndroidBigInt} for serialization, i.e. the
     * non-transient fields {@code signum} and {@code magnitude} are assigned.
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
//        BigInt bigInt = getBigInt();
//        signum = bigInt.sign();
//        magnitude = bigInt.bigEndianMagnitude();
        out.defaultWriteObject();
    }

    @Override
    public int intValue() {
        return 0;
    }

    @Override
    public long longValue() {
        return 0;
    }

    @Override
    public float floatValue() {
        return 0;
    }

    @Override
    public double doubleValue() {
        return 0;
    }
}
