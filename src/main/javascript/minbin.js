const END_MARKER = "END";

// low 3 bits contain type
const INT_8  = 1; // 0x0001 (17 = array)
const INT_16 =  2; // 2 (18 ..)
const INT_32 =  3; // 3 (19 ..)
const INT_64 =  4; // 4 (20 ..)
const TAG    =  5; // 5, top 5 bits contains tag id
const END    =  6; // 6, end marker
const RESERV =  7; // 7 escape for future extension

const UNSIGN_MASK = 8; // int only. if true => unsigned ..
const ARRAY_MASK = 16; // int only, next item expected to be length, then plain data little endian
const CHAR   = UNSIGN_MASK|INT_16;

/** return wether type is primitive or primitive array */
function isPrimitive(type) { return (type & RESERV) < TAG; }
/** return wether type is a tag */
function isTag(type) { return (type & 3) == TAG; }
/** extract tag id/nr from byte  */
function getTagId(type) { return (type >>> 3); }
/** get tag code as written to stream from tag id  */
function getTagCode(tagId) { return ((tagId << 3)|TAG); }
/** is primitive type signed ? */
function isSigned(type) { return (type & 3) < TAG && (type & UNSIGN_MASK) == 0; }
/** is primitive and array array */
function isArray(type) {  return (type & 3) < TAG && (type & ARRAY_MASK) != 0; }
function extractNumBytes(type) { return (1 << ((type & 3)-1)); }
function getBaseType(type) { return ((type&3)|(type&UNSIGN_MASK)); }
function prettyPrint(object) { return new MBPrinter().prettyPrintObject(object,"",""); }

// predefined tag id's
const STRING = 0;
const FLOAT = 1;
const DOUBLE = 2;
const DOUBLE_ARR = 3;
const FLOAT_ARR = 4;
const OBJECT = 5;
const SEQUENCE = 6;
const NULL = 7;
const BOOL = 8;
const HANDLE = 9; // not supported in 1.0

var clz2Ser = {};
var tag2Ser = [];
var tagCount = 0;

var serializer = [];

serializer[STRING] = new StringTagSer();
serializer[NULL] = new NullTagSer();
serializer[OBJECT] = new MBObjectTagSer();
serializer[SEQUENCE] = new MBSequenceTagSer();
serializer[FLOAT] = new FloatTagSer();
serializer[DOUBLE] = new FloatTagSer();
serializer[FLOAT_ARR] = new FloatArrTagSer();
serializer[DOUBLE_ARR] = new FloatArrTagSer();
serializer[BOOL] = new BigBoolTagSer();

function MBIn(rawmsg) {

    this.bytez = rawmsg; // Int8Array
    this.pos = 0;

    this.peekIn = function() {
        return this.bytez.charCodeAt(this.pos);
    };

    this.readIn = function() {
        return this.bytez.charCodeAt(this.pos++);
    };

    this.readRawInt = function(type) {
        var res = 0;
        var numBytes = extractNumBytes(type);
        var shift = 0;
        for ( var i = 0; i < numBytes; i++ ) {
            var b = (this.readIn()+256) & 0xff;
            res += b<<shift;
            shift+=8;
        }
        return res;
    };

    this.readInt = function() {
        var type = this.readIn();
        if ( !isPrimitive(type) || isArray(type)) {
            this.pos--;
            throw "no integer based id avaiable:"+type;
        }
        var numBytes = extractNumBytes(type);
        var l = this.readRawInt(type);
        if ( isSigned(type) ) {
            switch (numBytes) {
                case 1: l = (l&0xff); if ( l >= 128 ) l-=256; return l;
                case 2: l = (l&0xffff); if ( l >= 32768 ) l-=65636; return l;
                case 4: l = (l&0xffffffff); if ( l >= 0x80000000 ) l-=0xffffffff+1; return l;
                case 8: return l;
                default: throw "Wat?";
            }
        }
        return l;
    };

    this.readArray = function() {
        var type = this.readIn();
        if ( ! isArray(type) || ! isPrimitive(type) )
            throw "not a primitive array "+type;
        var len = this.readInt();
        var baseType = getBaseType(type);
        var result;
        switch (baseType) {
            case INT_8:  result = new Int8Array(len);  break;
            case INT_16: result = new Int16Array(len); break;
            case CHAR:   result = new Uint16Array(len);  break;
            case INT_32: result = new Int32Array(len); break;
            case INT_64: result = new Float64Array(len); break; // how to handle this in js ?
            default:
                throw "unknown array type";
        }
        return this.readArrayRaw(type, len, result);
    };

    this.readArrayRaw = function(type,len,array) {
        for ( var i = 0; i < len; i++ ) {
            var val = this.readRawInt(type);
            array[i] = val;
        }
        return array;
    };

    this.readTag = function( tag ) {
        var tagId = getTagId(tag);
        var ts = serializer[tagId];
        if (ts==null) {
            throw "no tagser found".concat(tagId);
        }
        return ts.readTag(this);
    };

    this.readObject = function() {
        var type = this.peekIn();
        if (type==END) {
            this.readIn();
            return END_MARKER;
        }
        if ( isPrimitive(type) ) {
            if ( isArray(type) ) {
                return this.readArray();
            }
            switch (type) {
                case INT_8:  return this.readInt();
                case INT_16: return this.readInt();
                case CHAR:   return this.readInt();
                case INT_32: return this.readInt();
                case INT_64: return this.readInt();
                default: throw "unexpected primitive type:";
            }
        } else {
//            if ( getTagId(type) == HANDLE ) {
//                return new MBRef((Integer) readTag(readIn()));
//            }
            return this.readTag(this.readIn());
        }
    }

}

