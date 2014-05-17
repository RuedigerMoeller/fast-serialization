package de.rm.testserver;

import de.rm.testserver.protocol.BasicValues;
import de.rm.testserver.protocol.MirrorRequest;
import de.rm.testserver.protocol.TestRequest;
import de.ruedigermoeller.serialization.FSTConfiguration;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.HashMap;

public class TestWSServer extends WebSocketServer {

    public static String ClassMap[][] = new String[][] {
        { "basicVals", BasicValues.class.getName() },
        { "mirror", MirrorRequest.class.getName() },
        { "testReq", TestRequest.class.getName() },
    };

    FSTConfiguration conf;

    public TestWSServer(InetSocketAddress address) {
        super(address);
        conf = FSTConfiguration.createCrossPlatformConfiguration();
        conf.registerCrossPlatformClassMapping( ClassMap );
    }

    @Override
    public void onOpen(final WebSocket webSocket, ClientHandshake clientHandshake) {
        System.out.println("onOpen");
        final HashMap map = new HashMap();
        map.put("Hello", "Me");
        webSocket.send(conf.asByteArray(map));

//        final Thread thread = new Thread() {
//            public void run() {
//                while( true ) {
//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                    webSocket.send(conf.asByteArray(map));
//                }
//            }
//        };
//        thread.start();
    }

    @Override
    public void onClose(WebSocket webSocket, int i, String s, boolean b) {
        System.out.println("onClose");
    }

    @Override
    public void onMessage(WebSocket conn, ByteBuffer message) {
        byte[] b = new byte[message.remaining()];
        message.get(b, 0, b.length);
        try {
            Object msg = conf.asObject(b);
            if (msg instanceof MirrorRequest) {
                conn.send(conf.asByteArray((Serializable) msg));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            conn.send("Error".getBytes());
        }
    }

    @Override
    public void onMessage(WebSocket webSocket, String s) {
        System.out.println("onMessage "+s);
    }

    @Override
    public void onError(WebSocket webSocket, Exception e) {
        System.out.println("onError");
    }

    public static void main( String arg[] ) {
        String host = "localhost";
        int port = 8887;
        TestWSServer server = new TestWSServer(new InetSocketAddress(host,port));
        server.run();
    }
}