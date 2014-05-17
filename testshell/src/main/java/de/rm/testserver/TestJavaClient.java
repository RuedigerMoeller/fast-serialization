package de.rm.testserver;

import de.ruedigermoeller.serialization.FSTConfiguration;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_10;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;

/**
 * Created by ruedi on 16.05.14.
 */
public class TestJavaClient extends WebSocketClient {

    FSTConfiguration cross = FSTConfiguration.createCrossPlatformConfiguration();

    public TestJavaClient(URI serverUri, Draft draft) {
        super(serverUri, draft);
        cross.registerCrossPlatformClassMapping(TestWSServer.ClassMap);
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        System.out.println("new connection opened "+handshakedata);
    }

    @Override
    public void onMessage(ByteBuffer bytes) {
        byte[] b = new byte[bytes.remaining()];
        bytes.get(b, 0, b.length);
        System.out.println("received message: " + cross.asObject(b));
    }

    @Override
    public void onMessage(String message) {
        System.out.println("received message: " + message);
    }

    @Override
    public void onClose(int i, String s, boolean b) {
        System.out.println("connection closed");
    }

    @Override
    public void onError(Exception ex) {
        System.err.println("an error occured:" + ex);
    }

    public static void main(String[] args) throws URISyntaxException {
        WebSocketClient client = new TestJavaClient(new URI("ws://localhost:8887"), new Draft_10());
        client.connect();
    }
}
