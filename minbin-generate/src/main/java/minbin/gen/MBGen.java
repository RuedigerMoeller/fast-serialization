package minbin.gen;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.lukehutch.fastclasspathscanner.FastClasspathScanner;
import org.nustaq.kontraktor.Actor;
import org.nustaq.kontraktor.Callback;
import org.nustaq.kontraktor.Future;
import org.nustaq.kontraktor.annotations.CallerSideMethod;
import org.nustaq.kontraktor.annotations.GenRemote;
import org.nustaq.serialization.FSTConfiguration;
import de.ruedigermoeller.template.TemplateExecutor;
import org.nustaq.serialization.minbin.GenMeta;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Created by ruedi on 26.05.14.
 */
public class MBGen {

    FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();
    HashSet<String> clazzSet = new HashSet<String>();
    HashMap<Class, List<MsgInfo>> infoMap = new HashMap<Class, List<MsgInfo>>();

    private void addTopLevelClass(String clazzName) throws ClassNotFoundException, IllegalAccessException, InstantiationException {

	    conf.setForceSerializable(true);

        Class c = Class.forName(clazzName);
        try {
            if ( Actor.class.isAssignableFrom(c) ) {
//                clazzSet.add(c.getName());
                prepareActorMeta(c);
            } else {
                GenMeta meta = (GenMeta) c.newInstance();
                List<Class> clazz = meta.getClasses();
                for (int i = 0; i < clazz.size(); i++) {
                    Class aClass = clazz.get(i);
	                addClz(clazzSet, aClass, infoMap);
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

    }

	private void generate(String outFile) throws ClassNotFoundException {
        System.out.println("generating to "+new File(outFile).getAbsolutePath());
		GenContext ctx = new GenContext();
		genClzList(outFile, new ArrayList<String>(clazzSet), ctx, infoMap, "./js/js.jsp");
	}

	private void addClz(Set<String> clazzSet, Class aClass, HashMap<Class, List<MsgInfo>> infoMap) {
		if ( ! clazzSet.contains(aClass.getName()) ) {
			if ( aClass.getName().startsWith("java.") || aClass.getName().startsWith("javax.") )
				return;
			if (Actor.class.isAssignableFrom(aClass)) {
				prepareActorMeta(aClass);
			} else
				clazzSet.add(aClass.getName());
		}
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

	private void prepareActorMeta(Class c) {
	    clazzSet.add(c.getName());
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
				    if (shouldAdd(parameterType))
				    {
					    addClz(clazzSet, parameterType, infoMap);
				    }
			    }
			    if ( Future.class.isAssignableFrom( method.getReturnType() ) ) {
				    Type genericReturnType = method.getGenericReturnType();
				    if (genericReturnType instanceof ParameterizedType) {
						ParameterizedType pm = (ParameterizedType) genericReturnType;
					    Type[] actualTypeArguments = pm.getActualTypeArguments();
					    Type clz = actualTypeArguments[0];
					    if ( actualTypeArguments.length > 0 && clz instanceof Class
						     && shouldAdd((Class<?>) clz)) {
						    addClz(clazzSet, (Class) clz, infoMap);
					    }
				    }
			    }
			    System.out.println("method:"+method);
		    }
	    }

        infoMap.put(c, methodInfos);
    }

	private boolean shouldAdd(Class<?> parameterType) {
		return ! Callback.class.isAssignableFrom(parameterType) &&
			 ! parameterType.isPrimitive() &&
			 ! (parameterType.isArray() && parameterType.getComponentType().isPrimitive()) &&
			 ! String.class.isAssignableFrom(parameterType) &&
	         ! parameterType.isArray() &&
//			 Serializable.class.isAssignableFrom(parameterType) &&
//			 ! Actor.class.isAssignableFrom(parameterType) &&
			 ! Number.class.isAssignableFrom(parameterType);
	}

	public static enum Lang {
        javascript,
        dart
    }

    @Parameter( names={"-lang", "-l" }, description = "target language javascript|dart" )
    Lang lang = Lang.javascript;

    @Parameter( names={"-class", "-c"}, description = "class containing generation description (must implement GenMeta) " )
    String clazz = null; //"org.rm.testserver.protocol.Meta";

    @Parameter( names={"-f"}, description = "file/directory to addTopLevelClass to" )
    String out;

    @Parameter( names={"-p"}, description = "',' separated list of whitelist packages" )
    String pack;

    public static void main(String arg[]) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        MBGen gen = new MBGen();
	    JCommander jCommander = new JCommander(gen, arg);
	    if ( (gen.pack == null && gen.clazz == null) || gen.out == null) {
		    jCommander.usage();
		    System.exit(-1);
	    }
        // fixme check args
	    if ( gen.clazz == null ) {
			MBGen mbGen = new MBGen();
			mbGen.lang = gen.lang;
			mbGen.out = gen.out;
		    System.out.println("no class arg given ... scanning classpath for @GenRemote. whitelist:"+gen.pack);
		    new FastClasspathScanner( gen.pack.split(",") )
				.matchClassesWithAnnotation( GenRemote.class, (clazz) -> {
					try {
						mbGen.addTopLevelClass(clazz.getName());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}).scan();

            if (mbGen.clazzSet.size()>0) {
                mbGen.generate(gen.out);
            } else
                System.out.println("no @GenRemote classes found in given packages");
        }
        //gen.addTopLevelClass("org.rm.testserver.protocol.Meta","../testshell/src/main/javascript/js/model.js");

    }


}
