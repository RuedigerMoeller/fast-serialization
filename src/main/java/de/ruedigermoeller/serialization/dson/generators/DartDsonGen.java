package de.ruedigermoeller.serialization.dson.generators;

import de.ruedigermoeller.serialization.FSTClazzInfo;
import de.ruedigermoeller.serialization.FSTConfiguration;
import de.ruedigermoeller.serialization.dson.DsonDeserializer;
import de.ruedigermoeller.serialization.dson.DsonTypeMapper;

import java.io.File;
import java.io.PrintStream;
import java.util.*;

/**
 * Copyright (c) 2012, Ruediger Moeller. All rights reserved.
 * <p/>
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * <p/>
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 * <p/>
 * Date: 22.12.13
 * Time: 02:37
 * To change this template use File | Settings | File Templates.
 */
public class DartDsonGen {

    /**
     * generate mapping fqNames to simple names
     * @param out - dart file name
     * @param clazzes
     * @throws Exception
     */
    public void generate( File out, Class ... clazzes) throws Exception {
        DsonTypeMapper mper = new DsonTypeMapper();
        for (int i = 0; i < clazzes.length; i++) {
            Class clazz = clazzes[i];
            mper.map(clazz.getSimpleName(),clazz);
        }
        generate(out,mper,clazzes);
    }

    public void generate( File out, DsonTypeMapper tm, Class ... clazzes) throws Exception {
        generate(out,Arrays.asList(clazzes),tm);
    }

    public void generate( File out, List<Class> clazzes, DsonTypeMapper tm ) throws Exception {
        PrintStream ps = new PrintStream(out);
        generate(out.getName().substring(0, out.getName().length() - ".dart".length()), ps, clazzes, tm);
    }

    protected void generateFactory(String name, PrintStream ps, List<Class> clazzes, DsonTypeMapper tm) {
        ps.println();
        ps.println( "class "+name+"Factory {");
        ps.println("  newInstance( String name ) {");
        ps.println("    switch(name) {");
        for (int i = 0; i < clazzes.size(); i++) {
            Class aClass = (Class) clazzes.get(i);
            String tagname = tm.getStringForType(aClass);
            ps.println("      case '" + tagname + "': return new " + tagname + "();");
        }
        ps.println( "      default: null;");
        ps.println( "    }");
        ps.println( "  }");
        ps.println( "}");
    }

    public void generate(String name, PrintStream ps, List clazzes, DsonTypeMapper tm) {
        for (int i = 0; i < clazzes.size(); i++) {
            Class aClass = (Class) clazzes.get(i);
            FSTClazzInfo clInfo = FSTConfiguration.getDefaultConfiguration().getCLInfoRegistry().getCLInfo(aClass);
            generate(ps,clInfo,tm);
        }
        generateFactory(name, ps, clazzes, tm);
    }

    protected void generate(PrintStream ps, FSTClazzInfo clInfo, DsonTypeMapper tm) {
        ps.println();
        String name = tm.getStringForType(clInfo.getClazz());
        ps.println("class " + name + " /*implements DsonReflectable*/ {");
        ps.println();
        ps.println("  dsonName() => '" + name + "';");
        ps.println();
        FSTClazzInfo.FSTFieldInfo[] fieldInfo = clInfo.getFieldInfo();
        for (int i = 0; i < fieldInfo.length; i++) {
            FSTClazzInfo.FSTFieldInfo fstFieldInfo = fieldInfo[i];
            String dartType = mapToTargetLang(fstFieldInfo.getType(),tm);
            ps.println("  " + dartType + " " + fstFieldInfo.getField().getName() + ";");
        }
        ps.println();
        ps.println("  operator []= ( String field, var val ) {");
        ps.println("    switch (field) {");
        for (int i = 0; i < fieldInfo.length; i++) {
            FSTClazzInfo.FSTFieldInfo fstFieldInfo = fieldInfo[i];
            ps.println("      case '" + fstFieldInfo.getField().getName() + "': "+fstFieldInfo.getField().getName()+" = val; break;");
        }
        ps.println("    }");
        ps.println("  }");
        ps.println();
        ps.println("  operator [] ( String field ) {");
        ps.println("    switch (field) {");
        for (int i = 0; i < fieldInfo.length; i++) {
            FSTClazzInfo.FSTFieldInfo fstFieldInfo = fieldInfo[i];
            ps.println("      case '" + fstFieldInfo.getField().getName() + "': return "+fstFieldInfo.getField().getName()+";");
        }
        ps.println("    }");
        ps.println("  }");
        ps.println();
        ps.println("  List<String> getFields() {");
        ps.println("    return [");
        ps.print("      ");
        for (int i = 0; i < fieldInfo.length; i++) {
            FSTClazzInfo.FSTFieldInfo fstFieldInfo = fieldInfo[i];
            ps.print(" '" + fstFieldInfo.getField().getName() + "',");
        }
        ps.println();
        ps.println("    ];");
        ps.println("  }");
        ps.println("}");
    }

    protected String mapToTargetLang(Class c,DsonTypeMapper tm) {
        if ( c == Object.class ) {
            return "var";
        }
        if ( c.isArray() ) {
            String type = mapToTargetLang(c.getComponentType(), tm);
            if ( "var".equals(type) ) {
                return "List";
            }
            return "List<"+ type +">";
        }
        if ( c.isPrimitive() ) {
            if ( c == float.class || c == float.class ) {
                return "double";
            }
            return "int";
        }
        if ( c == String.class ) {
            return "String";
        }
        if ( Map.class.isAssignableFrom(c) ) {
            return "Map";
        }
        if ( Collection.class.isAssignableFrom(c) ) {
            return "List";
        }
        return tm.getStringForType(c);
    }
    
    public static void main( String arg[]) {
        DsonTypeMapper mapper = new DsonTypeMapper();
        mapper.map("DateTime", Date.class);
        mapper.map("user", DsonDeserializer.UD.class);
        mapper.map("nested", DsonDeserializer.Sel.class);

        new DartDsonGen().generate( "RealLive", System.out,Arrays.asList(DsonDeserializer.UD.class, DsonDeserializer.Sel.class), mapper );

    }
}
