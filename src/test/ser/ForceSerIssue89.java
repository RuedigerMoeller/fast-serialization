package ser;

import org.nustaq.serialization.FSTConfiguration;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by ruedi on 29/10/15.
 */
public class ForceSerIssue89 {

    public static class JettyInfo {
        private final InetAddress ipAddress;
        private final Integer port;
        private final boolean isCoordinator;
        private final String controllerId;

        public JettyInfo(final InetAddress ipAddress, final Integer port, final boolean isCoordinator, final String controllerId) {
            this.ipAddress = ipAddress;
            this.port = port;
            this.isCoordinator = isCoordinator;
            this.controllerId = controllerId;
        }

        //getters for all fields
    }

    public static void main(String[] args) throws UnknownHostException {
        FSTConfiguration conf = FSTConfiguration.createFastBinaryConfiguration().setForceSerializable(true);
        JettyInfo poaksd1 = new JettyInfo(InetAddress.getLocalHost(), 8888, true, "poaksd");
        Object poaksd = conf.asObject(conf.asByteArray(poaksd1));
        System.out.println("POK");
    }

}
