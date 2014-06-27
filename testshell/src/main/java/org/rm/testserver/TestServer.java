package org.rm.testserver;

import com.cedarsoftware.util.DeepEquals;
import org.rm.testserver.protocol.*;
import org.nustaq.serialization.FSTConfiguration;
import org.nustaq.serialization.minbin.MBPrinter;
import io.netty.channel.ChannelHandlerContext;
import org.nustaq.netty2go.NettyWSHttpServer;
import org.nustaq.webserver.WebSocketHttpServer;

import java.io.File;
import java.io.Serializable;

/**
 * Created by ruedi on 27.05.14.
 */
public class TestServer extends WebSocketHttpServer {

    FSTConfiguration conf = FSTConfiguration.createCrossPlatformConfiguration();


    public TestServer(File contentRoot) {
        super(contentRoot);
        conf.registerCrossPlatformClassMappingUseSimpleName(new Meta().getClasses());
    }

    @Override
    public void onOpen(ChannelHandlerContext ctx) {
        sendWSBinaryMessage( ctx, conf.asByteArray(new BasicValues()) );
    }

    Object lastMirror = null;
    @Override
    public void onBinaryMessage(ChannelHandlerContext ctx, byte[] buffer) {
        Object msg = null;
        try {
            msg = conf.asObject(buffer);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        if ( msg instanceof TestRequest) {
            TestRequest req = (TestRequest) msg;
            if ( "BasicVals".equals(req.objectToSend) ) {
                BasicValues testReq = new BasicValues();
                MirrorRequest mreq = new MirrorRequest();
                lastMirror = testReq;
                mreq.toMirror = testReq;
                byte[] b = conf.asByteArray(mreq);
                MBPrinter.printMessage(b,System.out);
                sendWSBinaryMessage(ctx, b);
            } else if ("Pojos".equals(req.objectToSend) ) {
                Person p = new Person("heinz","huber","bla");
                p.addFollower(new Person("Pok", "nachname","nothing"))
                 .addFollower(new Person("Hinz", "xy","nothing"));
                Person p1 = new Person("heinz1","huber1","bla1");
                Person px = new Person("Hinz2", "xy2", "nothing 12");
                p1.addFollower(new Person("Pok2", "nachname2","nothing"))
                  .addFollower(px)
                  .addFollower(px);
                p1.friends = p1.followers; // test refs
                MirrorRequest mreq = new MirrorRequest();
                lastMirror = p;
                mreq.toMirror = p;
                byte[] b = conf.asByteArray(mreq);
                MBPrinter.printMessage(b,System.out);
                sendWSBinaryMessage(ctx, b);
            }
        } else
        if (msg instanceof MirrorRequest) {
            sendWSBinaryMessage(ctx, conf.asByteArray((Serializable) ((MirrorRequest) msg).toMirror));
        } else if ( lastMirror != null ) {
            boolean res = DeepEquals.deepEquals(lastMirror, msg);
            System.out.println("mirror response result:"+res);
            if ( res )
                sendWSBinaryMessage(ctx, conf.asByteArray("success"));
            else
                sendWSBinaryMessage(ctx, conf.asByteArray("failure"));
        } else {
            byte error[] = conf.asByteArray("Error");
            sendWSBinaryMessage(ctx,error);
        }
    }

    public static void main(String[] args) throws Exception {
        int port;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        } else {
            port = 8887;
        }
        new NettyWSHttpServer(port, new TestServer(new File(".") )).run();
    }
}
