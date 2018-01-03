package org.nustaq.serialization.coders;

import com.fasterxml.jackson.core.io.SerializedString;

public final class FSTJsonFieldNames {
    public final String TYPE; // = "typ"
    public final String OBJ; // = "obj";
    public final String SEQ_TYPE; // = "styp"
    public final String SEQ; // = "seq";
    public final String ENUM; // = "enum";
    public final String VAL; // = "val";
    public final String REF; // = "ref";
    public final SerializedString TYPE_S; // = new SerializedString(TYPE);
    public final SerializedString OBJ_S; // = new SerializedString(OBJ);
    public final SerializedString SEQ_TYPE_S; // = new SerializedString(SEQ_TYPE);
    public final SerializedString SEQ_S; // = new SerializedString(SEQ);
    public final SerializedString ENUM_S; // = new SerializedString(ENUM);
    public final SerializedString VAL_S; // = new SerializedString(VAL);
    public final SerializedString REF_S; // = new SerializedString(REF);

    public FSTJsonFieldNames(final String TYPE, final String OBJ, final String SEQ_TYPE, final String SEQ, final String ENUM, final String VAL, final String REF) {
        this.TYPE = TYPE;
        this.OBJ = OBJ;
        this.SEQ_TYPE = SEQ_TYPE;
        this.SEQ = SEQ;
        this.ENUM = ENUM;
        this.VAL = VAL;
        this.REF = REF;
        this.TYPE_S = new SerializedString(TYPE);
        this.OBJ_S = new SerializedString(OBJ);
        this.SEQ_TYPE_S = new SerializedString(SEQ_TYPE);
        this.SEQ_S = new SerializedString(SEQ);
        this.ENUM_S = new SerializedString(ENUM);
        this.VAL_S = new SerializedString(VAL);
        this.REF_S = new SerializedString(REF);
    }

    public FSTJsonFieldNames TYPE(final String TYPE) {
        return new FSTJsonFieldNames(TYPE, OBJ, SEQ_TYPE, SEQ, ENUM, VAL, REF);
    }
    public FSTJsonFieldNames OBJ(final String OBJ) {
        return new FSTJsonFieldNames(TYPE, OBJ, SEQ_TYPE, SEQ, ENUM, VAL, REF);
    }
    public FSTJsonFieldNames SEQ_TYPE(final String SEQ_TYPE) {
        return new FSTJsonFieldNames(TYPE, OBJ, SEQ_TYPE, SEQ, ENUM, VAL, REF);
    }
    public FSTJsonFieldNames SEQ(final String SEQ) {
        return new FSTJsonFieldNames(TYPE, OBJ, SEQ_TYPE, SEQ, ENUM, VAL, REF);
    }
    public FSTJsonFieldNames ENUM(final String ENUM) {
        return new FSTJsonFieldNames(TYPE, OBJ, SEQ_TYPE, SEQ, ENUM, VAL, REF);
    }
    public FSTJsonFieldNames VAL(final String VAL) {
        return new FSTJsonFieldNames(TYPE, OBJ, SEQ_TYPE, SEQ, ENUM, VAL, REF);
    }
    public FSTJsonFieldNames REF(final String REF) {
        return new FSTJsonFieldNames(TYPE, OBJ, SEQ_TYPE, SEQ, ENUM, VAL, REF);
    }
    public static final FSTJsonFieldNames DEFAULT = new FSTJsonFieldNames("typ", "obj", "styp", "seq", "enum", "val", "ref");
}
