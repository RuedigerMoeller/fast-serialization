package de.ruedigermoeller.serialization.testclasses.json;

/**
 * Created by ruedi on 17.05.14.
 */
public class BasicJSon {

    boolean aBoolean = true;

    byte aByte0 = -13;
    byte aByte1 = Byte.MIN_VALUE;
    byte aByte2 = Byte.MAX_VALUE;

    short aShort0 = -13345;
    short aShort1 = Short.MIN_VALUE;
    short aShort2 = Short.MAX_VALUE;

    char aChar0 = 35345;
    char aChar1 = Character.MIN_VALUE;
    char aChar2 = Character.MAX_VALUE;

    int aInt0 = -35345;
    int aInt1 = Integer.MIN_VALUE;
    int aInt2 = Integer.MAX_VALUE;

    float aFloat0 = -35435345.002f;
    float aFloat1 = Float.MIN_VALUE;
    float aFloat2 = Float.MAX_VALUE;

    double aDouble0 = -35435345.002d;
    double aDouble1 = Double.MIN_VALUE;
    double aDouble2 = Double.MAX_VALUE;


    Boolean _aBoolean = false;
    Boolean ugly[][] = {{true,false},null,{true,false,null}};

    Byte _aByte0 = -13;
    Object _aByte1 = Byte.MIN_VALUE;
    Byte _aByte2 = Byte.MAX_VALUE;
    Byte aByteA2[] = { Byte.MAX_VALUE  };

    Short _aShort0 = -1334;
    Object _aShort1 = Short.MIN_VALUE;
    Short _aShort2 = Short.MAX_VALUE;
    Short _aShort2a[] = {0,null,Short.MAX_VALUE};

    Character _aChar0 = 35345;
    Object _aChar1 = Character.MIN_VALUE;
    Character _aChar2 = Character.MAX_VALUE;
    Character _aChar2a[] = {null,Character.MAX_VALUE};


    Integer _aInt0 = 35345;
    Object _aInt1 = Integer.MIN_VALUE;
    Integer _aInt2 = Integer.MAX_VALUE;
    Integer _aInt2a[] = {Integer.MAX_VALUE};

    Float _aFloat0 = 123.66f;
    Object _aFloat1 = Float.MIN_VALUE;
    Float _aFloat2 = Float.MAX_VALUE;
    Float _aFloat2a[] = {-8.7f,Float.MAX_VALUE};

    Double _aDouble0 = 123.66d;
    Object _aDouble1 = Double.MIN_VALUE;
    Double _aDouble2 = Double.MAX_VALUE;
    Double _aDouble2a[] = {-88.0,Double.MAX_VALUE};

}
