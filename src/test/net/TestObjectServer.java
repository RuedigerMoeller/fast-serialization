package net;

import junit.framework.Assert;
import org.junit.Test;
import org.nustaq.net.TCPObjectServer;
import org.nustaq.net.TCPObjectSocket;

import java.io.EOFException;
import java.io.IOException;
import java.util.HashMap;

/**
 * Created by ruedi on 22.08.14.
 */
public class TestObjectServer {

    public static final int PORT = 7777;

    public void server() throws IOException {
        TCPObjectServer server = new TCPObjectServer(PORT);
        server.start( new TCPObjectServer.NewClientListener() {
            // for each client an own thread is created by default.
            // so no need to create a new Thread for the client here
            @Override
            public void connectionAccepted(TCPObjectSocket client) {
                try {
                    while( true ) {
                        Object request = client.readObject();
                        if ( request == null )
                            return; // connection closed
                        client.writeObject("Hello, I received: ");
                        client.writeObject(request);
                        client.flush();
                    }
                } catch (EOFException eof) {
                    //e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    volatile boolean ok = false;
    public void client() throws Exception {
        TCPObjectSocket socket = new TCPObjectSocket("localhost", PORT);

        // send request
        HashMap toWrite = new HashMap();
        toWrite.put("Greetings form year ", 2014 );
        toWrite.put("Random ", Math.random() );
        socket.writeObject(toWrite);
        socket.flush();                             // <== important, else nothing happens !

        // await 2 responses
        System.out.println(socket.readObject());
        System.out.println(socket.readObject());

        // done, close
        socket.close();
        ok = true;
    }

    @Test
    public void test() throws Exception {
        server();
        Thread.sleep(1000);
        client();
        Thread.sleep(2000);
        Assert.assertTrue(ok);
    }
}
