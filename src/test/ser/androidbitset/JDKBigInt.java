package ser.androidbitset;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Random;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.io.StreamCorruptedException;
import sun.misc.Unsafe;

public class JDKBigInt extends Number implements Comparable<JDKBigInt> {
    final int signum;
    final int[] mag;
    /** @deprecated */
    @Deprecated
    private int bitCount;
    /** @deprecated */
    @Deprecated
    private int bitLength;
    /** @deprecated */
    @Deprecated
    private int lowestSetBit;
    /** @deprecated */
    @Deprecated
    private int firstNonzeroIntNum;
    static final long LONG_MASK = 4294967295L;
    private static long[] bitsPerDigit = new long[]{0L, 0L, 1024L, 1624L, 2048L, 2378L, 2648L, 2875L, 3072L, 3247L, 3402L, 3543L, 3672L, 3790L, 3899L, 4001L, 4096L, 4186L, 4271L, 4350L, 4426L, 4498L, 4567L, 4633L, 4696L, 4756L, 4814L, 4870L, 4923L, 4975L, 5025L, 5074L, 5120L, 5166L, 5210L, 5253L, 5295L};
    private static final int SMALL_PRIME_THRESHOLD = 95;
    private static final int DEFAULT_PRIME_CERTAINTY = 100;
    private static final JDKBigInt SMALL_PRIME_PRODUCT = valueOf(152125131763605L);
    private static volatile Random staticRandom;
    private static final int MAX_CONSTANT = 16;
    private static JDKBigInt[] posConst = new JDKBigInt[17];
    private static JDKBigInt[] negConst = new JDKBigInt[17];
    public static final JDKBigInt ZERO;
    public static final JDKBigInt ONE;
    private static final JDKBigInt TWO;
    public static final JDKBigInt TEN;
    static int[] bnExpModThreshTable;
    private static String[] zeros;
    private static int[] digitsPerLong;
    private static JDKBigInt[] longRadix;
    private static int[] digitsPerInt;
    private static int[] intRadix;
    private static final long serialVersionUID = -8287574255936472291L;
    private static final ObjectStreamField[] serialPersistentFields;
    private static final Unsafe unsafe;
    private static final long signumOffset;
    private static final long magOffset;

    public JDKBigInt(byte[] val) {
        if(val.length == 0) {
            throw new NumberFormatException("Zero length JDKBigInt");
        } else {
            if(val[0] < 0) {
                this.mag = makePositive((byte[])val);
                this.signum = -1;
            } else {
                this.mag = stripLeadingZeroBytes(val);
                this.signum = this.mag.length == 0?0:1;
            }

        }
    }

    private JDKBigInt(int[] val) {
        if(val.length == 0) {
            throw new NumberFormatException("Zero length JDKBigInt");
        } else {
            if(val[0] < 0) {
                this.mag = makePositive((int[])val);
                this.signum = -1;
            } else {
                this.mag = trustedStripLeadingZeroInts(val);
                this.signum = this.mag.length == 0?0:1;
            }

        }
    }

    public JDKBigInt(int signum, byte[] magnitude) {
        this.mag = stripLeadingZeroBytes(magnitude);
        if(signum >= -1 && signum <= 1) {
            if(this.mag.length == 0) {
                this.signum = 0;
            } else {
                if(signum == 0) {
                    throw new NumberFormatException("signum-magnitude mismatch");
                }

                this.signum = signum;
            }

        } else {
            throw new NumberFormatException("Invalid signum value");
        }
    }

    private JDKBigInt(int signum, int[] magnitude) {
        this.mag = stripLeadingZeroInts(magnitude);
        if(signum >= -1 && signum <= 1) {
            if(this.mag.length == 0) {
                this.signum = 0;
            } else {
                if(signum == 0) {
                    throw new NumberFormatException("signum-magnitude mismatch");
                }

                this.signum = signum;
            }

        } else {
            throw new NumberFormatException("Invalid signum value");
        }
    }

    public JDKBigInt(String val, int radix) {
        int cursor = 0;
        int len = val.length();
        if(radix >= 2 && radix <= 36) {
            if(len == 0) {
                throw new NumberFormatException("Zero length JDKBigInt");
            } else {
                byte sign = 1;
                int index1 = val.lastIndexOf(45);
                int index2 = val.lastIndexOf(43);
                if(index1 + index2 > -1) {
                    throw new NumberFormatException("Illegal embedded sign character");
                } else {
                    if(index1 == 0 || index2 == 0) {
                        cursor = 1;
                        if(len == 1) {
                            throw new NumberFormatException("Zero length JDKBigInt");
                        }
                    }

                    if(index1 == 0) {
                        sign = -1;
                    }

                    while(cursor < len && Character.digit(val.charAt(cursor), radix) == 0) {
                        ++cursor;
                    }

                    if(cursor == len) {
                        this.signum = 0;
                        this.mag = ZERO.mag;
                    } else {
                        int numDigits = len - cursor;
                        this.signum = sign;
                        int numBits = (int)(((long)numDigits * bitsPerDigit[radix] >>> 10) + 1L);
                        int numWords = numBits + 31 >>> 5;
                        int[] magnitude = new int[numWords];
                        int firstGroupLen = numDigits % digitsPerInt[radix];
                        if(firstGroupLen == 0) {
                            firstGroupLen = digitsPerInt[radix];
                        }

                        String group = val.substring(cursor, cursor += firstGroupLen);
                        magnitude[numWords - 1] = Integer.parseInt(group, radix);
                        if(magnitude[numWords - 1] < 0) {
                            throw new NumberFormatException("Illegal digit");
                        } else {
                            int superRadix = intRadix[radix];
                            boolean groupVal = false;

                            while(cursor < len) {
                                group = val.substring(cursor, cursor += digitsPerInt[radix]);
                                int var16 = Integer.parseInt(group, radix);
                                if(var16 < 0) {
                                    throw new NumberFormatException("Illegal digit");
                                }

                                destructiveMulAdd(magnitude, superRadix, var16);
                            }

                            this.mag = trustedStripLeadingZeroInts(magnitude);
                        }
                    }
                }
            }
        } else {
            throw new NumberFormatException("Radix out of range");
        }
    }

