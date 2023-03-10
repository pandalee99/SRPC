package panda.rpc;

import panda.rpc.annotation.ServiceScan;
import panda.rpc.serializer.CommonSerializer;
import panda.rpc.transport.RpcServer;
import panda.rpc.transport.netty.server.NettyServer;

@ServiceScan
public class NettyTestServer {

    public static void main(String[] args) {
        RpcServer server = new NettyServer("127.0.0.1", 9999, CommonSerializer.PROTOBUF_SERIALIZER);
        server.start();
    }

}
