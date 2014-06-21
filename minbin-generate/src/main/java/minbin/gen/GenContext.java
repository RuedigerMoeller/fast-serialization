package minbin.gen;

import org.nustaq.serialization.FSTClazzInfo;

import java.util.List;
import java.util.Map;

/**
 * Created by ruedi on 27.05.2014.
 */
public class GenContext {

    public FSTClazzInfo clazzInfos[];

    public String getJSTransform(FSTClazzInfo.FSTFieldInfo fi) {
        String res = ""; String name = "val"; //fi.getField().getName();
        if ( List.class.isAssignableFrom( fi.getType() ) ) {
            res += "MinBin.jlist("+name+")";
        } else
        if ( Map.class.isAssignableFrom( fi.getType() ) ) {
            res += "MinBin.jmap(val)";
        } else if (fi.isArray() && fi.isIntegral()) {
            switch (fi.getIntegralCode(fi.getType().getComponentType())) {
                case FSTClazzInfo.FSTFieldInfo.BOOL:
                    res += name+"?1:0";
                    break;
                case FSTClazzInfo.FSTFieldInfo.BYTE:
                    res += "MinBin.i8("+name+")";
                    break;
                case FSTClazzInfo.FSTFieldInfo.CHAR:
                    res += "MinBin.ui16("+name+")";
                    break;
                case FSTClazzInfo.FSTFieldInfo.SHORT:
                    res += "MinBin.i16("+name+")";
                    break;
                case FSTClazzInfo.FSTFieldInfo.INT:
                    res += "MinBin.i32("+name+")";
                    break;
                case FSTClazzInfo.FSTFieldInfo.LONG:
                    res += "MinBin.i64("+name+")";
                    break;
                case FSTClazzInfo.FSTFieldInfo.FLOAT:
                    res += "MinBin.dbl("+name+")";
                    break;
                case FSTClazzInfo.FSTFieldInfo.DOUBLE:
                    res += "MinBin.dbl("+name+")";
                    break;
                default: throw new RuntimeException("wat? "+fi.getIntegralType());
            }
        } else if ( fi.getType() == String[].class ) {
            res += "MinBin.strArr("+name+")";
        } else
            res += "val";
        res += "";
        return res;
    }

}