    JDKBigInt(char[] val) {
        int cursor = 0;
        int len = val.length;
        byte sign = 1;
        if(val[0] == 45) {
            if(len == 1) {
                throw new NumberFormatException("Zero length JDKBigInt");
            }

            sign = -1;
            cursor = 1;
        } else if(val[0] == 43) {
            if(len == 1) {
                throw new NumberFormatException("Zero length JDKBigInt");
            }

            cursor = 1;
        }

        while(cursor < len && Character.digit(val[cursor], 10) == 0) {
            ++cursor;
        }

        if(cursor == len) {
            this.signum = 0;
            this.mag = ZERO.mag;
        } else {
            int numDigits = len - cursor;
            this.signum = sign;
            int numWords;
            if(len < 10) {
                numWords = 1;
            } else {
                int magnitude = (int)(((long)numDigits * bitsPerDigit[10] >>> 10) + 1L);
                numWords = magnitude + 31 >>> 5;
            }

            int[] var10 = new int[numWords];
            int firstGroupLen = numDigits % digitsPerInt[10];
            if(firstGroupLen == 0) {
                firstGroupLen = digitsPerInt[10];
            }

            var10[numWords - 1] = this.parseInt(val, cursor, cursor += firstGroupLen);

            while(cursor < len) {
                int groupVal = this.parseInt(val, cursor, cursor += digitsPerInt[10]);
                destructiveMulAdd(var10, intRadix[10], groupVal);
            }

            this.mag = trustedStripLeadingZeroInts(var10);
        }
    }

    private int parseInt(char[] source, int start, int end) {
        int result = Character.digit(source[start++], 10);
        if(result == -1) {
            throw new NumberFormatException(new String(source));
        } else {
            for(int index = start; index < end; ++index) {
                int nextVal = Character.digit(source[index], 10);
                if(nextVal == -1) {
                    throw new NumberFormatException(new String(source));
                }

                result = 10 * result + nextVal;
            }

            return result;
        }
    }

    private static void destructiveMulAdd(int[] x, int y, int z) {
        long ylong = (long)y & 4294967295L;
        long zlong = (long)z & 4294967295L;
        int len = x.length;
        long product = 0L;
        long carry = 0L;

        for(int sum = len - 1; sum >= 0; --sum) {
            product = ylong * ((long)x[sum] & 4294967295L) + carry;
            x[sum] = (int)product;
            carry = product >>> 32;
        }

        long var15 = ((long)x[len - 1] & 4294967295L) + zlong;
        x[len - 1] = (int)var15;
        carry = var15 >>> 32;

        for(int i = len - 2; i >= 0; --i) {
            var15 = ((long)x[i] & 4294967295L) + carry;
            x[i] = (int)var15;
            carry = var15 >>> 32;
        }

    }

    public JDKBigInt(String val) {
        this((String)val, 10);
    }

    public JDKBigInt(int numBits, Random rnd) {
        this(1, (byte[])randomBits(numBits, rnd));
    }

    private static byte[] randomBits(int numBits, Random rnd) {
        if(numBits < 0) {
            throw new IllegalArgumentException("numBits must be non-negative");
        } else {
            int numBytes = (int)(((long)numBits + 7L) / 8L);
            byte[] randomBits = new byte[numBytes];
            if(numBytes > 0) {
                rnd.nextBytes(randomBits);
                int excessBits = 8 * numBytes - numBits;
                randomBits[0] = (byte)(randomBits[0] & (1 << 8 - excessBits) - 1);
            }

            return randomBits;
        }
    }

    private boolean passesLucasLehmer() {
        JDKBigInt thisPlusOne = this.add(ONE);

        int d;
        for(d = 5; jacobiSymbol(d, this) != -1; d = d < 0?Math.abs(d) + 2:-(d + 2)) {
            ;
        }

        JDKBigInt u = lucasLehmerSequence(d, thisPlusOne, this);
        return u.mod(this).equals(ZERO);
    }

    private static int jacobiSymbol(int p, JDKBigInt n) {
        if(p == 0) {
            return 0;
        } else {
            int j = 1;
            int u = n.mag[n.mag.length - 1];
            int t;
            if(p < 0) {
                p = -p;
                t = u & 7;
                if(t == 3 || t == 7) {
                    j = -j;
                }
            }

            while((p & 3) == 0) {
                p >>= 2;
            }

            if((p & 1) == 0) {
                p >>= 1;
                if(((u ^ u >> 1) & 2) != 0) {
                    j = -j;
                }
            }

            if(p == 1) {
                return j;
            } else {
                if((p & u & 2) != 0) {
                    j = -j;
                }

                for(u = n.mod(valueOf((long)p)).intValue(); u != 0; u %= t) {
                    while((u & 3) == 0) {
                        u >>= 2;
                    }

                    if((u & 1) == 0) {
                        u >>= 1;
                        if(((p ^ p >> 1) & 2) != 0) {
                            j = -j;
                        }
                    }

                    if(u == 1) {
                        return j;
                    }

                    assert u < p;

                    t = u;
                    u = p;
                    p = t;
                    if((u & t & 2) != 0) {
                        j = -j;
                    }
                }

                return 0;
            }
        }
    }

    private static JDKBigInt lucasLehmerSequence(int z, JDKBigInt k, JDKBigInt n) {
        JDKBigInt d = valueOf((long)z);
        JDKBigInt u = ONE;
        JDKBigInt v = ONE;

        for(int i = k.bitLength() - 2; i >= 0; --i) {
            JDKBigInt u2 = u.multiply(v).mod(n);
            JDKBigInt v2 = v.square().add(d.multiply(u.square())).mod(n);
            if(v2.testBit(0)) {
                v2 = v2.subtract(n);
            }

            v2 = v2.shiftRight(1);
            u = u2;
            v = v2;
            if(k.testBit(i)) {
                u2 = u2.add(v2).mod(n);
                if(u2.testBit(0)) {
                    u2 = u2.subtract(n);
                }

                u2 = u2.shiftRight(1);
                v2 = v2.add(d.multiply(u)).mod(n);
                if(v2.testBit(0)) {
                    v2 = v2.subtract(n);
                }

                v2 = v2.shiftRight(1);
                u = u2;
                v = v2;
            }
        }

        return u;
    }

