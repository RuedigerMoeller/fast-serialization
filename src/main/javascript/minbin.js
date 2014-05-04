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
function extractNumBytes(type) { return (byte) (1 << ((type & 3)-1)); }
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

function MBIn(rawmsg) {

    this.bytez = rawmsg; // Uint8Array
    this.pos = 0;

    this.peekIn = function() {
        return this.bytez[this.pos];
    }

    this.readIn = function() {
        return this.bytez[this.pos++];
    }

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
    }

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
    }

    this.readArray = function() {
        var type = readIn();
        if ( ! isArray(type) || ! isPrimitive(type) )
            throw "not a primitive array "+type;
        var len = this.readInt();
        var baseType = getBaseType(type);
        return this.readArrayRaw(type, len);
    }

}