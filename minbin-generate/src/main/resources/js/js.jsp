<%
    import java.util.*;
    import java.io.*;
    import de.ruedigermoeller.template.*;

// add imports you need during generation =>
    import minbin.gen.*;
    import de.ruedigermoeller.serialization.*;

// this header is always required to make it work. Cut & Paste this as template
    public class CLAZZNAME implements IContextReceiver
    {
        public void receiveContext(Object o, PrintStream out) throws Exception
        {
            // asign your context
            GenContext CTX = (GenContext)o;
            for ( int ii = 0; ii < CTX.clazzInfos.length; ii++ ) {
                FSTClazzInfo CLZ = CTX.clazzInfos[ii];
                FSTClazzInfo.FSTFieldInfo fi[] = CLZ.getFieldInfo();
%>
var J<%+CLZ.getClazz().getSimpleName()%> = function(obj) {
    this.__typeInfo = '<%+CLZ.getClazz().getSimpleName()%>';
<% for (int i = 0; i < fi.length; i++ ) {
    String na = fi[i].getField().getName();
    na = Character.toUpperCase(na.charAt(0))+na.substring(1);
%>    this.set<%+na%> = function(val) { this.<%+fi[i].getField().getName()%> = <%+CTX.getJSTransform(fi[i])%>; };
<% } /*for*/
%>    this.fromObj = function(obj) {
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

<%          } // loop over classes%>

var mbfactory = function(clzname) {
    switch (clzname) {
<%
    for ( int ii = 0; ii < CTX.clazzInfos.length; ii++ ) {
    FSTClazzInfo CLZ = CTX.clazzInfos[ii];
%>        case '<%+CLZ.getClazz().getSimpleName()%>': return new J<%+CLZ.getClazz().getSimpleName()%>();
<% } %>        default: return { __typeInfo: clzname };
    }
};

MinBin.installFactory(mbfactory);
<%
    // this footer is always required (to match opening braces in header
        }
    }
%>