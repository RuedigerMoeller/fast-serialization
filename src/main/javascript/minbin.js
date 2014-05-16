

const END_MARKER = "END";

// low 3 bits contain type
const INT_8 = 1; // 0x0001 (17 = array)
const INT_16 = 2; // 2 (18 ..)
const INT_32 = 3; // 3 (19 ..)
const INT_64 = 4; // 4 (20 ..)
const TAG = 5; // 5, top 5 bits contains tag id
const END = 6; // 6, end marker
const RESERV = 7; // 7 escape for future extension

const UNSIGN_MASK = 8; // int only. if true => unsigned ..
const ARRAY_MASK = 16; // int only, next item expected to be length, then plain data little endian
const CHAR = UNSIGN_MASK | INT_16;

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

var MinBin = new function MinBin() {

    // public API =>

//    this.setKnownNames = function(listOfAttributeNames) {
//        this.nameHash = {};
//        for ( var i = 0; i < listOfAttributeNames.length; i++) {
//            this.nameHash[listOfAttributeNames[i],i];
//        }
//    };
    this.prettyPrint = function(object) { return new MBPrinter().prettyPrintStreamObject(object, "", ""); };
    this.decode = function (int8bufferOrString) {
        if ( this.isBinBlob(int8bufferOrString) )
            int8bufferOrString = String.fromCharCode.apply(null, new Uint16Array(int8bufferOrString));
        this.MBin.pos = 0;
        this.MBin.bytez = int8bufferOrString;
        return this.MBin.readObject();
    };
    this.encode = function (object) {
        this.MBOut.reset();
        this.MBOut.writeObject(object);
        return new Int8Array(this.MBOut.bytez, this.MBOut.pos);
    };

    // END public API

    this.serializer = [];
    this.nameHash = {};

    this.serializer[STRING] = new StringTagSer();
    this.serializer[NULL] = new NullTagSer();
    this.serializer[OBJECT] = new MBObjectTagSer();
    this.serializer[SEQUENCE] = new MBSequenceTagSer();
    this.serializer[FLOAT] = new FloatTagSer();
    this.serializer[DOUBLE] = new FloatTagSer();
    this.serializer[FLOAT_ARR] = new FloatArrTagSer();
    this.serializer[DOUBLE_ARR] = new FloatArrTagSer();
    this.serializer[BOOL] = new BigBoolTagSer();
    this.serializer[HANDLE] = new RefTagSer();

    this.serializer[STRING].tagId = STRING;
    this.serializer[NULL].tagId = NULL;
    this.serializer[OBJECT].tagId = OBJECT;
    this.serializer[SEQUENCE].tagId = SEQUENCE;
    this.serializer[FLOAT].tagId = FLOAT;
    this.serializer[DOUBLE].tagId = DOUBLE;
    this.serializer[FLOAT_ARR].tagId = FLOAT_ARR;
    this.serializer[DOUBLE_ARR].tagId = DOUBLE_ARR;
    this.serializer[BOOL].tagId = BOOL;
    this.serializer[HANDLE].tagId = HANDLE;

    this.getTagSerializerFor = function (obj) {
        if ( obj instanceof String || typeof obj == "string" )
            return this.serializer[STRING];
        if ( obj == null )
            return this.serializer[NULL];
        if ( obj instanceof Array )
            return this.serializer[SEQUENCE];
        if ( obj instanceof Object && ! this.isBinBlob(obj) )
            return this.serializer[OBJECT];
        if ( this.isFloat(obj) )
            return this.serializer[DOUBLE];
        if ( typeof obj == 'boolean' )
            return this.serializer[BOOL];
        return null;
    };

    this.isFloat = function(n) { return n === +n && n !== (n|0); };

    this.isInteger = function(n) { return n === +n && n === (n|0); };

    /** return wether type is primitive or primitive array */
    this.isPrimitive = function (type) { return (type & RESERV) < TAG; };

    /** return wether type is a tag */
    this.isTag = function (type) { return (type & 7) == TAG; };

    /** extract tag id/nr from byte  */
    this.getTagId = function(type) { return (type >>> 3); };

    /** get tag code as written to stream from tag id  */
    this.getTagCode = function(tagId) { return ((tagId << 3) | TAG); };

    /** is primitive type signed ? */
    this.isSigned = function(type) { return (type & 7) < TAG && (type & UNSIGN_MASK) == 0; };

    /** is primitive and array array */
    this.isArray = function(type) { return (type & 7) < TAG && (type & ARRAY_MASK) != 0; };

    this.extractNumBytes = function(type) { return (1 << ((type & 7) - 1)); };

    this.getBaseType = function(type) { return ((type & 7) | (type & UNSIGN_MASK)); };

    this.isBinBlob = function(value) {
        return value && value.buffer instanceof ArrayBuffer && value.byteLength !== undefined;
    };

    this.str2ab8 = function(str) {
        var bufView = new Int8Array(str.length);
        for (var i=0, strLen=str.length; i<strLen; i++) {
            bufView[i] = str.charCodeAt(i);
        }
        return bufView;
    };

    this.str2ab16 = function(str) {
        var bufView = new Uint16Array(str.length);
        for (var i=0, strLen=str.length; i<strLen; i++) {
            bufView[i] = str.charCodeAt(i);
        }
        return bufView;
    };

    this.MBin = new MBIn(null);
    this.MBOut = new MBOut();

};

