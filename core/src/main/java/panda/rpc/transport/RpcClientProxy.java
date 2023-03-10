package panda.rpc.transport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import panda.rpc.entity.RpcRequest;
import panda.rpc.entity.RpcResponse;
import panda.rpc.transport.netty.client.NettyClient;
import panda.rpc.transport.socket.client.SocketClient;
import panda.rpc.util.RpcMessageChecker;


import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * RPC客户端动态代理
 */
public class RpcClientProxy implements InvocationHandler {

    private static final Logger logger = LoggerFactory.getLogger(RpcClientProxy.class);

    private final RpcClient client;

    public RpcClientProxy(RpcClient client) {
        this.client = client;
    }

    @SuppressWarnings("unchecked")
    public <T> T getProxy(Class<T> clazz) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{clazz}, this);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        logger.info("调用方法: {}#{}", method.getDeclaringClass().getName(), method.getName());
        RpcRequest rpcRequest = new RpcRequest(UUID.randomUUID().toString(), method.getDeclaringClass().getName(),
                method.getName(), args, method.getParameterTypes(), false);
        RpcResponse rpcResponse = null;
        if (client instanceof NettyClient) {
            try {
                CompletableFuture<RpcResponse> completableFuture = (CompletableFuture<RpcResponse>) client.sendRequest(rpcRequest);
                rpcResponse = completableFuture.get();
            } catch (Exception e) {
                logger.error("方法调用请求发送失败", e);
                return null;
            }
        }
        if (client instanceof SocketClient) {
            rpcResponse = (RpcResponse) client.sendRequest(rpcRequest);
        }
        RpcMessageChecker.check(rpcRequest, rpcResponse);
        return rpcResponse.getData();
    }
}
/*
InvocationHandler 是 Java 标准库中的一个接口，它用于实现动态代理。

动态代理是一种运行时生成代理对象的技术。使用动态代理可以在运行时动态地创建一个实现特定接口的代理类，
这个代理类可以将所有方法调用委托给指定的对象或方法。
在委托调用前或调用后，代理类可以执行额外的逻辑，例如统计方法调用次数、记录方法调用日志等。

InvocationHandler 接口定义了一个方法 invoke，该方法会在代理类每次调用方法时被调用。该
方法有三个参数：

proxy：代理对象
method：被调用的方法
args：被调用方法的参数列表
invoke 方法的返回值是 Object 类型，它表示被调用方法的返回值。

因此，当我们想要使用动态代理技术时，需要实现 InvocationHandler 接口并重写 invoke 方法，
来控制代理类如何处理方法调用
 */
