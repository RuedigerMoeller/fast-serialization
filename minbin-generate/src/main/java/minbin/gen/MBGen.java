package minbin.gen;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import org.nustaq.kontraktor.Actor;
import org.nustaq.kontraktor.Callback;
import org.nustaq.kontraktor.Future;
import org.nustaq.kontraktor.annotations.CallerSideMethod;
import org.nustaq.serialization.FSTConfiguration;
import de.ruedigermoeller.template.TemplateExecutor;
import org.nustaq.serialization.minbin.GenMeta;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Created by ruedi on 26.05.14.
 */
public class MBGen {

    FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();

    private void generate(String clazzName, String outFile) throws ClassNotFoundException, IllegalAccessException, InstantiationException {

	    conf.setForceSerializable(true);
        System.out.println("generating to "+new File(outFile).getAbsolutePath());

        Class c = Class.forName(clazzName);
        HashSet<String> clazzSet = new HashSet<String>();
        HashMap<Class, List<MsgInfo>> infoMap = new HashMap<Class, List<MsgInfo>>();
        try {
            Object o = c.newInstance();
            if ( o instanceof Actor ) {
//                clazzSet.add(c.getName());
                prepareActorMeta(c, clazzSet, infoMap);
            } else {
                GenMeta meta = (GenMeta) o;
                List<Class> clazz = meta.getClasses();
                for (int i = 0; i < clazz.size(); i++) {
                    Class aClass = clazz.get(i);
                    clazzSet.add(aClass.getName());
                }
            }
        } catch (ClassCastException ex) {
            System.out.println("Expected a class implementing GenMeta interface or be an actor class");
            System.exit(-1);
        }
//        for (int i = 0; i < clazz.length; i++) {
//            Class aClass = clazz[i];
//            FSTClazzInfoRegistry.addAllReferencedClasses(aClass,list,c.getPackage().getName());
//        }

//        for (int i = 0; i < list.size(); i++) {
//            String s = list.get(i);
//            final String pack = c.getPackage().getName();
//            if (!s.startsWith(pack)) {
//                list.remove(i); i--;
//            }
//        }

        GenContext ctx = new GenContext();
	    genClzList(outFile, new ArrayList<String>(clazzSet), ctx, infoMap, "./js/js.jsp");
    }

	private void genClzList(String outFile, ArrayList<String> finallist, GenContext ctx, HashMap<Class, List<MsgInfo>> infoMap, String templateFile) throws ClassNotFoundException {
		GenClazzInfo infos[] = new GenClazzInfo[finallist.size()];
		for (int i = 0; i < infos.length; i++) {
		    infos[i] = new GenClazzInfo( conf.getClassInfo(Class.forName(finallist.get(i))) );
            infos[i].setMsgs(infoMap.get(infos[i].getClzInfo().getClazz()));
		    if ( infos[i] != null )
		        System.out.println("generating clz "+finallist.get(i));
		}
		ctx.clazzInfos = infos;
		if ( lang == Lang.javascript ) {
			TemplateExecutor.Run(outFile, templateFile, ctx);
		}
	}

	private void prepareActorMeta(Class c, Set<String> addClazzezHere, HashMap<Class,List<MsgInfo>> map) {
	    addClazzezHere.add(c.getName());
	    Method m[] = c.getMethods();
		ArrayList<MsgInfo> methodInfos = new ArrayList<MsgInfo>();
	    for (int i = 0; i < m.length; i++) {
		    Method method = m[i];
		    if (Modifier.isPublic(method.getModifiers()) &&
			    method.getAnnotation(CallerSideMethod.class) == null &&
			    ( method.getReturnType() == void.class || Future.class.isAssignableFrom(method.getReturnType()) ) &&
				method.getDeclaringClass() != Object.class &&
                !Modifier.isStatic(method.getModifiers())
			) {
			    Class<?>[] parameterTypes = method.getParameterTypes();
                final java.lang.reflect.Parameter[] parameters = method.getParameters();
                methodInfos.add(new MsgInfo(parameterTypes,method.getName(),method.getReturnType().getSimpleName(),parameters));
			    for (int j = 0; j < parameterTypes.length; j++) {
				    Class<?> parameterType = parameterTypes[j];
                    if ( Actor.class.isAssignableFrom(parameterType) && !addClazzezHere.contains(parameterType.getName()) ) {
                        prepareActorMeta(parameterType, addClazzezHere,map);
                    }
				    if ( ! Callback.class.isAssignableFrom(parameterType) &&
					     ! parameterType.isPrimitive() &&
					     ! (parameterType.isArray() && parameterType.getComponentType().isPrimitive()) &&
					     ! String.class.isAssignableFrom(parameterType) &&
					     Serializable.class.isAssignableFrom(parameterType) &&
					     ! Actor.class.isAssignableFrom(parameterType) &&
					     ! Number.class.isAssignableFrom(parameterType) )
				    {
						addClazzezHere.add(parameterType.getName());
				    }
			    }
			    System.out.println("method:"+method);
		    }
	    }

        map.put(c, methodInfos);
    }

    public static enum Lang {
        javascript,
        dart
    }

    @Parameter( names={"-lang", "-l" }, description = "target language javascript|dart" )
    Lang lang = Lang.javascript;

    @Parameter( names={"-class", "-c"}, description = "class containing generation description (must implement GenMeta) " )
    String clazz = "org.rm.testserver.protocol.Meta";

    @Parameter( names={"-f"}, description = "file/directory to generate to" )
    String out;

    public static void main(String arg[]) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        MBGen gen = new MBGen();
        new JCommander(gen,arg);
        // fixme check args
        gen.generate(gen.clazz,gen.out);
        //gen.generate("org.rm.testserver.protocol.Meta","../testshell/src/main/javascript/js/model.js");

    }


}
