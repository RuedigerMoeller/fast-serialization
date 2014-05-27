package de.ruedigermoeller.serialization.testclasses.basicstuff;

import de.ruedigermoeller.serialization.FSTConfiguration;
import de.ruedigermoeller.serialization.annotations.*;
import de.ruedigermoeller.serialization.testclasses.HasDescription;

import javax.security.auth.Subject;
import javax.swing.text.html.StyleSheet;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: ruedi
 * Date: 12.11.12
 * Time: 03:13
 * To change this template use File | Settings | File Templates.
 */
public class Primitives extends PrivatePrimitive implements Serializable, HasDescription {

//    String hidden (**)

    @Override
    public String getDescription() {
        return "A broad test of primitive values and specials such as Enums, EnumSets, Date, String, byte, short, int, .. Byte, Character; Short, Integer, .. <br> plus opaque private field with same name in a private subclass";
    }

    public enum SpecialEnum {
        ONE() {
            public void run() {
                System.out.println("One");
            }
        },
        TWO() {
            public void run() {
                System.out.println("One");
            }
        },
        THREE() {
            public void run() {
                System.out.println("One");
            }
        }
        ;

        public abstract void run();
        SpecialEnum() {};
    }

    public enum SampleEnum {
        None("","None",0),
                Complete("C","Complete",1),
                Complete_GiveUp_Allowed("D","Complete Give-Up Allowed",2),
                Complete_Position_Transaction_Allowed("E","Complete Position Transaction Allowed",3),
                Designated("G","Designated",4),
                Predesignated("P","Predesignated",5),
                Predesignated_GiveUp_Allowed("Q","Predesignated Give-Up Allowed",6),
                Predesignated_Position_Transaction_Allowed("R","Predesignated Position Transaction Allowed",7),
                GiveUp_Allowed("X","Give-Up Allowed",8),
                Position_Transaction_Allowed("Y","Position Transaction Allowed",9);

         String value;
         String stringRepresentation;
         int nativeEnumValue;

        SampleEnum(String value, String stringRepresentation, int nativeEnumValue)
        {
            this.value=value;
            this.stringRepresentation = stringRepresentation;
            this.nativeEnumValue = nativeEnumValue;
        }
    }

     char w = 234, x = 33344;
     byte y = -34, z = 126;
     short sh0 = 127;

     SpecialEnum specEn = SpecialEnum.TWO;

     int gg = -122;
     int zz = 99999;
     int ii = -23424;
     int jj = 0;
     int kk = Integer.MIN_VALUE;
     int hh = Integer.MAX_VALUE;

     long lll = 123;
     long mmm = 99999;

     double dq = 300.0;
     float t = 300.0f;

     boolean a0 = true;
     boolean a1 = false;
     boolean a2 = false;
     boolean a3 = true;

    Integer i0 = 1, i1 = 2, i3 = 23894, i4 = 238475638;
    Double  d1 = 2334234.0;
    Boolean bol1 = Boolean.TRUE;
    Boolean bol2 = new Boolean(false);

    Date date = new Date(1);
    Date date1 = new Date(2);

    SampleEnum en1 = SampleEnum.Predesignated_GiveUp_Allowed;
    EnumSet<SampleEnum> enSet = EnumSet.of(SampleEnum.Predesignated,SampleEnum.Complete);

    String st;

    String st1;
    String st2;
    String st3;
    String st4;
    String st5;
    String st6;
    String st7;

    StyleSheet on = null;
    URL on1 = null;
    File on2 = null;

    Object exceptions[] = {
        null, new Exception("test"), new ArrayIndexOutOfBoundsException(), new RuntimeException(new IllegalArgumentException("Blub"))
    };

    Subject sub = new Subject();

    public Primitives() {
    }

    public Primitives(int num) {
        st = "String"+num+"äöü";
        st1 = "String1"+num;
        st2 = st+"1"+num;
        hidden = "Visible";
        st3 = "visible its a hurdle this may be its a hurdle "+num;
        st4 = "etwas deutsch läuft.. ";
        st5 = st+"1"+num;
        st6 = "Some english, text; fragment. "+num;
        st7 = st6+" paokasd 1";
        try {
            throw new IOException();
        } catch (Exception ex) {
            exceptions[0] = ex;
        }
    }

    // to avoid measurement of pure stream init performance
    public static Primitives[] createPrimArray() {
        Primitives res[] = new Primitives[15];
        for (int i = 0; i < res.length; i++) {
            res[i] = new Primitives(i);
        }
        return res;
    }

}

class PrivatePrimitive {
    //private (many libs fail here, see (**)
    String hidden = "Hidden";
}

