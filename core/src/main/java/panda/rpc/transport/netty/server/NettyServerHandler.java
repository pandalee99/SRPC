package panda.rpc.transport.netty.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import panda.rpc.entity.RpcRequest;
import panda.rpc.entity.RpcResponse;
import panda.rpc.factory.SingletonFactory;
import panda.rpc.handler.RequestHandler;

/**
 * Netty中处理RpcRequest的Handler
 */
public class NettyServerHandler extends SimpleChannelInboundHandler<RpcRequest> {

    private static final Logger logger = LoggerFactory.getLogger(NettyServerHandler.class);
    private final RequestHandler requestHandler;

    public NettyServerHandler() {
        this.requestHandler = SingletonFactory.getInstance(RequestHandler.class);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest msg) throws Exception {
        try {
            if(msg.getHeartBeat()) {
                logger.info("接收到客户端心跳包...");
                return;
            }
            logger.info("服务器接收到请求: {}", msg);
            Object result = requestHandler.handle(msg);
            if (ctx.channel().isActive() && ctx.channel().isWritable()) {
                ctx.writeAndFlush(RpcResponse.success(result, msg.getRequestId()));
            } else {
                logger.error("通道不可写");
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("处理过程调用时有错误发生:");
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.READER_IDLE) {
                logger.info("长时间未收到心跳包，断开连接...");
                ctx.close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

}
/*
SimpleChannelInboundHandler 是 Netty 中的一个基础类，实现了 ChannelInboundHandler 接口。
它主要用于处理入站事件，即从对等端接收到的数据或状态更改事件，例如对等端连接或断开连接。
与 ChannelInboundHandlerAdapter 不同的是，SimpleChannelInboundHandler 可以自动释放资源，
因此不需要显示地调用 ReferenceCountUtil.release() 释放资源。

当数据从远程节点传入时，SimpleChannelInboundHandler 将自动将其转换为指定类型的对象，
并在调用 channelRead0() 方法时向你提供该对象，你只需要处理传入的数据。
可以使用这个类来构建各种应用程序，例如聊天应用程序、游戏服务器、文件传输应用程序等。
 */