package ser.androidbitset;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.LongBuffer;
import java.util.Arrays;

public class JDKBitSet implements Cloneable, Serializable {
    private static final int ADDRESS_BITS_PER_WORD = 6;
    private static final int BITS_PER_WORD = 64;
    private static final int BIT_INDEX_MASK = 63;
    private static final long WORD_MASK = -1L;
    private static final ObjectStreamField[] serialPersistentFields = new ObjectStreamField[]{new ObjectStreamField("bits", long[].class)};
    private long[] words;
    private transient int wordsInUse = 0;
    private transient boolean sizeIsSticky = false;
    private static final long serialVersionUID = 7997698588986878753L;

    private static int wordIndex(int bitIndex) {
        return bitIndex >> 6;
    }

    private void checkInvariants() {
        assert this.wordsInUse == 0 || this.words[this.wordsInUse - 1] != 0L;

        assert this.wordsInUse >= 0 && this.wordsInUse <= this.words.length;

        assert this.wordsInUse == this.words.length || this.words[this.wordsInUse] == 0L;

    }

    private void recalculateWordsInUse() {
        int i;
        for(i = this.wordsInUse - 1; i >= 0 && this.words[i] == 0L; --i) {
            ;
        }

        this.wordsInUse = i + 1;
    }

    public JDKBitSet() {
        this.initWords(64);
        this.sizeIsSticky = false;
    }

    public JDKBitSet(int nbits) {
        if(nbits < 0) {
            throw new NegativeArraySizeException("nbits < 0: " + nbits);
        } else {
            this.initWords(nbits);
            this.sizeIsSticky = true;
        }
    }

    private void initWords(int nbits) {
        this.words = new long[wordIndex(nbits - 1) + 1];
    }

    private JDKBitSet(long[] words) {
        this.words = words;
        this.wordsInUse = words.length;
        this.checkInvariants();
    }

    public static JDKBitSet valueOf(long[] longs) {
        int n;
        for(n = longs.length; n > 0 && longs[n - 1] == 0L; --n) {
            ;
        }

        return new JDKBitSet(Arrays.copyOf(longs, n));
    }

    public static JDKBitSet valueOf(LongBuffer lb) {
        lb = lb.slice();

        int n;
        for(n = lb.remaining(); n > 0 && lb.get(n - 1) == 0L; --n) {
            ;
        }

        long[] words = new long[n];
        lb.get(words);
        return new JDKBitSet(words);
    }

    public static JDKBitSet valueOf(byte[] bytes) {
        return valueOf((ByteBuffer)ByteBuffer.wrap(bytes));
    }

    public static JDKBitSet valueOf(ByteBuffer bb) {
        bb = bb.slice().order(ByteOrder.LITTLE_ENDIAN);

        int n;
        for(n = bb.remaining(); n > 0 && bb.get(n - 1) == 0; --n) {
            ;
        }

        long[] words = new long[(n + 7) / 8];
        bb.limit(n);

        int i;
        for(i = 0; bb.remaining() >= 8; words[i++] = bb.getLong()) {
            ;
        }

        int remaining = bb.remaining();

        for(int j = 0; j < remaining; ++j) {
            words[i] |= ((long)bb.get() & 255L) << 8 * j;
        }

        return new JDKBitSet(words);
    }

    public byte[] toByteArray() {
        int n = this.wordsInUse;
        if(n == 0) {
            return new byte[0];
        } else {
            int len = 8 * (n - 1);

            for(long bytes = this.words[n - 1]; bytes != 0L; bytes >>>= 8) {
                ++len;
            }

            byte[] var7 = new byte[len];
            ByteBuffer bb = ByteBuffer.wrap(var7).order(ByteOrder.LITTLE_ENDIAN);

            for(int x = 0; x < n - 1; ++x) {
                bb.putLong(this.words[x]);
            }

            for(long var8 = this.words[n - 1]; var8 != 0L; var8 >>>= 8) {
                bb.put((byte)((int)(var8 & 255L)));
            }

            return var7;
        }
    }

