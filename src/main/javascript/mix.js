// numbers: low 4bit == 1, high 4 bit denote length of int. if > int 64 length of floating point id
var INT_8      = parseInt( '00000001', 2 );
var INT_16     = parseInt( '00010001', 2 );
var INT_32     = parseInt( '00100001', 2 );
var INT_64     = parseInt( '00110001', 2 );

var ARRAY_MASK  = parseInt( '10000000', 2 ); // next item expected to be length
var UNSIGN_MASK = parseInt( '01000000', 2 ); // next item expected to be unsigned

var CHAR        = INT_16|UNSIGN_MASK;
var DOUBLE      = parseInt( '00000010', 2 );

var TUPEL       = parseInt( '00000011', 2 ); // id elems .. OR top 4 bits contain len < 16. == Object Array
var OBJECT      = parseInt( '00000100', 2 ); // id elems .. OR top 4 bits contain len < 16  == key<String> value<Object>
var ATOM        = parseInt( '00000101', 2 ); // nr of atom .. OR top 4 bits contains atom id if < 16

// default atoms full ids (hi 4 = id, low 4 = atom
var NULL      = parseInt( '00010000', 2 )|ATOM;
var TUPEL_END = parseInt( '00100000', 2 )|ATOM;
var STR_8     = parseInt( '00110000', 2 )|ATOM;
var STR_16    = parseInt( '01000000', 2 )|ATOM;

function Atom( name, value ) {
    this.name = name;
    this.value = value;
}

// global Atom instances 
var ATOM_TUPEL_END = new Atom("tuple_end", TUPEL_END>>>4);
var ATOM_NULL = new Atom("nil", NULL>>>4);
var ATOM_STR_8 = new Atom("string8", STR_8>>>4);
var ATOM_STR_16 = new Atom("string", STR_16>>>4);

function Tupel(id, contentArr, isStrMap) {
    this.content = contentArr;
    this.id = id;
    this.isStrMap = isStrMap;
}

function extractNumBytes(type) {
    return (type&48)>>>4;
}

///////////////////////////////////////////////////////////////////////////////////

function MixIn( /*Int8Array*/ int8arr ) {
    this.arr = int8arr;
    this.pos = 0;
}

MixIn.prototype.readIn = function() { return arr[pos++]; }
MixIn.prototype.peekIn = function() { return arr[pos]; }

MixIn.prototype.readRawInt = function(numBytes) {
    var res = 0;
    numBytes = (1<<numBytes);
    var shift = 0;
    for ( var i = 0; i < numBytes; i++ ) {
        var b = (this.readIn()+256) & 0xff;
        res += b<<shift;
        shift+=8;
    }
    return res;
}

MixIn.prototype.readInt = function() {
    var type = this.readIn();
    if ( (type & 0xf) >= DOUBLE || ((type& Mix.ARRAY_MASK)!=0)) {
        this.pos--;
        throw "no integer based id avaiable";
    }
    var numBytes = extractNumBytes(type);
    var l = this.readRawInt(numBytes);
    if ( (type & UNSIGN_MASK) == 0 ) {
        switch (numBytes) { // fixme: convert
            case 0: return l+1<<8;
            case 1: return l+1<<16;
            case 2: return l+1<<32;
            case 3: return l+1<<64;
        }
    }
    return l;
}

MixIn.prototype.readArray = function(type) {
    var typelen = extractNumBytes(type);
    var baseType = (type&0xf);
    var len = this.readInt();
    var result = null;
    if ( baseType == INT_8 ) {
        switch (typelen) {
            case 0: result = new Int8Array(len); break;
            case 1:
                if ((type & UNSIGN_MASK) != 0)
                    result = new Uint16Array(len);
                else
                    result = new Int16Array(len);
                break;
            case 2: result = new Int32Array(len); break;
            case 3: result = new Float64Array(len); break;
            default: throw "unknown array type";
        }
    } else if (baseType == DOUBLE) {
        result = new Float64Array(len);
    } else
        throw "unknown array structure";
    for ( var i = 0; i < len; i++ ) {
        result[i] = this.readRawInt(typelen);
    }
    return result;
}

MixIn.prototype.readRawDouble = function() {
    var byteBuff = this.readArray(this.readIn());
    var num = '';
    for (var i = 0; i < byteBuff.length; i++) {
        num += String.fromCharCode(byteBuff[i]);
    }
    return parseFloat(num);
}

MixIn.prototype.readDouble = new function() {
    var type = readIn();
    if ( (type & 0xf) != DOUBLE ) {
        this.pos--;
        throw "no double id avaiable";
    }
    return this.readRawDouble(this);
}

MixIn.prototype.readValue = function() {
    var typeTag = this.peekIn();
    var rawType = typeTag & 0xf;
    switch (rawType) {
        case INT_8:
            if ( (typeTag& Mix.ARRAY_MASK) != 0 )
                return this.readArray(this.readIn());
            return this.readInt();
        case DOUBLE:
            return this.readDouble();
        case Mix.TUPEL:
        case Mix.OBJECT:
            return this.readTupel(typeTag);
        case Mix.ATOM:
            if ( typeTag == Mix.TUPEL_END)
                return Mix.ATOM_TUPEL_END;
            if ( typeTag == Mix.STR_16)
                return Mix.ATOM_STR_16;
            if ( typeTag == Mix.STR_8)
                return Mix.ATOM_STR_8;
            if ( (typeTag>>>4) == 0 )
                return new Mix.Atom((int) readInt());
        else
            return new Mix.Atom(typeTag>>>4);
        default:
            throw new RuntimeException("unknown type "+typeTag);
    }
}