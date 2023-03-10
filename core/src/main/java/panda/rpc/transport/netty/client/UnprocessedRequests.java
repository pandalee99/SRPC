package panda.rpc.transport.netty.client;


import panda.rpc.entity.RpcResponse;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;


public class UnprocessedRequests {

    private static ConcurrentHashMap<String, CompletableFuture<RpcResponse>> unprocessedResponseFutures = new ConcurrentHashMap<>();

    public void put(String requestId, CompletableFuture<RpcResponse> future) {
        unprocessedResponseFutures.put(requestId, future);
    }

    public void remove(String requestId) {
        unprocessedResponseFutures.remove(requestId);
    }

    public void complete(RpcResponse rpcResponse) {
        CompletableFuture<RpcResponse> future = unprocessedResponseFutures.remove(rpcResponse.getRequestId());
        if (null != future) {
            future.complete(rpcResponse);
        } else {
            throw new IllegalStateException();
        }
    }
    /*
    这个类名为 UnprocessedRequests，用于处理未处理的RPC请求。

在这个类中，使用了一个静态的 ConcurrentHashMap 对象，用于存储未处理的RPC请求。
其中，Key 值为请求的ID，Value 值为一个 CompletableFuture<RpcResponse> 对象，用于异步获取 RPC 响应结果。

类中的 put 方法用于将未处理的请求放入 unprocessedResponseFutures 中。
remove 方法用于移除 unprocessedResponseFutures 中的请求
，而 complete 方法用于标记RPC响应已完成，并将结果设置到对应的 CompletableFuture 中。
如果找不到对应的请求，将会抛出 IllegalStateException 异常。

因此，这个类提供了一个便捷的方式来跟踪和处理未处理的RPC请求和响应。
     */

}
