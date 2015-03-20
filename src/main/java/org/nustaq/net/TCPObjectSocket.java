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
 */
public class TCPObjectSocket {

    public static int BUFFER_SIZE = 64000;

    InputStream in;
    OutputStream out;
    FSTConfiguration conf;
    Socket socket;
    Exception lastErr;
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
        this.out = new BufferedOutputStream(socket.getOutputStream(), BUFFER_SIZE);
        this.in  = new BufferedInputStream(socket.getInputStream(), BUFFER_SIZE);
        this.conf = conf;
    }

    public Exception getLastErr() {
        return lastErr;
    }

    public boolean isStopped() {
        return stopped;
    }

    public Object readObject() throws Exception {
        try {
            while ( !readLock.compareAndSet(false,true) );
            int ch1 = (in.read() + 256) & 0xff;
            int ch2 = (in.read()+ 256) & 0xff;
            int ch3 = (in.read() + 256) & 0xff;
            int ch4 = (in.read() + 256) & 0xff;
            int len = (ch4 << 24) + (ch3 << 16) + (ch2 << 8) + (ch1 << 0);
            if ( len <= 0 )
                throw new EOFException("client closed");
            int orglen = len;
            byte buffer[] = new byte[len]; // this could be reused !
            while (len > 0)
                len -= in.read(buffer, buffer.length - len, len);
            try {
                return conf.getObjectInput(buffer).readObject();
            } catch (Exception e) {
                System.out.println("orglen: "+orglen+" "+new String(buffer,0));
                final Object retry = conf.getObjectInput(buffer).readObject();
                throw e;
            }
        } finally {
            readLock.set(false);
        }
    }

    public void writeObject(Object toWrite) throws Exception {
        try {
            while ( !writeLock.compareAndSet(false,true) );
            FSTObjectOutput objectOutput = conf.getObjectOutput(); // could also do new with minor perf impact
            objectOutput.writeObject(toWrite);

            int written = objectOutput.getWritten();
            out.write((written >>> 0) & 0xFF);
            out.write((written >>> 8) & 0xFF);
            out.write((written >>> 16) & 0xFF);
            out.write((written >>> 24) & 0xFF);

            out.write(objectOutput.getBuffer(), 0, written);
            objectOutput.flush();
        } finally {
            writeLock.set(false);
        }
    }

    public void flush() throws IOException {
        out.flush();
    }

    public void setLastError(Exception ex) {
        stopped = true;
        lastErr = ex;
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
}