    private static Random getSecureRandom() {
        if(staticRandom == null) {
            staticRandom = new SecureRandom();
        }

        return staticRandom;
    }

    JDKBigInt(int[] magnitude, int signum) {
        this.signum = magnitude.length == 0?0:signum;
        this.mag = magnitude;
    }

    private JDKBigInt(byte[] magnitude, int signum) {
        this.signum = magnitude.length == 0?0:signum;
        this.mag = stripLeadingZeroBytes(magnitude);
    }

    public static JDKBigInt valueOf(long val) {
        return val == 0L?ZERO:(val > 0L && val <= 16L?posConst[(int)val]:(val < 0L && val >= -16L?negConst[(int)(-val)]:new JDKBigInt(val)));
    }

    private JDKBigInt(long val) {
        if(val < 0L) {
            val = -val;
            this.signum = -1;
        } else {
            this.signum = 1;
        }

        int highWord = (int)(val >>> 32);
        if(highWord == 0) {
            this.mag = new int[1];
            this.mag[0] = (int)val;
        } else {
            this.mag = new int[2];
            this.mag[0] = highWord;
            this.mag[1] = (int)val;
        }

    }

    private static JDKBigInt valueOf(int[] val) {
        return val[0] > 0?new JDKBigInt(val, 1):new JDKBigInt(val);
    }

    public JDKBigInt add(JDKBigInt val) {
        if(val.signum == 0) {
            return this;
        } else if(this.signum == 0) {
            return val;
        } else if(val.signum == this.signum) {
            return new JDKBigInt(add(this.mag, val.mag), this.signum);
        } else {
            int cmp = this.compareMagnitude(val);
            if(cmp == 0) {
                return ZERO;
            } else {
                int[] resultMag = cmp > 0?subtract(this.mag, val.mag):subtract(val.mag, this.mag);
                resultMag = trustedStripLeadingZeroInts(resultMag);
                return new JDKBigInt(resultMag, cmp == this.signum?1:-1);
            }
        }
    }

    private static int[] add(int[] x, int[] y) {
        if(x.length < y.length) {
            int[] xIndex = x;
            x = y;
            y = xIndex;
        }

        int var9 = x.length;
        int yIndex = y.length;
        int[] result = new int[var9];

        long sum;
        for(sum = 0L; yIndex > 0; result[var9] = (int)sum) {
            --var9;
            long var10000 = (long)x[var9] & 4294967295L;
            --yIndex;
            sum = var10000 + ((long)y[yIndex] & 4294967295L) + (sum >>> 32);
        }

        boolean carry;
        for(carry = sum >>> 32 != 0L; var9 > 0 && carry; carry = (result[var9] = x[var9] + 1) == 0) {
            --var9;
        }

        while(var9 > 0) {
            --var9;
            result[var9] = x[var9];
        }

        if(carry) {
            int[] bigger = new int[result.length + 1];
            System.arraycopy(result, 0, bigger, 1, result.length);
            bigger[0] = 1;
            return bigger;
        } else {
            return result;
        }
    }

    public JDKBigInt subtract(JDKBigInt val) {
        if(val.signum == 0) {
            return this;
        } else if(this.signum == 0) {
            return val.negate();
        } else if(val.signum != this.signum) {
            return new JDKBigInt(add(this.mag, val.mag), this.signum);
        } else {
            int cmp = this.compareMagnitude(val);
            if(cmp == 0) {
                return ZERO;
            } else {
                int[] resultMag = cmp > 0?subtract(this.mag, val.mag):subtract(val.mag, this.mag);
                resultMag = trustedStripLeadingZeroInts(resultMag);
                return new JDKBigInt(resultMag, cmp == this.signum?1:-1);
            }
        }
    }

    private static int[] subtract(int[] big, int[] little) {
        int bigIndex = big.length;
        int[] result = new int[bigIndex];
        int littleIndex = little.length;

        long difference;
        for(difference = 0L; littleIndex > 0; result[bigIndex] = (int)difference) {
            --bigIndex;
            long var10000 = (long)big[bigIndex] & 4294967295L;
            --littleIndex;
            difference = var10000 - ((long)little[littleIndex] & 4294967295L) + (difference >> 32);
        }

        for(boolean borrow = difference >> 32 != 0L; bigIndex > 0 && borrow; borrow = (result[bigIndex] = big[bigIndex] - 1) == -1) {
            --bigIndex;
        }

        while(bigIndex > 0) {
            --bigIndex;
            result[bigIndex] = big[bigIndex];
        }

        return result;
    }

    public JDKBigInt multiply(JDKBigInt val) {
        if(val.signum != 0 && this.signum != 0) {
            int[] result = this.multiplyToLen(this.mag, this.mag.length, val.mag, val.mag.length, (int[])null);
            result = trustedStripLeadingZeroInts(result);
            return new JDKBigInt(result, this.signum == val.signum?1:-1);
        } else {
            return ZERO;
        }
    }

    JDKBigInt multiply(long v) {
        if(v != 0L && this.signum != 0) {
            if(v == -9223372036854775808L) {
                return this.multiply(valueOf(v));
            } else {
                int rsign = v > 0L?this.signum:-this.signum;
                if(v < 0L) {
                    v = -v;
                }

                long dh = v >>> 32;
                long dl = v & 4294967295L;
                int xlen = this.mag.length;
                int[] value = this.mag;
                int[] rmag = dh == 0L?new int[xlen + 1]:new int[xlen + 2];
                long carry = 0L;
                int rstart = rmag.length - 1;

                int i;
                long product;
                for(i = xlen - 1; i >= 0; --i) {
                    product = ((long)value[i] & 4294967295L) * dl + carry;
                    rmag[rstart--] = (int)product;
                    carry = product >>> 32;
                }

                rmag[rstart] = (int)carry;
                if(dh != 0L) {
                    carry = 0L;
                    rstart = rmag.length - 2;

                    for(i = xlen - 1; i >= 0; --i) {
                        product = ((long)value[i] & 4294967295L) * dh + ((long)rmag[rstart] & 4294967295L) + carry;
                        rmag[rstart--] = (int)product;
                        carry = product >>> 32;
                    }

                    rmag[0] = (int)carry;
                }

                if(carry == 0L) {
                    rmag = Arrays.copyOfRange(rmag, 1, rmag.length);
                }

                return new JDKBigInt(rmag, rsign);
            }
        } else {
            return ZERO;
        }
    }

