package org.nustaq.net;

import org.nustaq.serialization.FSTConfiguration;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by ruedi on 22.08.14.
 *
 * As socket allowing to send/receive serializable objects
 * see ./test/net for an example
 *
 */
public class TCPObjectServer {

    public interface NewClientListener {
        public void connectionAccepted( TCPObjectSocket client );
    }

    ServerSocket welcomeSocket;
    FSTConfiguration conf;
    int port;
    volatile boolean terminated;

    public TCPObjectServer(FSTConfiguration conf, int port) {
        this.conf = conf;
        this.port = port;
    }

    public TCPObjectServer(int port) {
        this.conf = FSTConfiguration.createDefaultConfiguration();
        this.port = port;
    }

    public void start(final NewClientListener listener) throws IOException {
        new Thread("server "+port) {
            public void run() {
                try {
                    welcomeSocket = new ServerSocket(port);
                    int count = 0;
                    while (!terminated) {
                        final Socket connectionSocket = welcomeSocket.accept();
                        new Thread("tcp client "+count++) {
                            public void run() {
                                try {
                                    listener.connectionAccepted(new TCPObjectSocket(connectionSocket,conf));
                                } catch (IOException e) {
                                    dumpException(e);
                                }
                            }
                        }.start();
                    }
                } catch (Exception e) {
                    dumpException(e);
                } finally {
                    setTerminated(true);
                }
            }
        }.start();
    }

    protected void dumpException(Throwable th) {
        th.printStackTrace();
    }

    public void setTerminated(boolean terminated) {
        this.terminated = terminated;
    }

    public boolean isTerminated() {
        return terminated;
    }
}
