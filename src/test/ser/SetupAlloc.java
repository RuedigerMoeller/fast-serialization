package ser;

import org.nustaq.serialization.FSTConfiguration;

/**
 * Created by ruedi on 07.11.2014.
 */
public class SetupAlloc {

    public static void main(String arg[]) {
        FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();

        long tim = System.currentTimeMillis();

        for ( int i = 0; i < 1000*1000; i++) {
//            conf.getObjectOutput();
            conf.getObjectInput(new byte[10]);
        }
    }

}