    private int[] multiplyToLen(int[] x, int xlen, int[] y, int ylen, int[] z) {
        int xstart = xlen - 1;
        int ystart = ylen - 1;
        if(z == null || z.length < xlen + ylen) {
            z = new int[xlen + ylen];
        }

        long carry = 0L;
        int i = ystart;

        int j;
        for(j = ystart + 1 + xstart; i >= 0; --j) {
            long k = ((long)y[i] & 4294967295L) * ((long)x[xstart] & 4294967295L) + carry;
            z[j] = (int)k;
            carry = k >>> 32;
            --i;
        }

        z[xstart] = (int)carry;

        for(i = xstart - 1; i >= 0; --i) {
            carry = 0L;
            j = ystart;

            for(int var15 = ystart + 1 + i; j >= 0; --var15) {
                long product = ((long)y[j] & 4294967295L) * ((long)x[i] & 4294967295L) + ((long)z[var15] & 4294967295L) + carry;
                z[var15] = (int)product;
                carry = product >>> 32;
                --j;
            }

            z[i] = (int)carry;
        }

        return z;
    }

    private JDKBigInt square() {
        if(this.signum == 0) {
            return ZERO;
        } else {
            int[] z = squareToLen(this.mag, this.mag.length, (int[])null);
            return new JDKBigInt(trustedStripLeadingZeroInts(z), 1);
        }
    }

    private static final int[] squareToLen(int[] x, int len, int[] z) {
        int zlen = len << 1;
        if(z == null || z.length < zlen) {
            z = new int[zlen];
        }

        int lastProductLowWord = 0;
        int i = 0;

        int offset;
        for(offset = 0; i < len; ++i) {
            long t = (long)x[i] & 4294967295L;
            long product = t * t;
            z[offset++] = lastProductLowWord << 31 | (int)(product >>> 33);
            z[offset++] = (int)(product >>> 1);
            lastProductLowWord = (int)product;
        }

        i = len;

        for(offset = 1; i > 0; offset += 2) {
            int var11 = x[i - 1];
            var11 = mulAdd(z, x, offset, i - 1, var11);
            addOne(z, offset - 1, i, var11);
            --i;
        }

        primitiveLeftShift(z, zlen, 1);
        z[zlen - 1] |= x[len - 1] & 1;
        return z;
    }

    public JDKBigInt pow(int exponent) {
        if(exponent < 0) {
            throw new ArithmeticException("Negative exponent");
        } else if(this.signum == 0) {
            return exponent == 0?ONE:this;
        } else {
            int newSign = this.signum < 0 && (exponent & 1) == 1?-1:1;
            int[] baseToPow2 = this.mag;
            int[] result = new int[]{1};

            while(exponent != 0) {
                if((exponent & 1) == 1) {
                    result = this.multiplyToLen(result, result.length, baseToPow2, baseToPow2.length, (int[])null);
                    result = trustedStripLeadingZeroInts(result);
                }

                if((exponent >>>= 1) != 0) {
                    baseToPow2 = squareToLen(baseToPow2, baseToPow2.length, (int[])null);
                    baseToPow2 = trustedStripLeadingZeroInts(baseToPow2);
                }
            }

            return new JDKBigInt(result, newSign);
        }
    }

    static int bitLengthForInt(int n) {
        return 32 - Integer.numberOfLeadingZeros(n);
    }

    private static int[] leftShift(int[] a, int len, int n) {
        int nInts = n >>> 5;
        int nBits = n & 31;
        int bitsInHighWord = bitLengthForInt(a[0]);
        if(n <= 32 - bitsInHighWord) {
            primitiveLeftShift(a, len, nBits);
            return a;
        } else {
            int[] result;
            int i;
            if(nBits <= 32 - bitsInHighWord) {
                result = new int[nInts + len];

                for(i = 0; i < len; ++i) {
                    result[i] = a[i];
                }

                primitiveLeftShift(result, result.length, nBits);
                return result;
            } else {
                result = new int[nInts + len + 1];

                for(i = 0; i < len; ++i) {
                    result[i] = a[i];
                }

                primitiveRightShift(result, result.length, 32 - nBits);
                return result;
            }
        }
    }

    static void primitiveRightShift(int[] a, int len, int n) {
        int n2 = 32 - n;
        int i = len - 1;

        for(int c = a[i]; i > 0; --i) {
            int b = c;
            c = a[i - 1];
            a[i] = c << n2 | b >>> n;
        }

        a[0] >>>= n;
    }

    static void primitiveLeftShift(int[] a, int len, int n) {
        if(len != 0 && n != 0) {
            int n2 = 32 - n;
            int i = 0;
            int c = a[i];

            for(int m = i + len - 1; i < m; ++i) {
                int b = c;
                c = a[i + 1];
                a[i] = b << n | c >>> n2;
            }

            a[len - 1] <<= n;
        }
    }

    private static int bitLength(int[] val, int len) {
        return len == 0?0:(len - 1 << 5) + bitLengthForInt(val[0]);
    }

    public JDKBigInt abs() {
        return this.signum >= 0?this:this.negate();
    }

    public JDKBigInt negate() {
        return new JDKBigInt(this.mag, -this.signum);
    }

    public int signum() {
        return this.signum;
    }

    public JDKBigInt mod(JDKBigInt m) {
        return new JDKBigInt(0);
    }

    private static int[] montReduce(int[] n, int[] mod, int mlen, int inv) {
        int c = 0;
        int len = mlen;
        int offset = 0;

        do {
            int nEnd = n[n.length - 1 - offset];
            int carry = mulAdd(n, mod, offset, mlen, inv * nEnd);
            c += addOne(n, offset, mlen, carry);
            ++offset;
            --len;
        } while(len > 0);

        while(c > 0) {
            c += subN(n, mod, mlen);
        }

        while(intArrayCmpToLen(n, mod, mlen) >= 0) {
            subN(n, mod, mlen);
        }

        return n;
    }

