package minbin.gen;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

import java.util.List;

/**
 * Created by ruedi on 26.05.14.
 */
public class MBGen {


    private void generate() {
        if ( lang == Lang.javascript ) {

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
