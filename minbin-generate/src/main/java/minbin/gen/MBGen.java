package minbin.gen;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import de.rm.testserver.protocol.BasicValues;
import de.rm.testserver.protocol.Meta;
import de.ruedigermoeller.serialization.FSTConfiguration;
import de.ruedigermoeller.template.TemplateExecutor;

import java.util.List;

/**
 * Created by ruedi on 26.05.14.
 */
public class MBGen {


    private void generate() {
        FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();
        GenContext ctx = new GenContext();
        ctx.clazz = conf.getClassInfo(BasicValues.class);
        if ( lang == Lang.javascript ) {
            TemplateExecutor.Run("./src/main/resources/js/js.jsp",ctx);
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

    public static void main(String arg[]) {
        MBGen gen = new MBGen();
        new JCommander(gen,arg);
        // fixme check args
        gen.generate();

    }


}
