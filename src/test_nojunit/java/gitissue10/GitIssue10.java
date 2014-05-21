package gitissue10;

import de.ruedigermoeller.serialization.*;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Created by moelrue on 21.05.2014.
 * 
 * Example on how to deal with blocking IO. The trick is to encode/decode from byte arrays and write length of
 * byte array first, so you don't run into a blocking read when deserializing
 * 
 */
public class GitIssue10 {

    static boolean UseStdStreams = false;
    
    static FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();

    protected static Object readObjectFromStream(DataInputStream inputStream) throws IOException, ClassNotFoundException {
        if ( UseStdStreams ) {
            ObjectInputStream in = new ObjectInputStream(inputStream);
            return in.readObject();
        } else {
            int len = inputStream.readInt();
            byte buffer[] = new byte[len]; // this could be reused !
            while (len > 0)
                len -= inputStream.read(buffer, buffer.length - len, len);
            return GitIssue10.conf.getObjectInput(buffer).readObject();
        }
    }

    protected static void writeObjectToStream(DataOutputStream outputStream, Object toWrite) throws IOException {
        if ( UseStdStreams ) {
            ObjectOutputStream out = new ObjectOutputStream(outputStream);
            out.writeObject(toWrite);
            out.flush();
        } else {
            // write object 
            FSTObjectOutput objectOutput = conf.getObjectOutput(); // could also do new with minor perf impact
            // write object to internal buffer
            objectOutput.writeObject(toWrite);
            // write length
            outputStream.writeInt(objectOutput.getWritten());
            // write bytes
            outputStream.write(objectOutput.getBuffer(), 0, objectOutput.getWritten());

            objectOutput.flush(); // return for reuse to conf
        }
    }


    static class TCPServer
    {
        public void main(String argv[]) throws Exception
        {
            String clientSentence;
            String capitalizedSentence;
            ServerSocket welcomeSocket = new ServerSocket(6789);
            Socket connectionSocket = welcomeSocket.accept();
            DataOutputStream outputStream = new DataOutputStream(connectionSocket.getOutputStream());
            DataInputStream inputStream = new DataInputStream(connectionSocket.getInputStream() );

            try {
                while (true) {
                    // read object
                    Object read = readObjectFromStream(inputStream);

                    // write response
                    writeObjectToStream(outputStream, read);
                }
            } catch (EOFException ex) {
                // client terminated
                System.out.println("client terminated");
            }
        }
    }

    static class TCPClient {
        public void main(String argv[]) throws Exception  {

            Socket clientSocket = new Socket("localhost", 6789);

            try {
                DataOutputStream outputStream = new DataOutputStream(clientSocket.getOutputStream());
                DataInputStream inputStream = new DataInputStream(clientSocket.getInputStream());

                while (true) {
                    long tim = System.currentTimeMillis();
                    for (int n = 0; n < 10000; n++) {
                        HashMap toWrite = new HashMap();
                        toWrite.put(1, "Hello");
                        toWrite.put(2, "Data");

                        // write request
                        writeObjectToStream(outputStream, toWrite);
                        // get response
                        Object response = readObjectFromStream(inputStream);
                        if ( ! ((HashMap)response).get(1).equals("Hello") ) {
                            throw new RuntimeException("encoding error");
                        }
                    }
                    System.out.println("time for 10000 req/resp (20000 encodes, 20000 decodes)" + (System.currentTimeMillis() - tim));
                }
            } finally {
                clientSocket.close();
            }

        }
    }

    public static void main(String arg[]) {
        final TCPServer srv = new TCPServer();
        final TCPClient cl = new TCPClient();

        new Thread("server") {
            public void run() {
                try {
                    srv.main(null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();

        new Thread("client") {
            public void run() {
                try {
                    cl.main(null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }
}
