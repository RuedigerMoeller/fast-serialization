package de.rm.testserver;

import de.rm.testserver.protocol.BasicValues;
import de.rm.testserver.protocol.MirrorRequest;
import de.rm.testserver.protocol.Person;
import de.rm.testserver.protocol.TestRequest;
import de.ruedigermoeller.serialization.FSTConfiguration;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.CharsetUtil;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import static io.netty.handler.codec.http.HttpHeaders.Names.*;
import static io.netty.handler.codec.http.HttpHeaders.*;
import static io.netty.handler.codec.http.HttpMethod.*;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.*;


public class TestWSServer {

    public static String ClassMap[][] = new String[][] {
            { "person", Person.class.getName() },
            { "basicVals", BasicValues.class.getName() },
            { "mirror", MirrorRequest.class.getName() },
            { "testReq", TestRequest.class.getName() },
    };

    private final int port;
    FSTConfiguration conf;

    public TestWSServer(int port) {
        this.port = port;
        conf = FSTConfiguration.createCrossPlatformConfiguration();
        conf.registerCrossPlatformClassMapping( ClassMap );
    }

    public void run() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new WebSocketServerInitializer());

            Channel ch = b.bind(port).sync().channel();
            System.out.println("Web socket server started at port " + port + '.');
            System.out.println("Open your browser and navigate to http://localhost:" + port + '/');

            ch.closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public class WebSocketServerInitializer extends ChannelInitializer<SocketChannel> {
        @Override
        public void initChannel(SocketChannel ch) throws Exception {
            ChannelPipeline pipeline = ch.pipeline();
            pipeline.addLast("codec-http", new HttpServerCodec());
            pipeline.addLast("aggregator", new HttpObjectAggregator(65536));
            WebSocketServerHandler handler = new WebSocketServerHandler();
            pipeline.addLast("handler", handler);
        }
    }

    private static final Logger logger = Logger.getLogger(WebSocketServerHandler.class.getName());
    private static final String WEBSOCKET_PATH = "/websocket";

    public class WebSocketServerHandler extends SimpleChannelInboundHandler<Object> {

        private WebSocketServerHandshaker handshaker;

        @Override
        public void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
            if (msg instanceof FullHttpRequest) {
                handleHttpRequest(ctx, (FullHttpRequest) msg);
            } else
            if (msg instanceof WebSocketFrame) {
                handleWebSocketFrame(ctx, (WebSocketFrame) msg);
            }
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            ctx.flush();
        }

        private void handleHttpRequest(final ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
            // Handle a bad request.
            if (!req.getDecoderResult().isSuccess()) {
                sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST));
                return;
            }

            // Allow only GET methods.
            if (req.getMethod() != GET) {
                sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN));
                return;
            }

            // Send the demo page and favicon.ico
            if ("/".equals(req.getUri())) {
                ByteBuf content = WebSocketServerIndexPage.getContent(getWebSocketLocation(req));
                FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, OK, content);

                res.headers().set(CONTENT_TYPE, "text/html; charset=UTF-8");
                setContentLength(res, content.readableBytes());

                sendHttpResponse(ctx, req, res);
                return;
            }
            if ("/favicon.ico".equals(req.getUri())) {
                FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, NOT_FOUND);
                sendHttpResponse(ctx, req, res);
                return;
            }

            // Handshake
            WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
                    getWebSocketLocation(req), null, false);
            handshaker = wsFactory.newHandshaker(req);
            if (handshaker == null) {
                WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
            } else {
                handshaker.handshake(ctx.channel(), req).addListener(
                    new ChannelFutureListener() {
                        public void operationComplete(ChannelFuture future) {
                            onOpen(ctx.channel());
                        }
                    }
                );
            }
        }

        private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
            // Check for closing frame
            if (frame instanceof CloseWebSocketFrame) {
                handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
                onClose(this);
                return;
            }
            if (frame instanceof PingWebSocketFrame) {
                ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
                return;
            }
            if (frame instanceof BinaryWebSocketFrame) {
                ByteBuf rawMessage = frame.content();
                int size = rawMessage.readableBytes();
                byte[] buffer = new byte[size];
                rawMessage.readBytes(buffer);
                onMessage(ctx.channel(), buffer);
                return;
            }
            if (!(frame instanceof TextWebSocketFrame)) {
                throw new UnsupportedOperationException(String.format("%s frame types not supported", frame.getClass()
                        .getName()));
            }

            // Send the uppercase string back.
            String request = ((TextWebSocketFrame) frame).text();
            if (logger.isLoggable(Level.FINE)) {
                logger.fine(String.format("%s received %s", ctx.channel(), request));
            }
            ctx.channel().write(new TextWebSocketFrame(request.toUpperCase()));
        }

        private void sendHttpResponse(
                ChannelHandlerContext ctx, FullHttpRequest req, FullHttpResponse res) {
            // Generate an error page if response getStatus code is not OK (200).
            if (res.getStatus().code() != 200) {
                ByteBuf buf = Unpooled.copiedBuffer(res.getStatus().toString(), CharsetUtil.UTF_8);
                res.content().writeBytes(buf);
                buf.release();
                setContentLength(res, res.content().readableBytes());
            }

            // Send the response and close the connection if necessary.
            ChannelFuture f = ctx.channel().writeAndFlush(res);
            if (!isKeepAlive(req) || res.getStatus().code() != 200) {
                f.addListener(ChannelFutureListener.CLOSE);
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            cause.printStackTrace();
            ctx.close();
        }

        private String getWebSocketLocation(FullHttpRequest req) {
            return "ws://" + req.headers().get(HOST) + WEBSOCKET_PATH;
        }
    }

    public void onOpen(final Channel webSocket) {
        System.out.println("onOpen");
        webSocket.writeAndFlush(new BinaryWebSocketFrame(Unpooled.wrappedBuffer(conf.asByteArray(new BasicValues()))));

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

    public void onClose(WebSocketServerHandler webSocketServerHandler) {
        System.out.println("onClose");
    }

    public void onMessage(Channel conn, byte[] b) {
        System.out.println("onMessage "+conn);
        Object msg = null;
        try {
            msg = conf.asObject(b);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        if (msg instanceof MirrorRequest) {
            conn.writeAndFlush(new BinaryWebSocketFrame(Unpooled.wrappedBuffer(conf.asByteArray((Serializable) msg))));
        } else {
            byte error[] = conf.asByteArray("Error");
            conn.writeAndFlush(new BinaryWebSocketFrame(Unpooled.wrappedBuffer(error)));
        }
    }

//    @Override
//    public void onMessage(WebSocket webSocket, String s) {
//        System.out.println("onMessage "+s);
//    }
//
//    @Override
//    public void onError(WebSocket webSocket, Exception e) {
//        System.out.println("onError");
//    }

    public static void main(String[] args) throws Exception {
        int port;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        } else {
            port = 8887;
        }
        new TestWSServer(port).run();
    }

}