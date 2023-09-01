package panda.rpc.transport.netty.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import panda.rpc.codec.CommonDecoder;
import panda.rpc.codec.CommonEncoder;
import panda.rpc.serializer.CommonSerializer;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * 用于获取 Channel 对象
 */

public class ChannelProvider {

    private static final Logger         logger    = LoggerFactory.getLogger(ChannelProvider.class);
    private static       EventLoopGroup eventLoopGroup;
    private static       Bootstrap      bootstrap = initializeBootstrap();

    private static Map<String, Channel> channels = new ConcurrentHashMap<>();

    public static Channel get(InetSocketAddress inetSocketAddress, CommonSerializer serializer)
            throws InterruptedException {
        String key = inetSocketAddress.toString() + serializer.getCode();
        if (channels.containsKey(key)) {
            Channel channel = channels.get(key);
            if (channels != null && channel.isActive()) {
                return channel;
            } else {
                channels.remove(key);
            }
        }
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) {
                /*自定义序列化编解码器*/
                // RpcResponse -> ByteBuf
                ch.pipeline().addLast(new CommonEncoder(serializer))
                        .addLast(new IdleStateHandler(0, 5, 0, TimeUnit.SECONDS))
                        .addLast(new CommonDecoder())
                        .addLast(new NettyClientHandler());
            }
        });
        Channel channel = null;
        try {
            channel = connect(bootstrap, inetSocketAddress);
        } catch (ExecutionException e) {
            logger.error("连接客户端时有错误发生", e);
            return null;
        }
        channels.put(key, channel);
        logger.info("创造一个Channel" + channel.toString());
        return channel;
    }

    private static Channel connect(Bootstrap bootstrap, InetSocketAddress inetSocketAddress)
            throws ExecutionException, InterruptedException {
        CompletableFuture<Channel> completableFuture = new CompletableFuture<>();
        bootstrap.connect(inetSocketAddress).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                logger.info("客户端连接成功!");
                completableFuture.complete(future.channel());
            } else {
                throw new IllegalStateException();
            }
        });
        return completableFuture.get();
    }

    private static Bootstrap initializeBootstrap() {
        eventLoopGroup = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                //连接的超时时间，超过这个时间还是建立不上的话则代表连接失败
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                //是否开启 TCP 底层心跳机制
                .option(ChannelOption.SO_KEEPALIVE, true)
                //TCP默认开启了 Nagle 算法，该算法的作用是尽可能的发送大数据快，减少网络传输。TCP_NODELAY 参数的作用就是控制是否启用 Nagle 算法。
                .option(ChannelOption.TCP_NODELAY, true);
        return bootstrap;
    }

}










/*
这段代码实现了一个ChannelProvider类，它是一个单例模式，用于提供与服务器建立连接的Netty Channel。
该类维护了一个静态的Bootstrap实例，用于启动客户端连接，也维护了一个静态的Map用于缓存已连接的Channel，避免重复连接。

在get()方法中，首先会根据传入的inetSocketAddress和serializer生成一个唯一的key，用于在缓存Map中查找Channel。
如果Map中已经有该Channel并且该Channel是可用的，直接返回这个Channel。
否则，先将这个Channel从Map中移除，再重新连接服务器，建立新的Channel。
最后，将新建的或者已存在的Channel保存到Map中，并返回该Channel。
Bootstrap是Netty中的一个重要组件，它用于配置Netty客户端或服务器的启动参数，并且可以创建和管理Channel。
在该类中，通过initializeBootstrap()方法初始化了Bootstrap实例，包括线程模型、Channel类型、连接超时时间、心跳机制等。
在connect()方法中，使用CompletableFuture异步方式建立客户端连接，并通过ChannelFutureListener回调函数处理连接结果。
如果连接成功，将返回一个Channel实例，否则返回null。
 */