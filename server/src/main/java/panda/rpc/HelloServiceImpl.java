package panda.rpc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import panda.rpc.annotation.Service;

@Service
public class HelloServiceImpl implements HelloService {

    private static final Logger logger = LoggerFactory.getLogger(HelloServiceImpl.class);

    @Override
    public String hello(HelloObject object) {
        logger.info("接收到消息：{}", object.getMessage());
        return "服务端收到一个信息： "+object.getMessage();
    }

}