    public long[] toLongArray() {
        return Arrays.copyOf(this.words, this.wordsInUse);
    }

    private void ensureCapacity(int wordsRequired) {
        if(this.words.length < wordsRequired) {
            int request = Math.max(2 * this.words.length, wordsRequired);
            this.words = Arrays.copyOf(this.words, request);
            this.sizeIsSticky = false;
        }

    }

    private void expandTo(int wordIndex) {
        int wordsRequired = wordIndex + 1;
        if(this.wordsInUse < wordsRequired) {
            this.ensureCapacity(wordsRequired);
            this.wordsInUse = wordsRequired;
        }

    }

    private static void checkRange(int fromIndex, int toIndex) {
        if(fromIndex < 0) {
            throw new IndexOutOfBoundsException("fromIndex < 0: " + fromIndex);
        } else if(toIndex < 0) {
            throw new IndexOutOfBoundsException("toIndex < 0: " + toIndex);
        } else if(fromIndex > toIndex) {
            throw new IndexOutOfBoundsException("fromIndex: " + fromIndex + " > toIndex: " + toIndex);
        }
    }

    public void flip(int bitIndex) {
        if(bitIndex < 0) {
            throw new IndexOutOfBoundsException("bitIndex < 0: " + bitIndex);
        } else {
            int wordIndex = wordIndex(bitIndex);
            this.expandTo(wordIndex);
            this.words[wordIndex] ^= 1L << bitIndex;
            this.recalculateWordsInUse();
            this.checkInvariants();
        }
    }

    public void flip(int fromIndex, int toIndex) {
        checkRange(fromIndex, toIndex);
        if(fromIndex != toIndex) {
            int startWordIndex = wordIndex(fromIndex);
            int endWordIndex = wordIndex(toIndex - 1);
            this.expandTo(endWordIndex);
            long firstWordMask = -1L << fromIndex;
            long lastWordMask = -1L >>> -toIndex;
            if(startWordIndex == endWordIndex) {
                this.words[startWordIndex] ^= firstWordMask & lastWordMask;
            } else {
                this.words[startWordIndex] ^= firstWordMask;

                for(int i = startWordIndex + 1; i < endWordIndex; ++i) {
                    this.words[i] = ~this.words[i];
                }

                this.words[endWordIndex] ^= lastWordMask;
            }

            this.recalculateWordsInUse();
            this.checkInvariants();
        }
    }

    public void set(int bitIndex) {
        if(bitIndex < 0) {
            throw new IndexOutOfBoundsException("bitIndex < 0: " + bitIndex);
        } else {
            int wordIndex = wordIndex(bitIndex);
            this.expandTo(wordIndex);
            this.words[wordIndex] |= 1L << bitIndex;
            this.checkInvariants();
        }
    }

    public void set(int bitIndex, boolean value) {
        if(value) {
            this.set(bitIndex);
        } else {
            this.clear(bitIndex);
        }

    }

    public void set(int fromIndex, int toIndex) {
        checkRange(fromIndex, toIndex);
        if(fromIndex != toIndex) {
            int startWordIndex = wordIndex(fromIndex);
            int endWordIndex = wordIndex(toIndex - 1);
            this.expandTo(endWordIndex);
            long firstWordMask = -1L << fromIndex;
            long lastWordMask = -1L >>> -toIndex;
            if(startWordIndex == endWordIndex) {
                this.words[startWordIndex] |= firstWordMask & lastWordMask;
            } else {
                this.words[startWordIndex] |= firstWordMask;

                for(int i = startWordIndex + 1; i < endWordIndex; ++i) {
                    this.words[i] = -1L;
                }

                this.words[endWordIndex] |= lastWordMask;
            }

            this.checkInvariants();
        }
    }