function StringTagSer() {

    this.writeTag = function(data, out) {
        var s = data;
        var isAsc = s.length < 64;
        if (isAsc) {
            for (var i = 0; i < s.length; i++) {
                if (s.charAt(i) >= 127) {
                    isAsc = false;
                    break;
                }
            }
        }
//        if (isAsc) {
//            byte[] strBytes = s.getBytes();
//            out.writeOut((byte) (MinBin.INT_8|MinBin.ARRAY_MASK));
//            out.writeIntPacked(strBytes.length);
//            out.writeRaw(strBytes, 0, strBytes.length);
//        } else {
//            final char[] chars = s.toCharArray();
//            out.writeArray(chars, 0, chars.length);
//        }
    };

    this.readTag = function(inp) {
        var arr = inp.readArray();
        var res = '';
        for (var i = 0; i < arr.length; i++) {
            res += String.fromCharCode(arr[i]);
        }
        return res;
    };

//    @Override
//    public Class getClassEncoded() {
//        return String.class;
//    }
}

function NullTagSer() {

    this.writeTag = function(data, out) {};

    this.readTag = function(inp) { return null; };

//    @Override
//    public Class getClassEncoded() {
//        return null;
//    }
}

function MBObjectTagSer() {

    /**
     * tag is already written. break down the given object into more tags or primitives
     *
     * @param data
     * @param out
     */
    this.writeTag = function(data, out) {
//        MBObject ob = (MBObject) data;
//        out.writeObject(ob.getTypeInfo());
//        out.writeIntPacked(ob.size());
//        for (Iterator iterator = ob.keyIterator(); iterator.hasNext(); ) {
//            String next = (String) iterator.next();
//            out.writeTag(next);
//            out.writeObject(ob.get(next));
//        }
    };

    /**
     * tag is already read, reconstruct the object
     *
     * @param in
     * @return
     */
    this.readTag = function(inp) {
        var typeInfo = inp.readObject();
        var len = inp.readInt();
        var obj = { "__typeInfo" : typeInfo };
        for ( var i=0; i < len || len < 0 ; i++ ) {
            var key = inp.readObject();
            if (key==END_MARKER)
                break;
            obj[key] = inp.readObject();
        }
        return obj;
    };

//    public Class getClassEncoded() {
//        return MBObject.class;
//    }
}

function MBSequenceTagSer() {

    /**
     * tag is already written. break down the given object into more tags or primitives
     *
     * @param data
     * @param out
     */
    this.writeTag = function(data, out) {
//        MBSequence ob = (MBSequence) data;
//        out.writeTag(ob.getTypeInfo());
//        out.writeIntPacked(ob.size());
//        for (int i = 0; i < ob.size(); i++) {
//            Object o = ob.get(i);
//            out.writeObject(o);
//        }
    };

    /**
     * tag is already read, reconstruct the object
     *
     * @param in
     * @return
     */
    this.readTag = function (inp) {
        var typeInfo = inp.readObject();
        var len = inp.readInt();
        var arr = [];
        arr["__typeInfo"] = typeInfo;
        for ( var i=0; i < len || len < 0; i++ ) {
            var o = inp.readObject();
            if ( o == END_MARKER )
                break;
            arr.push(o);
        }
        return arr;
    };


    /**
     * @return the class this tag serializer is responsible for
     */
//    @Override
//    public Class getClassEncoded() {
//        return MBSequence.class;
//    }
}

