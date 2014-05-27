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
            FSTClazzInfo CLZ = CTX.clazz;
            FSTClazzInfo.FSTFieldInfo fi[] = CLZ.getFieldInfo();
%>
function J<%+CLZ.getClazz().getSimpleName()%>(map) {
<% for (int i = 0; i < fi.length; i++ ) {
%>    this.__<%+fi[i].getField().getName()%> = ;
<% } /*for*/
%>}
<%
    // this footer is always required (to match opening braces in header
        }
    }
%>