    public void set(int fromIndex, int toIndex, boolean value) {
        if(value) {
            this.set(fromIndex, toIndex);
        } else {
            this.clear(fromIndex, toIndex);
        }

    }

    public void clear(int bitIndex) {
        if(bitIndex < 0) {
            throw new IndexOutOfBoundsException("bitIndex < 0: " + bitIndex);
        } else {
            int wordIndex = wordIndex(bitIndex);
            if(wordIndex < this.wordsInUse) {
                this.words[wordIndex] &= ~(1L << bitIndex);
                this.recalculateWordsInUse();
                this.checkInvariants();
            }
        }
    }

    public void clear(int fromIndex, int toIndex) {
        checkRange(fromIndex, toIndex);
        if(fromIndex != toIndex) {
            int startWordIndex = wordIndex(fromIndex);
            if(startWordIndex < this.wordsInUse) {
                int endWordIndex = wordIndex(toIndex - 1);
                if(endWordIndex >= this.wordsInUse) {
                    toIndex = this.length();
                    endWordIndex = this.wordsInUse - 1;
                }

                long firstWordMask = -1L << fromIndex;
                long lastWordMask = -1L >>> -toIndex;
                if(startWordIndex == endWordIndex) {
                    this.words[startWordIndex] &= ~(firstWordMask & lastWordMask);
                } else {
                    this.words[startWordIndex] &= ~firstWordMask;

                    for(int i = startWordIndex + 1; i < endWordIndex; ++i) {
                        this.words[i] = 0L;
                    }

                    this.words[endWordIndex] &= ~lastWordMask;
                }

                this.recalculateWordsInUse();
                this.checkInvariants();
            }
        }
    }

    public void clear() {
        while(this.wordsInUse > 0) {
            this.words[--this.wordsInUse] = 0L;
        }

    }

    public boolean get(int bitIndex) {
        if(bitIndex < 0) {
            throw new IndexOutOfBoundsException("bitIndex < 0: " + bitIndex);
        } else {
            this.checkInvariants();
            int wordIndex = wordIndex(bitIndex);
            return wordIndex < this.wordsInUse && (this.words[wordIndex] & 1L << bitIndex) != 0L;
        }
    }

    public JDKBitSet get(int fromIndex, int toIndex) {
        checkRange(fromIndex, toIndex);
        this.checkInvariants();
        int len = this.length();
        if(len > fromIndex && fromIndex != toIndex) {
            if(toIndex > len) {
                toIndex = len;
            }

            JDKBitSet result = new JDKBitSet(toIndex - fromIndex);
            int targetWords = wordIndex(toIndex - fromIndex - 1) + 1;
            int sourceIndex = wordIndex(fromIndex);
            boolean wordAligned = (fromIndex & 63) == 0;

            for(int lastWordMask = 0; lastWordMask < targetWords - 1; ++sourceIndex) {
                result.words[lastWordMask] = wordAligned?this.words[sourceIndex]:this.words[sourceIndex] >>> fromIndex | this.words[sourceIndex + 1] << -fromIndex;
                ++lastWordMask;
            }

            long var10 = -1L >>> -toIndex;
            result.words[targetWords - 1] = (toIndex - 1 & 63) < (fromIndex & 63)?this.words[sourceIndex] >>> fromIndex | (this.words[sourceIndex + 1] & var10) << -fromIndex:(this.words[sourceIndex] & var10) >>> fromIndex;
            result.wordsInUse = targetWords;
            result.recalculateWordsInUse();
            result.checkInvariants();
            return result;
        } else {
            return new JDKBitSet(0);
        }
    }

