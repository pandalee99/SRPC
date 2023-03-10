package panda.rpc;

import panda.rpc.annotation.ServiceScan;
import panda.rpc.serializer.CommonSerializer;
import panda.rpc.transport.RpcServer;
import panda.rpc.transport.socket.server.SocketServer;

@ServiceScan
public class SocketTestServer {

    public static void main(String[] args) {
        RpcServer server = new SocketServer("127.0.0.1", 9998, CommonSerializer.HESSIAN_SERIALIZER);
        server.start();
    }

}
