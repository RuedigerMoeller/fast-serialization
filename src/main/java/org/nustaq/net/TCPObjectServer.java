/*
 * Copyright 2014 Ruediger Moeller.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
 * see ./test/net for an example.
 *
 * Note that by providing a Json configuration, it can be used cross language
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
