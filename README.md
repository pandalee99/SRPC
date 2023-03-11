


# 使用Netty+Nacos+Protobuf制作RPC框架

<a name="KeQ2k"></a>

## 简介

<a name="FQTT2"></a>

### 显现的功能

这个RPC实现了一些基本的功能：

- 使用Netty来进行网络传输，效率比起传统的NIO要高很多。
- 使用单例模式，在Netty获取Channel的过程中，会有一个ChannelProvider去提供Channel单例。
- 使用Nacos作为服务的注册中心，用于管理注册的服务，当客户端请求发过来时，Nacos会寻找合适的服务返回给客户端消费。
- 实现了负载均衡的功能，，客户端对于Nacos返回的服务列表，会使用负载均衡算法，选择一个自己需要的服务加入，目前实现了轮询算法和随机选取算法。
- 加入了心跳检测机制，并不会发送完消息立即结束，而是保持的长连接，提高效率。
- 使用Potobuf作为对象的的序列化工具，实现Netty中的编/解码的功能，提高了效率。
- 实现了钩子函数，当服务端下线的时候会自动去Nacos注销服务。
- 使用CompletableFuture来接受客户端返回的结果。

<a name="UlREa"></a>

### 测试

由于使用Nacos，调试比较简单：<br />下载好Nacos，无论是win版还是linux版，在官网都有，比较方便；<br />但是由于Nacos一般都要配置数据库，为了方便测试，可以使用命令先进行单机运行

```shell
startup.cmd -m standalone
```

客户端：

```java
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
```

服务端：

```java
@ServiceScan
public class NettyTestServer {

    public static void main(String[] args) {
        RpcServer server = new NettyServer("127.0.0.1", 9999, CommonSerializer.PROTOBUF_SERIALIZER);
        server.start();
    }

}

```

之后会有一个测试结果：<br />客户端收到信息<br />![image.png](https://cdn.nlark.com/yuque/0/2023/png/34531809/1678513541152-f58818bf-b30d-438c-bebd-3374cb6f6b3f.png#averageHue=%23f5ecea&clientId=ucfcf756b-0ecd-4&from=paste&height=400&id=uc9973f1d&name=image.png&originHeight=600&originWidth=2205&originalType=binary&ratio=1&rotation=0&showTitle=false&size=111506&status=done&style=none&taskId=ubbd01eee-67bf-4697-b872-6eef4c3b304&title=&width=1470)<br />服务端收到信息<br />![image.png](https://cdn.nlark.com/yuque/0/2023/png/34531809/1678513584290-393ae7f9-f650-4a6d-b6a2-ad58cd7c2360.png#averageHue=%23f5ebe9&clientId=ucfcf756b-0ecd-4&from=paste&height=301&id=u72da898a&name=image.png&originHeight=452&originWidth=2307&originalType=binary&ratio=1&rotation=0&showTitle=false&size=88585&status=done&style=none&taskId=u57f54541-d548-4720-aca7-051e4b65050&title=&width=1538)

<a name="VFwOu"></a>
