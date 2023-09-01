package panda.rpc.codec;

import panda.rpc.entity.RpcRequest;
import panda.rpc.entity.RpcResponse;
import panda.rpc.enumeration.PackageType;
import panda.rpc.enumeration.RpcError;
import panda.rpc.exception.RpcException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import panda.rpc.serializer.CommonSerializer;

import java.util.List;

/**
 * 通用的解码拦截器
 */
public class CommonDecoder extends ReplayingDecoder {

    private static final Logger logger       = LoggerFactory.getLogger(CommonDecoder.class);
    private static final int    MAGIC_NUMBER = 0xCAFEBABE;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        int magic = in.readInt();
        if (magic != MAGIC_NUMBER) {
            logger.error("不识别的协议包: {}", magic);
            throw new RpcException(RpcError.UNKNOWN_PROTOCOL);
        }
        int packageCode = in.readInt();
        Class<?> packageClass;
        if (packageCode == PackageType.REQUEST_PACK.getCode()) {
            packageClass = RpcRequest.class;
        } else if (packageCode == PackageType.RESPONSE_PACK.getCode()) {
            packageClass = RpcResponse.class;
        } else {
            logger.error("不识别的数据包: {}", packageCode);
            throw new RpcException(RpcError.UNKNOWN_PACKAGE_TYPE);
        }
        int serializerCode = in.readInt();
        CommonSerializer serializer = CommonSerializer.getByCode(serializerCode);
        if (serializer == null) {
            logger.error("不识别的反序列化器: {}", serializerCode);
            throw new RpcException(RpcError.UNKNOWN_SERIALIZER);
        }
        int length = in.readInt();
        byte[] bytes = new byte[length];
        in.readBytes(bytes);
        Object obj = serializer.deserialize(bytes, packageClass);
        out.add(obj);
    }

}


/*
ReplayingDecoder 和 ByteToMessageDecoder 都是 Netty 中的解码器，但它们的工作原理不同。

ByteToMessageDecoder 是基于“先存储再解码”的原则。它会将读取的数据存储在缓冲区中，直到满足解码条件，
然后再将缓冲区中的数据解码成对象。这种方式的优点是可以减少解码器的内存使用，但缺点是需要更多的代码来处理缓冲区中的数据。

ReplayingDecoder 则是基于“先解码再读取”的原则。它会先进行解码，如果发现还需要更多的数据才能解码成功，
则会等待更多的数据到来，直到满足解码条件。这种方式的优点是更容易编写和维护，但缺点是可能需要更多的内存，
因为它需要在每个解码阶段保存完整的解码状态。

因此，ByteToMessageDecoder 适用于那些需要处理大量数据或需要更高性能的场景，
而 ReplayingDecoder 则适用于那些需要更简单的代码或需要更少的内存的场景。
 */