    private static int intArrayCmpToLen(int[] arg1, int[] arg2, int len) {
        for(int i = 0; i < len; ++i) {
            long b1 = (long)arg1[i] & 4294967295L;
            long b2 = (long)arg2[i] & 4294967295L;
            if(b1 < b2) {
                return -1;
            }

            if(b1 > b2) {
                return 1;
            }
        }

        return 0;
    }

    private static int subN(int[] a, int[] b, int len) {
        long sum = 0L;

        while(true) {
            --len;
            if(len < 0) {
                return (int)(sum >> 32);
            }

            sum = ((long)a[len] & 4294967295L) - ((long)b[len] & 4294967295L) + (sum >> 32);
            a[len] = (int)sum;
        }
    }

    static int mulAdd(int[] out, int[] in, int offset, int len, int k) {
        long kLong = (long)k & 4294967295L;
        long carry = 0L;
        offset = out.length - offset - 1;

        for(int j = len - 1; j >= 0; --j) {
            long product = ((long)in[j] & 4294967295L) * kLong + ((long)out[offset] & 4294967295L) + carry;
            out[offset--] = (int)product;
            carry = product >>> 32;
        }

        return (int)carry;
    }

    static int addOne(int[] a, int offset, int mlen, int carry) {
        offset = a.length - 1 - mlen - offset;
        long t = ((long)a[offset] & 4294967295L) + ((long)carry & 4294967295L);
        a[offset] = (int)t;
        if(t >>> 32 == 0L) {
            return 0;
        } else {
            do {
                --mlen;
                if(mlen < 0) {
                    return 1;
                }

                --offset;
                if(offset < 0) {
                    return 1;
                }

                ++a[offset];
            } while(a[offset] == 0);

            return 0;
        }
    }

    private JDKBigInt modPow2(JDKBigInt exponent, int p) {
        JDKBigInt result = valueOf(1L);
        JDKBigInt baseToPow2 = this.mod2(p);
        int expOffset = 0;
        int limit = exponent.bitLength();
        if(this.testBit(0)) {
            limit = p - 1 < limit?p - 1:limit;
        }

        while(expOffset < limit) {
            if(exponent.testBit(expOffset)) {
                result = result.multiply(baseToPow2).mod2(p);
            }

            ++expOffset;
            if(expOffset < limit) {
                baseToPow2 = baseToPow2.square().mod2(p);
            }
        }

        return result;
    }

    private JDKBigInt mod2(int p) {
        if(this.bitLength() <= p) {
            return this;
        } else {
            int numInts = p + 31 >>> 5;
            int[] mag = new int[numInts];

            int excessBits;
            for(excessBits = 0; excessBits < numInts; ++excessBits) {
                mag[excessBits] = this.mag[excessBits + (this.mag.length - numInts)];
            }

            excessBits = (numInts << 5) - p;
            mag[0] = (int)((long)mag[0] & (1L << 32 - excessBits) - 1L);
            return mag[0] == 0?new JDKBigInt(1, mag):new JDKBigInt(mag, 1);
        }
    }

    public JDKBigInt shiftLeft(int n) {
        if(this.signum == 0) {
            return ZERO;
        } else if(n == 0) {
            return this;
        } else if(n < 0) {
            if(n == -2147483648) {
                throw new ArithmeticException("Shift distance of Integer.MIN_VALUE not supported.");
            } else {
                return this.shiftRight(-n);
            }
        } else {
            int nInts = n >>> 5;
            int nBits = n & 31;
            int magLen = this.mag.length;
            Object newMag = null;
            int i;
            int[] var10;
            if(nBits == 0) {
                var10 = new int[magLen + nInts];

                for(i = 0; i < magLen; ++i) {
                    var10[i] = this.mag[i];
                }
            } else {
                i = 0;
                int nBits2 = 32 - nBits;
                int highBits = this.mag[0] >>> nBits2;
                if(highBits != 0) {
                    var10 = new int[magLen + nInts + 1];
                    var10[i++] = highBits;
                } else {
                    var10 = new int[magLen + nInts];
                }

                int j;
                for(j = 0; j < magLen - 1; var10[i++] = this.mag[j++] << nBits | this.mag[j] >>> nBits2) {
                    ;
                }

                var10[i] = this.mag[j] << nBits;
            }

            return new JDKBigInt(var10, this.signum);
        }
    }

    public JDKBigInt shiftRight(int n) {
        if(n == 0) {
            return this;
        } else if(n < 0) {
            if(n == -2147483648) {
                throw new ArithmeticException("Shift distance of Integer.MIN_VALUE not supported.");
            } else {
                return this.shiftLeft(-n);
            }
        } else {
            int nInts = n >>> 5;
            int nBits = n & 31;
            int magLen = this.mag.length;
            Object newMag = null;
            if(nInts >= magLen) {
                return this.signum >= 0?ZERO:negConst[1];
            } else {
                int onesLost;
                int i;
                int j;
                int[] var10;
                if(nBits == 0) {
                    onesLost = magLen - nInts;
                    var10 = new int[onesLost];

                    for(i = 0; i < onesLost; ++i) {
                        var10[i] = this.mag[i];
                    }
                } else {
                    onesLost = 0;
                    i = this.mag[0] >>> nBits;
                    if(i != 0) {
                        var10 = new int[magLen - nInts];
                        var10[onesLost++] = i;
                    } else {
                        var10 = new int[magLen - nInts - 1];
                    }

                    j = 32 - nBits;

                    for(int j1 = 0; j1 < magLen - nInts - 1; var10[onesLost++] = this.mag[j1++] << j | this.mag[j1] >>> nBits) {
                        ;
                    }
                }

                if(this.signum < 0) {
                    boolean var11 = false;
                    i = magLen - 1;

                    for(j = magLen - nInts; i >= j && !var11; --i) {
                        var11 = this.mag[i] != 0;
                    }

                    if(!var11 && nBits != 0) {
                        var11 = this.mag[magLen - nInts - 1] << 32 - nBits != 0;
                    }

                    if(var11) {
                        var10 = this.javaIncrement(var10);
                    }
                }

                return new JDKBigInt(var10, this.signum);
            }
        }
    }

