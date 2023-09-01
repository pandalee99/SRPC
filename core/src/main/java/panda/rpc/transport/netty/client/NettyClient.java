package panda.rpc.transport.netty.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import panda.rpc.entity.RpcRequest;
import panda.rpc.entity.RpcResponse;
import panda.rpc.enumeration.RpcError;
import panda.rpc.exception.RpcException;
import panda.rpc.factory.SingletonFactory;
import panda.rpc.loadbalancer.LoadBalancer;
import panda.rpc.loadbalancer.RandomLoadBalancer;
import panda.rpc.registry.NacosServiceDiscovery;
import panda.rpc.registry.ServiceDiscovery;
import panda.rpc.serializer.CommonSerializer;
import panda.rpc.transport.RpcClient;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

/**
 * NIO方式消费侧客户端类
 */
public class NettyClient implements RpcClient {

    private static final Logger         logger = LoggerFactory.getLogger(NettyClient.class);
    private static final EventLoopGroup group;
    private static final Bootstrap      bootstrap;

    static {
        group = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class);
    }

    private final ServiceDiscovery serviceDiscovery;
    private final CommonSerializer serializer;

    private final UnprocessedRequests unprocessedRequests;

    public NettyClient() {
        this(DEFAULT_SERIALIZER, new RandomLoadBalancer());
    }

    public NettyClient(LoadBalancer loadBalancer) {
        this(DEFAULT_SERIALIZER, loadBalancer);
    }

    public NettyClient(Integer serializer) {
        //随机轮询策略来进行负载均衡
        this(serializer, new RandomLoadBalancer());
    }

    public NettyClient(Integer serializer, LoadBalancer loadBalancer) {
        //另一个构造方法，表示自定义负载均衡策略

        //注入服务发现
        this.serviceDiscovery = new NacosServiceDiscovery(loadBalancer);
        //序列化器
        this.serializer = CommonSerializer.getByCode(serializer);
        //单例工厂
        this.unprocessedRequests = SingletonFactory.getInstance(UnprocessedRequests.class);
    }

    @Override
    public CompletableFuture<RpcResponse> sendRequest(RpcRequest rpcRequest) {
        if (serializer == null) {
            logger.error("未设置序列化器");
            throw new RpcException(RpcError.SERIALIZER_NOT_FOUND);
        }
        CompletableFuture<RpcResponse> resultFuture = new CompletableFuture<>();
        try {
            InetSocketAddress inetSocketAddress = serviceDiscovery.lookupService(rpcRequest.getInterfaceName());
            Channel channel = ChannelProvider.get(inetSocketAddress, serializer);
            if (!channel.isActive()) {
                group.shutdownGracefully();
                return null;
            }
            unprocessedRequests.put(rpcRequest.getRequestId(), resultFuture);
            channel.writeAndFlush(rpcRequest).addListener((ChannelFutureListener) future1 -> {
                if (future1.isSuccess()) {
                    logger.info(String.format("客户端发送消息: %s", rpcRequest.toString()));
                } else {
                    future1.channel().close();
                    resultFuture.completeExceptionally(future1.cause());
                    logger.error("发送消息时有错误发生: ", future1.cause());
                }
            });
        } catch (InterruptedException e) {
            unprocessedRequests.remove(rpcRequest.getRequestId());
            logger.error(e.getMessage(), e);
            Thread.currentThread().interrupt();
        }
        return resultFuture;
    }

}







/*

在RPC框架中，CompletableFuture类通常被用于异步处理远程过程调用(RPC)的结果。
当客户端发起一个RPC请求时，客户端代码通常会被阻塞，直到远程服务器返回结果。
使用CompletableFuture类，客户端代码可以在发送RPC请求后，立即返回一个CompletableFuture对象，该对象代表了RPC结果的未来值。
当远程服务器返回结果时，服务器将结果封装到一个CompletableFuture对象中，客
户端代码可以使用CompletableFuture的一组方法，等待RPC结果的完成，并在完成后处理结果。
这种方式可以避免客户端代码阻塞等待RPC结果，从而提高代码的执行效率和可扩展性。

除了用于处理RPC结果之外，CompletableFuture类还可以用于客户端和服务器之间的并行处理，
比如并行执行多个RPC请求，然后等待所有请求完成后，合并结果并返回给客户端。

在RPC框架中，CompletableFuture类通常是作为异步编程的基础组件，用于实现异步RPC调用和结果处理，提高系统的性能和可扩展性。

 */