    public int nextSetBit(int fromIndex) {
        if(fromIndex < 0) {
            throw new IndexOutOfBoundsException("fromIndex < 0: " + fromIndex);
        } else {
            this.checkInvariants();
            int u = wordIndex(fromIndex);
            if(u >= this.wordsInUse) {
                return -1;
            } else {
                long word;
                for(word = this.words[u] & -1L << fromIndex; word == 0L; word = this.words[u]) {
                    ++u;
                    if(u == this.wordsInUse) {
                        return -1;
                    }
                }

                return u * 64 + Long.numberOfTrailingZeros(word);
            }
        }
    }

    public int nextClearBit(int fromIndex) {
        if(fromIndex < 0) {
            throw new IndexOutOfBoundsException("fromIndex < 0: " + fromIndex);
        } else {
            this.checkInvariants();
            int u = wordIndex(fromIndex);
            if(u >= this.wordsInUse) {
                return fromIndex;
            } else {
                long word;
                for(word = ~this.words[u] & -1L << fromIndex; word == 0L; word = ~this.words[u]) {
                    ++u;
                    if(u == this.wordsInUse) {
                        return this.wordsInUse * 64;
                    }
                }

                return u * 64 + Long.numberOfTrailingZeros(word);
            }
        }
    }

    public int previousSetBit(int fromIndex) {
        if(fromIndex < 0) {
            if(fromIndex == -1) {
                return -1;
            } else {
                throw new IndexOutOfBoundsException("fromIndex < -1: " + fromIndex);
            }
        } else {
            this.checkInvariants();
            int u = wordIndex(fromIndex);
            if(u >= this.wordsInUse) {
                return this.length() - 1;
            } else {
                long word;
                for(word = this.words[u] & -1L >>> -(fromIndex + 1); word == 0L; word = this.words[u]) {
                    if(u-- == 0) {
                        return -1;
                    }
                }

                return (u + 1) * 64 - 1 - Long.numberOfLeadingZeros(word);
            }
        }
    }

    public int previousClearBit(int fromIndex) {
        if(fromIndex < 0) {
            if(fromIndex == -1) {
                return -1;
            } else {
                throw new IndexOutOfBoundsException("fromIndex < -1: " + fromIndex);
            }
        } else {
            this.checkInvariants();
            int u = wordIndex(fromIndex);
            if(u >= this.wordsInUse) {
                return fromIndex;
            } else {
                long word;
                for(word = ~this.words[u] & -1L >>> -(fromIndex + 1); word == 0L; word = ~this.words[u]) {
                    if(u-- == 0) {
                        return -1;
                    }
                }

                return (u + 1) * 64 - 1 - Long.numberOfLeadingZeros(word);
            }
        }
    }

    public int length() {
        return this.wordsInUse == 0?0:64 * (this.wordsInUse - 1) + (64 - Long.numberOfLeadingZeros(this.words[this.wordsInUse - 1]));
    }

    public boolean isEmpty() {
        return this.wordsInUse == 0;
    }

    public boolean intersects(JDKBitSet set) {
        for(int i = Math.min(this.wordsInUse, set.wordsInUse) - 1; i >= 0; --i) {
            if((this.words[i] & set.words[i]) != 0L) {
                return true;
            }
        }

        return false;
    }

    public int cardinality() {
        int sum = 0;

        for(int i = 0; i < this.wordsInUse; ++i) {
            sum += Long.bitCount(this.words[i]);
        }

        return sum;
    }

    public void and(JDKBitSet set) {
        if(this != set) {
            while(this.wordsInUse > set.wordsInUse) {
                this.words[--this.wordsInUse] = 0L;
            }

            for(int i = 0; i < this.wordsInUse; ++i) {
                this.words[i] &= set.words[i];
            }

            this.recalculateWordsInUse();
            this.checkInvariants();
        }
    }

    public void or(JDKBitSet set) {
        if(this != set) {
            int wordsInCommon = Math.min(this.wordsInUse, set.wordsInUse);
            if(this.wordsInUse < set.wordsInUse) {
                this.ensureCapacity(set.wordsInUse);
                this.wordsInUse = set.wordsInUse;
            }

            for(int i = 0; i < wordsInCommon; ++i) {
                this.words[i] |= set.words[i];
            }

            if(wordsInCommon < set.wordsInUse) {
                System.arraycopy(set.words, wordsInCommon, this.words, wordsInCommon, this.wordsInUse - wordsInCommon);
            }

            this.checkInvariants();
        }
    }