function MBOut() {

    this.bytez = new Int8Array(5000);
    this.pos = 0;
    this.objId2pos = {};
    this.string2Id = {};

    this.__idcnt=1;
    this.objectId = function(obj) {
        if (obj==null || typeof obj === "undefined") return null;

        if ( typeof obj === "string" ) {
            if ( this.string2Id[obj] == null ) {
                this.string2Id[obj] = this.__idcnt++;
            }
            return this.string2Id[obj];
        }

        if (obj.__idcnt==null || typeof obj.__idcnt === "undefined")
            obj.__idcnt=this.__idcnt++;
        return obj.__idcnt;
    };

//    this.registerObject = function(o) {
//        if ( o != null )
//            this.objId2pos[this.objectId(o)] = this.pos;
//    };

    this.writeRefIfApplicable = function(o) {
        if ( o == null )
            return true;
        if ( typeof o == 'number' || typeof o == 'undefined' )
            return true;
        var id = this.objectId(o);
        if ( id != null ) {
            var lookup = this.objId2pos[id];
            if (lookup != null && lookup != this.pos) {
                this.writeTagHeader(HANDLE);
                this.writeIntPacked(lookup);
                return false;
            }
            this.objId2pos[this.objectId(o)] = this.pos;
            console.log("register ".concat(o).concat(" id ").concat(id).concat(" to ").concat(this.pos));
        } else {
            console.log("cant id ".concat(o) );
        }
        return true;
    };

    /**
     * write single byte, grow byte array if needed
     * @param b
     */
    this.writeOut = function(b) {
        if (this.pos == this.bytez.length - 1) {
            this.resize();
        }
        this.bytez[this.pos++] = b;
    };

    this.resize = function() {
        var tmp = new Int8Array(Math.min(this.bytez.length + 50 * 1000 * 1000, this.bytez.length * 2));
        tmp.set(this.bytez,0);
        this.bytez = tmp;
    };

    /**
     * write an int type with header
     * @param type
     * @param data
     */
    this.writeInt = function(type, data) {
        if (!MinBin.isPrimitive(type) || MinBin.isArray(type))
            throw "illegal type code";
        this.writeOut(type);
        this.writeRawInt(type, data);
    };

    /**
     * encode int without header tag
     * @param data
     */
    this.writeRawInt = function(type, data) {
        var numBytes = MinBin.extractNumBytes(type);
        for (var i = 0; i < numBytes; i++) {
            this.writeOut((data & 0xff));
            data = data >>> 8;
        }
    };

    /**
     * encode int using only as much bytes as needed to represent it
     * @param data
     */
    this.writeIntPacked = function(data) {
        if (data <= 127 && data >= -128) this.writeInt(INT_8, data);
        else if (data <= 32767 && data >= -32768) this.writeInt(INT_16, data);
        else if (data <= 0x7fffffff && data >= -0x80000000) this.writeInt(INT_32, data);
        else throw "long not supported from js, number overflow";
    };

    /**
     * write primitive array + header. no floating point or object array allowed. Just int based types
     * @param primitiveArray
     * @param start
     * @param len
     */
    this.writeArray = function(primitiveArray, start, len) {
        var type = ARRAY_MASK;
        if (primitiveArray instanceof Int8Array) type |= INT_8;
        else if (primitiveArray instanceof Int16Array) type |= INT_16;
        else if (primitiveArray instanceof Uint16Array)  type |= INT_16 | UNSIGN_MASK;
        else if (primitiveArray instanceof Int32Array)   type |= INT_32;
        else throw "unsupported type ".concat(primitiveArray);
        this.writeOut(type);
        this.writeIntPacked(len);
        for (var i = start; i < start + len; i++) {
            this.writeRawInt(type, primitiveArray[i]);
        }
    };

    this.writeTagHeader = function(tagId) {
        this.writeOut(MinBin.getTagCode(tagId));
    };

    this.writeTag = function( obj ) {
        if (obj==END_MARKER) {
            this.writeOut(END);
            return;
        }
        var tagSerializer = MinBin.getTagSerializerFor(obj);
        if ( tagSerializer == MinBin.serializer[SEQUENCE] || tagSerializer == MinBin.serializer[OBJECT] ) {
            if ( !this.writeRefIfApplicable(obj) )
                return;
        }
        if ( tagSerializer == null ) {
            throw "no tag serializer found for "+obj;
        }
        this.writeTagHeader(tagSerializer.tagId);
        tagSerializer.writeTag(obj,this);
    };

    this.getWritten = function() { return this.pos; };
    this.getBytez = function() { return this.bytez; };

    /**
     * completely reset state
     */
    this.reset = function() {
        this.pos = 0;
        this.__idcnt = 1;
        this.objId2pos = {};
        this.string2Id = {};
    };

    this.writeObject = function(o) {
        if ( o == null ) {
            this.writeTag(o);
        } else if ( MinBin.isInteger(o) ) {
            this.writeIntPacked(o);
        } else if ( MinBin.isBinBlob(o) ) {
            if ( this.writeRefIfApplicable(o) )
                this.writeArray( o, 0, o.length);
        } else if (o instanceof MBLong){
            this.writeOut(INT_64);
            this.writeRawInt(INT_32, o.loInt);
            this.writeRawInt(INT_32, o.hiInt);
        } else {
            this.writeTag(o);
        }
    };
}


