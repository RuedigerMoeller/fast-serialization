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
import org.nustaq.serialization.FSTObjectOutput;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by ruedi on 22.08.14.
 *
 * As socket allowing to send/receive serializable objects
 * see ./test/net for an example
 *
 * Note that by providing a Json configuration, it can be used cross language
 *
 */
public class TCPObjectSocket {

    public static int BUFFER_SIZE = 512_000;

    InputStream in;
    OutputStream out;
    FSTConfiguration conf;
    Socket socket;
    Throwable lastErr;
    boolean stopped;

    AtomicBoolean readLock = new AtomicBoolean(false);
    AtomicBoolean writeLock = new AtomicBoolean(false);

    public TCPObjectSocket(String host, int port) throws IOException {
        this(new Socket(host, port), FSTConfiguration.createDefaultConfiguration());
    }

    public TCPObjectSocket(String host, int port, FSTConfiguration conf) throws IOException {
        this(new Socket(host, port),conf);
    }

    public TCPObjectSocket( Socket socket, FSTConfiguration conf) throws IOException {
        this.socket = socket;
//        socket.setSoLinger(true,0);
        this.out = new BufferedOutputStream(socket.getOutputStream(), BUFFER_SIZE);
        this.in  = new BufferedInputStream(socket.getInputStream(), BUFFER_SIZE);
        this.conf = conf;
    }

    public boolean isStopped() {
        return stopped;
    }

    public boolean isClosed() {
        return socket.isClosed();
    }

    /**
     * enables reading raw bytes from socket
     * @return
     */
    public InputStream getIn() {
        return in;
    }

    public Object readObject() throws Exception {
        try {
            while ( !readLock.compareAndSet(false,true) );

            return conf.decodeFromStream(in);

        } finally {
            readLock.set(false);
        }
    }

    public void writeObject(Object toWrite) throws Exception {
        try {
            while ( !writeLock.compareAndSet(false,true) );
            conf.encodeToStream(out,toWrite);
        } finally {
            writeLock.set(false);
        }
    }

    public void flush() throws IOException {
        out.flush();
    }

    public void setLastError(Throwable ex) {
        stopped = true;
        lastErr = ex;
    }

    public Throwable getLastError() {
        return lastErr;
    }

    public void close() throws IOException {
        flush();
        socket.close();
    }

    public Socket getSocket() {
        return socket;
    }

    public FSTConfiguration getConf() {
        return conf;
    }

    public void setConf(FSTConfiguration conf) {
        this.conf = conf;
    }

}
