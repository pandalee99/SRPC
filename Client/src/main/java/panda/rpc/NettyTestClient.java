package panda.rpc;

import panda.rpc.serializer.CommonSerializer;
import panda.rpc.transport.RpcClient;
import panda.rpc.transport.RpcClientProxy;
import panda.rpc.transport.netty.client.NettyClient;
public class NettyTestClient {
    public static void main(String[] args) {
        RpcClient client = new NettyClient(CommonSerializer.PROTOBUF_SERIALIZER);
        RpcClientProxy rpcClientProxy = new RpcClientProxy(client);
        //测试同类型的两个服务
        HelloService helloService = rpcClientProxy.getProxy(HelloService.class);
        HelloObject object = new HelloObject(114514, "Client need a help");
        String res = helloService.hello(object);
        System.out.println(res);
        HelloService helloService2 = rpcClientProxy.getProxy(HelloService.class);
        String res2 = helloService2.hello(object);
        System.out.println(res2);
        //测试不同类型的服务
        ByeService byeService=rpcClientProxy.getProxy(ByeService.class);
        System.out.println(byeService.bye("see you "));
    }
}
