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
function J<%+CLZ.getClazz().getSimpleName()%>() {
    this.__typeInfo = '<%+CLZ.getClazz().getSimpleName()%>';
<% for (int i = 0; i < fi.length; i++ ) {
    String na = fi[i].getField().getName();
    na = Character.toUpperCase(na.charAt(0))+na.substring(1);
%>    this.set<%+na%> = function(val) { this.<%+fi[i].getField().getName()%> = <%+CTX.getJSTransform(fi[i])%>; }
<% } /*for*/
%>    this.map = function(obj) {
        for ( var key in obj ) {
            if ( this.hasOwnProperty('set'.concat(key.substring(0,1).toUpperCase()).concat(key.substring(1)) ) {
                this[concat(key)](map[key]);
            }
        }
    }
}

<%          }
    // this footer is always required (to match opening braces in header
        }
    }
%>