function MBIn(rawmsg) {

    this.bytez = rawmsg; // Int8Array
    this.pos = 0;

    this.peekIn = function() { return this.bytez.charCodeAt(this.pos); };

    this.readIn = function() { return this.bytez.charCodeAt(this.pos++); };

    this.readRawInt = function(type) {
        var res = 0;
        var numBytes = MinBin.extractNumBytes(type);
        if ( numBytes == 8 ) // special handling for long
        {
            var lng = new MBLong();
            lng.loInt = ((this.readIn()+256) & 0xff);
            lng.loInt += ((this.readIn()+256) & 0xff)<<8;
            lng.loInt += ((this.readIn()+256) & 0xff)<<16;
            lng.loInt += ((this.readIn()+256) & 0xff)<<24;
            lng.hiInt = ((this.readIn()+256) & 0xff);
            lng.hiInt += ((this.readIn()+256) & 0xff)<<8;
            lng.hiInt += ((this.readIn()+256) & 0xff)<<16;
            lng.hiInt += ((this.readIn()+256) & 0xff)<<24;
            return lng;
        }
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
        if ( !MinBin.isPrimitive(type) || MinBin.isArray(type)) {
            this.pos--;
            throw "no integer based id avaiable:"+type;
        }
        var numBytes = MinBin.extractNumBytes(type);
        if ( numBytes == 8 ) { // long special for js
            return this.readRawInt(type);
        }
        var l = this.readRawInt(type);
        if ( MinBin.isSigned(type) ) {
            switch (numBytes) {
                case 1: l = (l&0xff); if ( l >= 128 ) l-=256; return l;
                case 2: l = (l&0xffff); if ( l >= 32768 ) l-=65636; return l;
                case 4: l = (l&0xffffffff); if ( l >= 0x80000000 ) l-=0xffffffff+1; return l;
                default: throw "Wat? ".concat(numBytes);
            }
        }
        return l;
    };

    this.readArray = function() {
        var type = this.readIn();
        if ( ! MinBin.isArray(type) || ! MinBin.isPrimitive(type) )
            throw "not a primitive array "+type;
        var len = this.readInt();
        var baseType = MinBin.getBaseType(type);
        var result;
        switch (baseType) {
            case INT_8:  result = new Int8Array(len);  break;
            case INT_16: result = new Int16Array(len); break;
            case CHAR:   result = new Uint16Array(len);  break;
            case INT_32: result = new Int32Array(len); break;
            case INT_64:
                result = [];
                for ( var i = 0; i < len; i++ ) {
                    var l = new MBLong();
                    l.loInt = this.readRawInt(INT_32);
                    l.hiInt = this.readRawInt(INT_32);
                    result.push(l);
                }
                result.__typeInfo = "longarr";
                return result;
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
        var tagId = MinBin.getTagId(tag);
        var ts = MinBin.serializer[tagId];
        if (ts==null) {
            throw "no tagser found ".concat(tagId);
        }
        return ts.readTag(this);
    };

    this.readObject = function() {
        var type = this.peekIn();
        if (type==END) {
            this.readIn();
            return END_MARKER;
        }
        if ( MinBin.isPrimitive(type) ) {
            if ( MinBin.isArray(type) ) {
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
            return this.readTag(this.readIn());
        }
    }

}


function MBRef(pos) {
    this.pos = pos;
}

function MBLong() {
    this.loInt = 0;
    this.hiInt = 0;
}

function StringTagSer() {

    this.writeTag = function(data, out) {
        var s = data;
        var isAsc = s.length < 64;
        if (isAsc) {
            for (var i = 0; i < s.length; i++) {
                if (s.charCodeAt(i) >= 127) {
                    isAsc = false;
                    break;
                }
            }
        }
        out.writeOut(INT_8|ARRAY_MASK);
        out.writeIntPacked(s.length);
        for (var i = 0; i < s.length; i++) {
            if (isAsc)
                out.writeOut(data.charCodeAt(i));
            else
                out.writeRawInt(CHAR,data.charCodeAt(i));
        }
    };

    this.readTag = function(inp) {
        var arr = inp.readArray();
        var res = '';
        for (var i = 0; i < arr.length; i++) {
            res += String.fromCharCode(arr[i]);
        }
        return res;
    };

}

function NullTagSer() {
    this.writeTag = function(data, out) {};
    this.readTag = function(inp) { return null; };
}

function MBObjectTagSer() {
    /**
     * tag is already written. break down the given object into more tags or primitives
     *
     * @param data
     * @param out
     */
    this.writeTag = function(data, out) {
        var name = data.__typeInfo;
        if ( typeof name === "undefined" ) {
            name = data.constructor.name;
        }
        out.writeObject(name);
        out.writeIntPacked(-1);
        for (var next in data ) {
            if (data.hasOwnProperty(next) && next != "__typeInfo" && next != "__idcnt" ) {
                out.writeObject(next);
                if ( out.writeRefIfApplicable(data[next]) ) {
                    out.writeObject(data[next]);
                }
            }
        }
        out.writeOut(END);
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
            if ( key == "st" )
                var x = 99; // debug
            console.log('key '.concat(key));
            if (key==END_MARKER)
                break;
            obj[key] = inp.readObject();
        }
        return obj;
    };
}

function MBSequenceTagSer() {
    this.writeTag = function(data, out) {
        out.writeTag(data.__typeInfo);
        out.writeIntPacked(data.length);
        for (var i = 0; i < data.length; i++) {
            if ( out.writeRefIfApplicable(data[i]) )
                out.writeObject(data[i]);
        }
    };
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
}

function FloatTagSer() {
    this.writeTag = function(data, out) {
        data = ''.concat(data);
        var bytes = MinBin.str2ab8(data);
        out.writeArray(bytes, 0, bytes.length);
    };
    this.readTag = function(inp) {
        var ba = inp.readArray();
        var s = String.fromCharCode.apply(null, new Uint16Array(ba));
        return parseFloat(s);
    };
}

function BigBoolTagSer() {
    this.writeTag = function(data,out) { out.writeInt(INT_8, data?1:0); };
    this.readTag = function(inp) { return inp.readInt() != 0; };
}

function RefTagSer() {
    this.writeTag = function(data,out) {
        throw "unexpected call";
    };
    this.readTag = function(inp) {
        return new MBRef(inp.readInt());
    };
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
}

function MBPrinter(object) {

    this.prettyPrintStreamObject = function( o, out, indent ) {
        if (o instanceof MBRef) {
            return "#".concat(o.pos);
        } else
        if ( o instanceof MBLong) {
            return this.objectToString(o);
        } else if ( o instanceof Array ) {
            return this.prettyPrintSequence(o, out, indent.concat("  "));
        } else if (o instanceof Object && ! MinBin.isBinBlob(o) ) {
            return this.prettyPrintObject(o, out, indent.concat("  "));
        } else
            return this.objectToString(o);
    };

    this.prettyPrintObject = function(t, out, indent) {
        out = this.prettyPrintStreamObject(t.__typeInfo,out,indent);
        out = out.concat(" {\n");
        for (var next in t ) {
            if (t.hasOwnProperty(next) && next != "__typeInfo" && next != "__idcnt") {
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
        if ( o instanceof MBLong ) {
            return "L[".concat(o.hiInt).concat(",").concat(o.loInt).concat("]");
        }
        if ( o == null ) {
            return "NULL";
        }
        if ( o instanceof Array || MinBin.isBinBlob(o))
            return this.arrayToString(o);
        if ( o instanceof String || typeof o == "string") {
            return "\"".concat(o).concat("\"");
        }
        return "".concat(o);
    };

}

