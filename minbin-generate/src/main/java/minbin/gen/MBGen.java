package minbin.gen;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import de.ruedigermoeller.serialization.FSTClazzInfo;
import de.ruedigermoeller.serialization.FSTClazzInfoRegistry;
import de.ruedigermoeller.serialization.FSTConfiguration;
import de.ruedigermoeller.template.TemplateExecutor;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by ruedi on 26.05.14.
 */
public class MBGen {

    private void generate(String clazzName, String outFile) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();

        System.out.println("generating to "+new File(outFile).getAbsolutePath());

        Class c = Class.forName(clazzName);
        GenMeta meta = (GenMeta) c.newInstance();
        ArrayList<String> list = new ArrayList<String>();
        Class clazz[] = meta.getClasses();
        for (int i = 0; i < clazz.length; i++) {
            Class aClass = clazz[i];
            FSTClazzInfoRegistry.addAllReferencedClasses(aClass,list,c.getPackage().getName());
        }

        for (int i = 0; i < list.size(); i++) {
            String s = list.get(i);
            final String pack = c.getPackage().getName();
            if (!s.startsWith(pack)) {
                list.remove(i); i--;
            }
        }

        list = new ArrayList<String>(new HashSet<String>(list));

        GenContext ctx = new GenContext();

        FSTClazzInfo infos[] = new FSTClazzInfo[list.size()];
        for (int i = 0; i < infos.length; i++) {
            infos[i] = conf.getClassInfo(Class.forName(list.get(i)));
        }
        ctx.clazzInfos = infos;
        if ( lang == Lang.javascript ) {
            TemplateExecutor.Run(outFile,"./src/main/resources/js/js.jsp",ctx);
        }

    }












    public static enum Lang {
        javascript,
        dart
    }

    @Parameter( names={"-lang", "-l" }, description = "target language javascript|dart" )
    Lang lang;

    @Parameter( names={"-classes -cl"}, description = "list of classes to generate" )
    List classes;

    @Parameter( names={"-f"}, description = "file/directory to generate to" )
    String out;

    public static void main(String arg[]) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        MBGen gen = new MBGen();
        new JCommander(gen,arg);
        // fixme check args
        gen.generate("de.rm.testserver.protocol.Meta","../testshell/src/main/javascript/js/model.js");

    }


}
