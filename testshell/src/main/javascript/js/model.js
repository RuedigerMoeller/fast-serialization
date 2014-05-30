
var JTestRequest = function(obj) {
    this.__typeInfo = 'TestRequest';
    this.setObjectToSend = function(val) { this.objectToSend = val; };
    this.fromObj = function(obj) {
        for ( var key in obj ) {
            var setter = 'set'.concat(key.substr(0,1).toUpperCase()).concat(key.substr(1));
            if ( this.hasOwnProperty(setter) ) {
                this[setter](obj[key]);
            }
        }
        return this;
    };
    if ( obj != null ) {
        this.fromObj(obj);
    }
};


var JEvent = function(obj) {
    this.__typeInfo = 'Event';
    this.setDate = function(val) { this.date = val; };
    this.setTitle = function(val) { this.title = val; };
    this.fromObj = function(obj) {
        for ( var key in obj ) {
            var setter = 'set'.concat(key.substr(0,1).toUpperCase()).concat(key.substr(1));
            if ( this.hasOwnProperty(setter) ) {
                this[setter](obj[key]);
            }
        }
        return this;
    };
    if ( obj != null ) {
        this.fromObj(obj);
    }
};


var JBasicValues = function(obj) {
    this.__typeInfo = 'BasicValues';
    this.setB = function(val) { this.b = val; };
    this.setB1 = function(val) { this.b1 = val; };
    this.setB2 = function(val) { this.b2 = val; };
    this.setAnInt = function(val) { this.anInt = val; };
    this.setAList = function(val) { this.aList = MinBin.jlist(val); };
    this.setAMap = function(val) { this.aMap = MinBin.jmap(val); };
    this.setAString = function(val) { this.aString = val; };
    this.setAStringArr = function(val) { this.aStringArr = MinBin.strArr(val); };
    this.setBytes = function(val) { this.bytes = MinBin.i8(val); };
    this.setAnIntArray = function(val) { this.anIntArray = MinBin.i32(val); };
    this.fromObj = function(obj) {
        for ( var key in obj ) {
            var setter = 'set'.concat(key.substr(0,1).toUpperCase()).concat(key.substr(1));
            if ( this.hasOwnProperty(setter) ) {
                this[setter](obj[key]);
            }
        }
        return this;
    };
    if ( obj != null ) {
        this.fromObj(obj);
    }
};


var JPerson = function(obj) {
    this.__typeInfo = 'Person';
    this.setFollowers = function(val) { this.followers = MinBin.jlist(val); };
    this.setFriends = function(val) { this.friends = MinBin.jlist(val); };
    this.setFirstName = function(val) { this.firstName = val; };
    this.setMisc = function(val) { this.misc = val; };
    this.setName = function(val) { this.name = val; };
    this.fromObj = function(obj) {
        for ( var key in obj ) {
            var setter = 'set'.concat(key.substr(0,1).toUpperCase()).concat(key.substr(1));
            if ( this.hasOwnProperty(setter) ) {
                this[setter](obj[key]);
            }
        }
        return this;
    };
    if ( obj != null ) {
        this.fromObj(obj);
    }
};


var JMirrorRequest = function(obj) {
    this.__typeInfo = 'MirrorRequest';
    this.setToMirror = function(val) { this.toMirror = val; };
    this.fromObj = function(obj) {
        for ( var key in obj ) {
            var setter = 'set'.concat(key.substr(0,1).toUpperCase()).concat(key.substr(1));
            if ( this.hasOwnProperty(setter) ) {
                this[setter](obj[key]);
            }
        }
        return this;
    };
    if ( obj != null ) {
        this.fromObj(obj);
    }
};



var mbfactory = function(clzname) {
    switch (clzname) {
        case 'TestRequest': return new JTestRequest();
        case 'Event': return new JEvent();
        case 'BasicValues': return new JBasicValues();
        case 'Person': return new JPerson();
        case 'MirrorRequest': return new JMirrorRequest();
        default: return { __typeInfo: clzname };
    }
};

MinBin.installFactory(mbfactory);