    int[] javaIncrement(int[] val) {
        int lastSum = 0;

        for(int i = val.length - 1; i >= 0 && lastSum == 0; --i) {
            lastSum = ++val[i];
        }

        if(lastSum == 0) {
            val = new int[val.length + 1];
            val[0] = 1;
        }

        return val;
    }

    public JDKBigInt and(JDKBigInt val) {
        int[] result = new int[Math.max(this.intLength(), val.intLength())];

        for(int i = 0; i < result.length; ++i) {
            result[i] = this.getInt(result.length - i - 1) & val.getInt(result.length - i - 1);
        }

        return valueOf(result);
    }

    public JDKBigInt or(JDKBigInt val) {
        int[] result = new int[Math.max(this.intLength(), val.intLength())];

        for(int i = 0; i < result.length; ++i) {
            result[i] = this.getInt(result.length - i - 1) | val.getInt(result.length - i - 1);
        }

        return valueOf(result);
    }

    public JDKBigInt xor(JDKBigInt val) {
        int[] result = new int[Math.max(this.intLength(), val.intLength())];

        for(int i = 0; i < result.length; ++i) {
            result[i] = this.getInt(result.length - i - 1) ^ val.getInt(result.length - i - 1);
        }

        return valueOf(result);
    }

    public JDKBigInt not() {
        int[] result = new int[this.intLength()];

        for(int i = 0; i < result.length; ++i) {
            result[i] = ~this.getInt(result.length - i - 1);
        }

        return valueOf(result);
    }

    public JDKBigInt andNot(JDKBigInt val) {
        int[] result = new int[Math.max(this.intLength(), val.intLength())];

        for(int i = 0; i < result.length; ++i) {
            result[i] = this.getInt(result.length - i - 1) & ~val.getInt(result.length - i - 1);
        }

        return valueOf(result);
    }

    public boolean testBit(int n) {
        if(n < 0) {
            throw new ArithmeticException("Negative bit address");
        } else {
            return (this.getInt(n >>> 5) & 1 << (n & 31)) != 0;
        }
    }

    public JDKBigInt setBit(int n) {
        if(n < 0) {
            throw new ArithmeticException("Negative bit address");
        } else {
            int intNum = n >>> 5;
            int[] result = new int[Math.max(this.intLength(), intNum + 2)];

            for(int i = 0; i < result.length; ++i) {
                result[result.length - i - 1] = this.getInt(i);
            }

            result[result.length - intNum - 1] |= 1 << (n & 31);
            return valueOf(result);
        }
    }

    public JDKBigInt clearBit(int n) {
        if(n < 0) {
            throw new ArithmeticException("Negative bit address");
        } else {
            int intNum = n >>> 5;
            int[] result = new int[Math.max(this.intLength(), (n + 1 >>> 5) + 1)];

            for(int i = 0; i < result.length; ++i) {
                result[result.length - i - 1] = this.getInt(i);
            }

            result[result.length - intNum - 1] &= ~(1 << (n & 31));
            return valueOf(result);
        }
    }

    public JDKBigInt flipBit(int n) {
        if(n < 0) {
            throw new ArithmeticException("Negative bit address");
        } else {
            int intNum = n >>> 5;
            int[] result = new int[Math.max(this.intLength(), intNum + 2)];

            for(int i = 0; i < result.length; ++i) {
                result[result.length - i - 1] = this.getInt(i);
            }

            result[result.length - intNum - 1] ^= 1 << (n & 31);
            return valueOf(result);
        }
    }

    public int getLowestSetBit() {
        int lsb = this.lowestSetBit - 2;
        if(lsb == -2) {
            byte var4 = 0;
            if(this.signum == 0) {
                lsb = var4 - 1;
            } else {
                int i;
                int b;
                for(i = 0; (b = this.getInt(i)) == 0; ++i) {
                    ;
                }

                lsb = var4 + (i << 5) + Integer.numberOfTrailingZeros(b);
            }

            this.lowestSetBit = lsb + 2;
        }

        return lsb;
    }

    public int bitLength() {
        int n = this.bitLength - 1;
        if(n == -1) {
            int[] m = this.mag;
            int len = m.length;
            if(len == 0) {
                n = 0;
            } else {
                int magBitLength = (len - 1 << 5) + bitLengthForInt(this.mag[0]);
                if(this.signum < 0) {
                    boolean pow2 = Integer.bitCount(this.mag[0]) == 1;

                    for(int i = 1; i < len && pow2; ++i) {
                        pow2 = this.mag[i] == 0;
                    }

                    n = pow2?magBitLength - 1:magBitLength;
                } else {
                    n = magBitLength;
                }
            }

            this.bitLength = n + 1;
        }

        return n;
    }

    public int bitCount() {
        int bc = this.bitCount - 1;
        if(bc == -1) {
            bc = 0;

            int magTrailingZeroCount;
            for(magTrailingZeroCount = 0; magTrailingZeroCount < this.mag.length; ++magTrailingZeroCount) {
                bc += Integer.bitCount(this.mag[magTrailingZeroCount]);
            }

            if(this.signum < 0) {
                magTrailingZeroCount = 0;

                int j;
                for(j = this.mag.length - 1; this.mag[j] == 0; --j) {
                    magTrailingZeroCount += 32;
                }

                magTrailingZeroCount += Integer.numberOfTrailingZeros(this.mag[j]);
                bc += magTrailingZeroCount - 1;
            }

            this.bitCount = bc + 1;
        }

        return bc;
    }

    public int compareTo(JDKBigInt val) {
        if(this.signum == val.signum) {
            switch(this.signum) {
                case -1:
                    return val.compareMagnitude(this);
                case 1:
                    return this.compareMagnitude(val);
                default:
                    return 0;
            }
        } else {
            return this.signum > val.signum?1:-1;
        }
    }

    final int compareMagnitude(JDKBigInt val) {
        int[] m1 = this.mag;
        int len1 = m1.length;
        int[] m2 = val.mag;
        int len2 = m2.length;
        if(len1 < len2) {
            return -1;
        } else if(len1 > len2) {
            return 1;
        } else {
            for(int i = 0; i < len1; ++i) {
                int a = m1[i];
                int b = m2[i];
                if(a != b) {
                    return ((long)a & 4294967295L) < ((long)b & 4294967295L)?-1:1;
                }
            }

            return 0;
        }
    }