    public void xor(JDKBitSet set) {
        int wordsInCommon = Math.min(this.wordsInUse, set.wordsInUse);
        if(this.wordsInUse < set.wordsInUse) {
            this.ensureCapacity(set.wordsInUse);
            this.wordsInUse = set.wordsInUse;
        }

        for(int i = 0; i < wordsInCommon; ++i) {
            this.words[i] ^= set.words[i];
        }

        if(wordsInCommon < set.wordsInUse) {
            System.arraycopy(set.words, wordsInCommon, this.words, wordsInCommon, set.wordsInUse - wordsInCommon);
        }

        this.recalculateWordsInUse();
        this.checkInvariants();
    }

    public void andNot(JDKBitSet set) {
        for(int i = Math.min(this.wordsInUse, set.wordsInUse) - 1; i >= 0; --i) {
            this.words[i] &= ~set.words[i];
        }

        this.recalculateWordsInUse();
        this.checkInvariants();
    }

    public int hashCode() {
        long h = 1234L;
        int i = this.wordsInUse;

        while(true) {
            --i;
            if(i < 0) {
                return (int)(h >> 32 ^ h);
            }

            h ^= this.words[i] * (long)(i + 1);
        }
    }

    public int size() {
        return this.words.length * 64;
    }

    public boolean equals(Object obj) {
        if(!(obj instanceof JDKBitSet)) {
            return false;
        } else if(this == obj) {
            return true;
        } else {
            JDKBitSet set = (JDKBitSet)obj;
            this.checkInvariants();
            set.checkInvariants();
            if(this.wordsInUse != set.wordsInUse) {
                return false;
            } else {
                for(int i = 0; i < this.wordsInUse; ++i) {
                    if(this.words[i] != set.words[i]) {
                        return false;
                    }
                }

                return true;
            }
        }
    }

    public Object clone() {
        if(!this.sizeIsSticky) {
            this.trimToSize();
        }

        try {
            JDKBitSet e = (JDKBitSet)super.clone();
            e.words = (long[])this.words.clone();
            e.checkInvariants();
            return e;
        } catch (CloneNotSupportedException var2) {
            throw new InternalError();
        }
    }

    private void trimToSize() {
        if(this.wordsInUse != this.words.length) {
            this.words = Arrays.copyOf(this.words, this.wordsInUse);
            this.checkInvariants();
        }

    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        this.checkInvariants();
        if(!this.sizeIsSticky) {
            this.trimToSize();
        }

        ObjectOutputStream.PutField fields = s.putFields();
        fields.put("bits", this.words);
        s.writeFields();
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        ObjectInputStream.GetField fields = s.readFields();
        this.words = (long[])((long[])fields.get("bits", (Object)null));
        this.wordsInUse = this.words.length;
        this.recalculateWordsInUse();
        this.sizeIsSticky = this.words.length > 0 && this.words[this.words.length - 1] == 0L;
        this.checkInvariants();
    }

    public String toString() {
        this.checkInvariants();
        int numBits = this.wordsInUse > 128?this.cardinality():this.wordsInUse * 64;
        StringBuilder b = new StringBuilder(6 * numBits + 2);
        b.append('{');
        int i = this.nextSetBit(0);
        if(i != -1) {
            b.append(i);

            for(i = this.nextSetBit(i + 1); i >= 0; i = this.nextSetBit(i + 1)) {
                int endOfRun = this.nextClearBit(i);

                do {
                    b.append(", ").append(i);
                    ++i;
                } while(i < endOfRun);
            }
        }

        b.append('}');
        return b.toString();
    }
}
