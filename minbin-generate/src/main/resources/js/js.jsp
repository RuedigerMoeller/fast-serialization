<%
    import java.util.*;
    import java.io.*;
    import de.ruedigermoeller.template.*;

// add imports you need during generation =>
    import minbin.gen.*;
    import org.nustaq.serialization.*;

// this header is always required to make it work. Cut & Paste this as template
    public class CLAZZNAME implements IContextReceiver
    {
    public void receiveContext(Object o, PrintStream out) throws Exception
    {
    // asign context
    GenContext CTX = (GenContext)o;
    for ( int ii = 0; ii < CTX.clazzInfos.length; ii++ ) {
        // setup context
        GenClazzInfo INF = CTX.clazzInfos[ii];
        FSTClazzInfo CLZ = INF.getClzInfo();
        List<MsgInfo> MSGS = INF.getMsgs();
        FSTClazzInfo.FSTFieldInfo fi[] = CLZ.getFieldInfo();
// content begins here =>
%>
var J<%+CLZ.getClazz().getSimpleName()%> = function(obj) {
    this.__typeInfo = '<%+(INF.isClientSide()?CLZ.getClazz().getName():CLZ.getClazz().getSimpleName())%>';
<% if (INF.isActor()) {%>    this.receiverKey=obj;
    this._actorProxy = true;
<%}%>
<% for (int i = 0; ! INF.isActor() && i < fi.length; i++ ) {
    String fnam = fi[i].getField().getName();
    String na = "j_"+fnam;
    //na = Character.toUpperCase(na.charAt(0))+na.substring(1);
%>    this.<%+na%> = function() { return <%+CTX.getJSTransform(fi[i])%>; };
<% } /*for*/
%>
<% for (int i = 0; INF.isActor() && i < INF.getMsgs().size(); i++ ) {
    MsgInfo mi = INF.getMsgs().get(i);
%>    this.<%+mi.getName()%> = function(<% for(int pi=0;pi<mi.getParameters().length;pi++) {%><%+mi.getParameters()[pi].getName()%><%+((pi==mi.getParameters().length-1)?"":", ")%><%} %>)<%
        if (!INF.isClientSide()) {%> {
        var call = MinBin.obj('call', {
            method: '<%+mi.getName()%>',
            receiverKey: this.receiverKey,
            args: MinBin.jarray([<% for(int pi=0;pi<mi.getParameters().length;pi++) {%>
                <%+CTX.getJSTransform(mi.getParameters()[pi].getName(),mi.getParams()[pi])%><%+((pi<mi.getParameters().length-1) ? "," : "")%><%}%>
            ])
        });
<% if (mi.hasFutureResult()) { %>        return Kontraktor.send(call,true);
<% } else {%>        return Kontraktor.send(call);
<% } %>    };
<% } else { %> { /**/ };
<% }/*if isClienSide else*/
   } /*for*/
   if (!INF.isActor()) {
%>
    this.fromObj = function(obj) {
        for ( var key in obj ) {
            var setter = 'j_'.concat(key);
            if ( this.hasOwnProperty(setter) ) {
                this[key] = obj[key];
            }
        }
        return this;
    };
    if ( obj != null ) {
        this.fromObj(obj);
    }
<%} /*if is actor*/%>
};

<%          } // loop over classes%>

var mbfactory = function(clzname,jsObjOrRefId) {
    switch (clzname) {
<%
    for ( int ii = 0; ii < CTX.clazzInfos.length; ii++ ) {
    FSTClazzInfo CLZ = CTX.clazzInfos[ii].getClzInfo();
%>        case '<%+CLZ.getClazz().getSimpleName()%>': return new J<%+CLZ.getClazz().getSimpleName()%>(jsObjOrRefId);
<% } %>        default: if (!jsObjOrRefId) return { __typeInfo: clzname }; else { jsObjOrRefId.__typeInfo = clzname; return jsObjOrRefId; }
    }
};

MinBin.installFactory(mbfactory);
<%
    // this footer is always required (to match opening braces in header
    } // method
} // class
%>