    public boolean equals(Object x) {
        if(x == this) {
            return true;
        } else if(!(x instanceof JDKBigInt)) {
            return false;
        } else {
            JDKBigInt xInt = (JDKBigInt)x;
            if(xInt.signum != this.signum) {
                return false;
            } else {
                int[] m = this.mag;
                int len = m.length;
                int[] xm = xInt.mag;
                if(len != xm.length) {
                    return false;
                } else {
                    for(int i = 0; i < len; ++i) {
                        if(xm[i] != m[i]) {
                            return false;
                        }
                    }

                    return true;
                }
            }
        }
    }

    public JDKBigInt min(JDKBigInt val) {
        return this.compareTo((JDKBigInt)val) < 0?this:val;
    }

    public JDKBigInt max(JDKBigInt val) {
        return this.compareTo((JDKBigInt)val) > 0?this:val;
    }

    public int hashCode() {
        int hashCode = 0;

        for(int i = 0; i < this.mag.length; ++i) {
            hashCode = (int)((long)(31 * hashCode) + ((long)this.mag[i] & 4294967295L));
        }

        return hashCode * this.signum;
    }


    public byte[] toByteArray() {
        int byteLen = this.bitLength() / 8 + 1;
        byte[] byteArray = new byte[byteLen];
        int i = byteLen - 1;
        int bytesCopied = 4;
        int nextInt = 0;

        for(int intIndex = 0; i >= 0; --i) {
            if(bytesCopied == 4) {
                nextInt = this.getInt(intIndex++);
                bytesCopied = 1;
            } else {
                nextInt >>>= 8;
                ++bytesCopied;
            }

            byteArray[i] = (byte)nextInt;
        }

        return byteArray;
    }

    public int intValue() {
        boolean result = false;
        int result1 = this.getInt(0);
        return result1;
    }

    public long longValue() {
        long result = 0L;

        for(int i = 1; i >= 0; --i) {
            result = (result << 32) + ((long)this.getInt(i) & 4294967295L);
        }

        return result;
    }

    public float floatValue() {
        return Float.parseFloat(this.toString());
    }

    public double doubleValue() {
        return Double.parseDouble(this.toString());
    }

    private static int[] stripLeadingZeroInts(int[] val) {
        int vlen = val.length;

        int keep;
        for(keep = 0; keep < vlen && val[keep] == 0; ++keep) {
            ;
        }

        return Arrays.copyOfRange(val, keep, vlen);
    }

    private static int[] trustedStripLeadingZeroInts(int[] val) {
        int vlen = val.length;

        int keep;
        for(keep = 0; keep < vlen && val[keep] == 0; ++keep) {
            ;
        }

        return keep == 0?val:Arrays.copyOfRange(val, keep, vlen);
    }

    private static int[] stripLeadingZeroBytes(byte[] a) {
        int byteLength = a.length;

        int keep;
        for(keep = 0; keep < byteLength && a[keep] == 0; ++keep) {
            ;
        }

        int intLength = byteLength - keep + 3 >>> 2;
        int[] result = new int[intLength];
        int b = byteLength - 1;

        for(int i = intLength - 1; i >= 0; --i) {
            result[i] = a[b--] & 255;
            int bytesRemaining = b - keep + 1;
            int bytesToTransfer = Math.min(3, bytesRemaining);

            for(int j = 8; j <= bytesToTransfer << 3; j += 8) {
                result[i] |= (a[b--] & 255) << j;
            }
        }

        return result;
    }

    private static int[] makePositive(byte[] a) {
        int byteLength = a.length;

        int keep;
        for(keep = 0; keep < byteLength && a[keep] == -1; ++keep) {
            ;
        }

        int k;
        for(k = keep; k < byteLength && a[k] == 0; ++k) {
            ;
        }

        int extraByte = k == byteLength?1:0;
        int intLength = (byteLength - keep + extraByte + 3) / 4;
        int[] result = new int[intLength];
        int b = byteLength - 1;

        int i;
        for(i = intLength - 1; i >= 0; --i) {
            result[i] = a[b--] & 255;
            int numBytesToTransfer = Math.min(3, b - keep + 1);
            if(numBytesToTransfer < 0) {
                numBytesToTransfer = 0;
            }

            int mask;
            for(mask = 8; mask <= 8 * numBytesToTransfer; mask += 8) {
                result[i] |= (a[b--] & 255) << mask;
            }

            mask = -1 >>> 8 * (3 - numBytesToTransfer);
            result[i] = ~result[i] & mask;
        }

        for(i = result.length - 1; i >= 0; --i) {
            result[i] = (int)(((long)result[i] & 4294967295L) + 1L);
            if(result[i] != 0) {
                break;
            }
        }

        return result;
    }

    private static int[] makePositive(int[] a) {
        int keep;
        for(keep = 0; keep < a.length && a[keep] == -1; ++keep) {
            ;
        }

        int j;
        for(j = keep; j < a.length && a[j] == 0; ++j) {
            ;
        }

        int extraInt = j == a.length?1:0;
        int[] result = new int[a.length - keep + extraInt];

        int i;
        for(i = keep; i < a.length; ++i) {
            result[i - keep + extraInt] = ~a[i];
        }

        for(i = result.length - 1; ++result[i] == 0; --i) {
            ;
        }

        return result;
    }

    private int intLength() {
        return (this.bitLength() >>> 5) + 1;
    }

    private int signBit() {
        return this.signum < 0?1:0;
    }

    private int signInt() {
        return this.signum < 0?-1:0;
    }

    private int getInt(int n) {
        if(n < 0) {
            return 0;
        } else if(n >= this.mag.length) {
            return this.signInt();
        } else {
            int magInt = this.mag[this.mag.length - n - 1];
            return this.signum >= 0?magInt:(n <= this.firstNonzeroIntNum()?-magInt:~magInt);
        }
    }

