package panda.rpc;

import panda.rpc.serializer.CommonSerializer;
import panda.rpc.transport.RpcClient;
import panda.rpc.transport.RpcClientProxy;
import panda.rpc.transport.netty.client.NettyClient;

public class NettyTestClient {

    public static void main(String[] args) {
        RpcClient client = new NettyClient(CommonSerializer.PROTOBUF_SERIALIZER);
        RpcClientProxy rpcClientProxy = new RpcClientProxy(client);
        HelloService helloService = rpcClientProxy.getProxy(HelloService.class);
        HelloObject object = new HelloObject(114514, "Client send a Message");
        String res = helloService.hello(object);
        System.out.println(res);
    }

}
