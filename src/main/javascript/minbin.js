const END_MARKER = "END";

// low 3 bits contain type
const  INT_8  = 1; // 0x0001 (17 = array)
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

// predefined tag id's
const NULL = 7;
const STRING = 0;
const OBJECT = 5;
const SEQUENCE = 6;
const DOUBLE = 2;
const DOUBLE_ARR = 3;
const FLOAT = 1;
const FLOAT_ARR = 4;
const BOOL = 8;
const HANDLE = 9;

var clz2Ser = {};
var tag2Ser = [];
var tagCount = 0;

var serializer = [];
serializer[STRING] = new StringTagSer();

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
        var tmp = 0;
        if ( isSigned(type) ) {
            switch (numBytes) {
                case 1: tmp = (l&0xff); if ( l >= 128 ) l-=256; return tmp;
                case 2: tmp = (l&0xffff); if ( l >= 32768 ) l-=65636; return tmp;
                case 4: (l&0xffffffff); if ( l >= 0x80000000 ) l-=0xffffffff+1; return tmp;
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
            array[i] = this.readRawInt(type);
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

var fr = new FileReader();
fr.readAs