package panda.rpc.transport.netty.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import panda.rpc.codec.CommonDecoder;
import panda.rpc.codec.CommonEncoder;
import panda.rpc.hook.ShutdownHook;
import panda.rpc.provider.ServiceProviderImpl;
import panda.rpc.registry.NacosServiceRegistry;
import panda.rpc.serializer.CommonSerializer;
import panda.rpc.transport.AbstractRpcServer;

import java.util.concurrent.TimeUnit;

/**
 * NIO方式服务提供侧
 */
public class NettyServer extends AbstractRpcServer {
    //同时也继承了serviceRegistry和serviceProvider

    private final CommonSerializer serializer;

    public NettyServer(String host, int port) {
        this(host, port, DEFAULT_SERIALIZER);
    }

    public NettyServer(String host, int port, Integer serializer) {
        this.host = host;
        this.port = port;
        serviceRegistry = new NacosServiceRegistry();
        serviceProvider = new ServiceProviderImpl();
        this.serializer = CommonSerializer.getByCode(serializer);
        scanServices();
    }

    @Override
    public void start() {
        //使用静态方法代替构造方法
        ShutdownHook.getShutdownHook().addClearAllHook();
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            //固定写法
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .option(ChannelOption.SO_BACKLOG, 256)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new IdleStateHandler(30, 0, 0, TimeUnit.SECONDS))
                                    .addLast(new CommonEncoder(serializer))
                                    .addLast(new CommonDecoder())
                                    .addLast(new NettyServerHandler());
                        }
                    });
            ChannelFuture future = serverBootstrap.bind(host, port).sync();
            future.channel().closeFuture().sync();

        } catch (InterruptedException e) {
            logger.error("启动服务器时有错误发生: ", e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

}












/*
在 Netty 中，LoggingHandler 是一个用于打印日志的处理器。
它将日志消息记录到由 slf4j 提供的日志框架中，
并允许您根据需要配置日志级别，以便在不同的情况下记录不同级别的日志消息。
在 Netty 的 ServerBootstrap 和 Bootstrap 中，它通常被用作 ChannelPipeline 中的一个处理器。
在服务器端，它可以用来记录连接事件、断开连接事件等；在客户端，它可以用来记录连接服务器的过程。
通过使用 LoggingHandler，您可以方便地了解您的应用程序正在执行的操作，以及在出现问题时快速定位错误。
 */
/*
在Netty中，ServerChannel和SocketChannel都是Channel的子接口。

ServerChannel是一个监听传入连接的Channel，它是用来接受客户端的连接请求的，
并且对于每一个客户端连接请求，ServerChannel都会为其创建一个新的SocketChannel实例，
从而使得客户端可以通过这个新的SocketChannel与服务端进行通信。
在Netty中，ServerChannel通常被用来实现服务端的网络层，例如TCP/IP协议的服务端。

而SocketChannel则代表了一个已经建立的TCP连接，它用于在客户端和服务端之间进行通信。
在Netty中，SocketChannel通常被用来实现客户端的网络层，例如TCP/IP协议的客户端。

总的来说，ServerChannel主要负责监听客户端的连接请求，而SocketChannel则负责实际的数据传输。

.option(ChannelOption.SO_BACKLOG, 128)指定了ServerSocketChannel的连接队列大小为128。

.childOption(ChannelOption.TCP_NODELAY, true)
指定了SocketChannel的TCP_NODELAY选项为true，表示禁用Nagle算法。


 */
/*
在 Netty 中，IdleStateHandler 是一个用于处理空闲状态的处理器。
它可以在 Channel 上检测特定类型的空闲时间，并在这些时间段内未发生读取、写入或读写事件时触发相应的事件。
常用的空闲状态类型有三种：READER_IDLE，WRITER_IDLE 和 ALL_IDLE。

IdleStateHandler 可以用于实现心跳机制，可以通过配置空闲时间间隔和触发事件来判断是否需要发送心跳包。
它可以被添加到 Netty 的 ChannelPipeline 中，以监视 Channel 上的空闲事件，
以便您可以采取适当的措施，例如关闭连接或发送心跳消息。
 */