    private int firstNonzeroIntNum() {
        int fn = this.firstNonzeroIntNum - 2;
        if(fn == -2) {
            boolean var4 = false;
            int mlen = this.mag.length;

            int i;
            for(i = mlen - 1; i >= 0 && this.mag[i] == 0; --i) {
                ;
            }

            fn = mlen - i - 1;
            this.firstNonzeroIntNum = fn + 2;
        }

        return fn;
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        ObjectInputStream.GetField fields = s.readFields();
        int sign = fields.get("signum", -2);
        byte[] magnitude = (byte[])((byte[])fields.get("magnitude", (Object)null));
        String message;
        if(sign >= -1 && sign <= 1) {
            if(magnitude.length == 0 != (sign == 0)) {
                message = "JDKBigInt: signum-magnitude mismatch";
                if(fields.defaulted("magnitude")) {
                    message = "JDKBigInt: Magnitude not present in stream";
                }

                throw new StreamCorruptedException(message);
            } else {
                unsafe.putIntVolatile(this, signumOffset, sign);
                unsafe.putObjectVolatile(this, magOffset, stripLeadingZeroBytes(magnitude));
            }
        } else {
            message = "JDKBigInt: Invalid signum value";
            if(fields.defaulted("signum")) {
                message = "JDKBigInt: Signum not present in stream";
            }

            throw new StreamCorruptedException(message);
        }
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        ObjectOutputStream.PutField fields = s.putFields();
        fields.put("signum", this.signum);
        fields.put("magnitude", this.magSerializedForm());
        fields.put("bitCount", -1);
        fields.put("bitLength", -1);
        fields.put("lowestSetBit", -2);
        fields.put("firstNonzeroByteNum", -2);
        s.writeFields();
    }

    private byte[] magSerializedForm() {
        int len = this.mag.length;
        int bitLen = len == 0?0:(len - 1 << 5) + bitLengthForInt(this.mag[0]);
        int byteLen = bitLen + 7 >>> 3;
        byte[] result = new byte[byteLen];
        int i = byteLen - 1;
        int bytesCopied = 4;
        int intIndex = len - 1;

        for(int nextInt = 0; i >= 0; --i) {
            if(bytesCopied == 4) {
                nextInt = this.mag[intIndex--];
                bytesCopied = 1;
            } else {
                nextInt >>>= 8;
                ++bytesCopied;
            }

            result[i] = (byte)nextInt;
        }

        return result;
    }

    static {
        int ex;
        for(ex = 1; ex <= 16; ++ex) {
            int[] magnitude = new int[]{ex};
            posConst[ex] = new JDKBigInt(magnitude, 1);
            negConst[ex] = new JDKBigInt(magnitude, -1);
        }

        ZERO = new JDKBigInt(new int[0], 0);
        ONE = valueOf(1L);
        TWO = valueOf(2L);
        TEN = valueOf(10L);
        bnExpModThreshTable = new int[]{7, 25, 81, 241, 673, 1793, 2147483647};
        zeros = new String[64];
        zeros[63] = "000000000000000000000000000000000000000000000000000000000000000";

        for(ex = 0; ex < 63; ++ex) {
            zeros[ex] = zeros[63].substring(0, ex);
        }

        digitsPerLong = new int[]{0, 0, 62, 39, 31, 27, 24, 22, 20, 19, 18, 18, 17, 17, 16, 16, 15, 15, 15, 14, 14, 14, 14, 13, 13, 13, 13, 13, 13, 12, 12, 12, 12, 12, 12, 12, 12};
        longRadix = new JDKBigInt[]{null, null, valueOf(4611686018427387904L), valueOf(4052555153018976267L), valueOf(4611686018427387904L), valueOf(7450580596923828125L), valueOf(4738381338321616896L), valueOf(3909821048582988049L), valueOf(1152921504606846976L), valueOf(1350851717672992089L), valueOf(1000000000000000000L), valueOf(5559917313492231481L), valueOf(2218611106740436992L), valueOf(8650415919381337933L), valueOf(2177953337809371136L), valueOf(6568408355712890625L), valueOf(1152921504606846976L), valueOf(2862423051509815793L), valueOf(6746640616477458432L), valueOf(799006685782884121L), valueOf(1638400000000000000L), valueOf(3243919932521508681L), valueOf(6221821273427820544L), valueOf(504036361936467383L), valueOf(876488338465357824L), valueOf(1490116119384765625L), valueOf(2481152873203736576L), valueOf(4052555153018976267L), valueOf(6502111422497947648L), valueOf(353814783205469041L), valueOf(531441000000000000L), valueOf(787662783788549761L), valueOf(1152921504606846976L), valueOf(1667889514952984961L), valueOf(2386420683693101056L), valueOf(3379220508056640625L), valueOf(4738381338321616896L)};
        digitsPerInt = new int[]{0, 0, 30, 19, 15, 13, 11, 11, 10, 9, 9, 8, 8, 8, 8, 7, 7, 7, 7, 7, 7, 7, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 5};
        intRadix = new int[]{0, 0, 1073741824, 1162261467, 1073741824, 1220703125, 362797056, 1977326743, 1073741824, 387420489, 1000000000, 214358881, 429981696, 815730721, 1475789056, 170859375, 268435456, 410338673, 612220032, 893871739, 1280000000, 1801088541, 113379904, 148035889, 191102976, 244140625, 308915776, 387420489, 481890304, 594823321, 729000000, 887503681, 1073741824, 1291467969, 1544804416, 1838265625, 60466176};
        serialPersistentFields = new ObjectStreamField[]{new ObjectStreamField("signum", Integer.TYPE), new ObjectStreamField("magnitude", byte[].class), new ObjectStreamField("bitCount", Integer.TYPE), new ObjectStreamField("bitLength", Integer.TYPE), new ObjectStreamField("firstNonzeroByteNum", Integer.TYPE), new ObjectStreamField("lowestSetBit", Integer.TYPE)};
        unsafe = null;//Unsafe.getUnsafe();
        signumOffset = 0;
        magOffset = 0;

//        try {
//            signumOffset = unsafe.objectFieldOffset(JDKBigInt.class.getDeclaredField("signum"));
//            magOffset = unsafe.objectFieldOffset(JDKBigInt.class.getDeclaredField("mag"));
//        } catch (Exception var2) {
//            throw new Error(var2);
//        }
    }
}