function FloatTagSer() {

    this.writeTag = function(data, out) {
//        byte[] bytes = Float.toString((Float) data).getBytes();
//        out.writeArray(bytes, 0, bytes.length);
    };

    this.readTag = function(inp) {
        var ba = inp.readArray();
        var s = String.fromCharCode.apply(null, new Uint16Array(ba));
        return parseFloat(s);
    };

//    @Override
//    public Class getClassEncoded() {
//        return Float.class;
//    }
}

function BigBoolTagSer() {

    this.writeTag = function(data,out) {
//        out.writeInt(INT_8, data?1:0);
    };

    this.readTag = function(inp) {
        return inp.readInt() != 0;
    }

//    @Override
//    public Class getClassEncoded() {
//        return Boolean.class;
//    }
}


function FloatArrTagSer() {

    this.writeTag = function(data, out) {
//        var d[] = [];
//        out.writeIntPacked(((double[]) data).length);
//        for (int i = 0; i < d.length; i++) {
//            byte[] bytes = Float.toString(d[i]).getBytes();
//            out.writeArray(bytes, 0, bytes.length);
//        }
    };

    this.readTag = function(inp) {
        var len = inp.readInt();
        var res = [];
        for (var i = 0; i < len; i++) {
            var ba = inp.readArray();
            var s = String.fromCharCode.apply(null, new Uint16Array(ba));
            res.push( parseFloat(s) );
        }
        return res;
    };

//    @Override
//    public Class getClassEncoded() {
//        return float[].class;
//    }
}

function MBPrinter(object) {

    this.prettyPrintStreamObject = function( o, out, indent ) {
        if ( o instanceof Array ) {
            return this.prettyPrintSequence(o, out, indent.concat("  "));
        } else if (o instanceof Object && ! this.isAbv(o) ) {
            return this.prettyPrintObject(o, out, indent.concat("  "));
        } else
            return this.objectToString(o);
    };

    this.isAbv = function(value) {
        return value && value.buffer instanceof ArrayBuffer && value.byteLength !== undefined;
    };

    this.prettyPrintObject = function(t, out, indent) {
        out = this.prettyPrintStreamObject(t.__typeInfo,out,indent);
        out = out.concat(" {\n");
        for (var next in t ) {
            if (t.hasOwnProperty(next) && next != "__typeInfo") {
                out = out.concat( indent+"  " );
                out = out.concat( this.prettyPrintStreamObject(next, out, indent) );
                out = out.concat(" : ");
                out = out.concat( this.prettyPrintStreamObject( t[next], "", indent ) );
                out = out.concat("\n");
            }
        }
        out = out.concat(indent.concat( "}") );
        return out;
    };

    this.prettyPrintSequence = function(t, out, indent) {
        out = out.concat(this.prettyPrintStreamObject(t.__typeInfo,out,indent));
        out = out.concat(" [\n");
        for (var i = 0; i < t.length; i++) {
            out = out.concat(indent.concat("  "));
            out = out.concat(this.prettyPrintStreamObject(t[i], "", indent));
            out = out.concat("\n");
        }
        out = out.concat(indent.concat( "]") );
        return out;
    };

    this.arrayToString = function(o) {
        var len = o.length;
        var res = "[ ";
        for (var i = 0; i < len; i++) {
            res = res.concat(o[i]);
            if ( i < len-1)
                res=res.concat(",");
        }
        res = res.concat(" ]");
        return res;
    };

    this.objectToString = function(o) {
        if ( o == null ) {
            return "NULL";
        }
        if ( o instanceof Array || this.isAbv(o))
            return this.arrayToString(o);
        if ( o instanceof String || typeof o == "string") {
            return "\"".concat(o).concat("\"");
        }
        return "".concat(o);
    }

}
