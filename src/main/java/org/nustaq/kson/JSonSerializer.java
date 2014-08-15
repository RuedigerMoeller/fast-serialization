package org.nustaq.kson;

import org.nustaq.serialization.FSTConfiguration;

import java.io.ObjectOutput;

/**
 * Created by ruedi on 12.08.2014.
 */
public class JSonSerializer extends KsonSerializer {

    protected String clazzAttributeName = "_type";
    protected boolean quoteKeyNames = true;
    private boolean tagTypes = true;

    public JSonSerializer(KsonCharOutput out, KsonTypeMapper mapper, FSTConfiguration conf) {
        super(out, mapper, conf);
    }

    public boolean isQuoteKeyNames() {
        return quoteKeyNames;
    }

    public void setQuoteKeyNames(boolean quoteKeyNames) {
        this.quoteKeyNames = quoteKeyNames;
    }

    public String getClazzAttributeName() {
        return clazzAttributeName;
    }

    public void setClazzAttributeName(String clazzAttributeName) {
        this.clazzAttributeName = clazzAttributeName;
    }

    @Override
    protected boolean shouldQuote(String string) {
        return true;
    }

    @Override
    protected void removeLastListSep() {
        out.back(2);
        out.writeChar('\n'); // hack, anyway json spec will not change and easy to fix in cases
    }

    @Override
    protected void writeListEnd() {
        out.writeChar(']');
    }

    @Override
    protected void writeListStart() {
        out.writeString("[ ");
    }

    @Override
    protected void writeListSep() {
        if (out.lastChar() == '\n') {
            out.back(1);
            out.writeChar(',');
            out.writeChar('\n');
        } else {
            out.writeString(", ");
        }
    }

    @Override
    protected void writeClazzTag(Class expectedClass, Object o) {
        if (!tagTypes || expectedClass == o.getClass()) {
            out.writeString("{");
        } else {
            String stringForType = mapper.getStringForType(o.getClass());
            if (quoteKeyNames) {
                out.writeString("{ \"" + clazzAttributeName + "\": \"" + stringForType + "\" ,");
            } else {
                out.writeString("{ " + clazzAttributeName + ": \"" + stringForType + "\" ,");
            }
        }
    }

    @Override
    protected void writeKey(String name) {
        if (quoteKeyNames) {
            out.writeChar('\"');
            super.writeKey(name);
            out.writeChar('\"');
        } else
            super.writeKey(name);
    }

    public JSonSerializer noTypeTags() {
        tagTypes = false;
        return this;
    }
}
