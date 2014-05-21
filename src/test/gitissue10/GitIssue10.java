package gitissue10;

import de.ruedigermoeller.serialization.*;
import de.ruedigermoeller.serialization.util.*;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Created by moelrue on 21.05.2014.
 */
public class GitIssue10 {

    static class TCPServer
    {
        public void main(String argv[]) throws Exception
        {
            String clientSentence;
            String capitalizedSentence;
            ServerSocket welcomeSocket = new ServerSocket(6789);

            while(true)
            {
                Socket connectionSocket = welcomeSocket.accept();
                OutputStream outputStream = connectionSocket.getOutputStream();
                InputStream inputStream = connectionSocket.getInputStream();

                FSTObjectOutput toClient = new FSTObjectOutput(outputStream);
                FSTObjectInput fromClient = new FSTObjectInput(inputStream);

                Object read = fromClient.readObject();
                toClient.writeObject(read);
                toClient.flush();
            }
        }
    }

    static class TCPClient {
        public void main(String argv[]) throws Exception  {
            String sentence;
            String modifiedSentence;
            Socket clientSocket = new Socket("localhost", 6789);
            OutputStream outputStream = clientSocket.getOutputStream();
            InputStream inputStream = clientSocket.getInputStream();

            FSTObjectOutput toSrv = new FSTObjectOutput(outputStream);
            toSrv.writeObject(new HashMap<>(System.getProperties()));
            toSrv.flush();

            FSTObjectInput fromSrv = new FSTObjectInput(inputStream);
            System.out.println("FROM SERVER: " + fromSrv.readObject());

            clientSocket.close();
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
