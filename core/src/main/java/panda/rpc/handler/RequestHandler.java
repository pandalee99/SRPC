package panda.rpc.handler;

import panda.rpc.entity.RpcRequest;
import panda.rpc.entity.RpcResponse;
import panda.rpc.enumeration.ResponseCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import panda.rpc.provider.ServiceProvider;
import panda.rpc.provider.ServiceProviderImpl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 进行过程调用的处理器
 */
public class RequestHandler {

    private static final Logger          logger = LoggerFactory.getLogger(RequestHandler.class);
    private static final ServiceProvider serviceProvider;

    static {
        serviceProvider = new ServiceProviderImpl();
    }

    public Object handle(RpcRequest rpcRequest) {
        Object service = serviceProvider.getServiceProvider(rpcRequest.getInterfaceName());
        return invokeTargetMethod(rpcRequest, service);
    }

    private Object invokeTargetMethod(RpcRequest rpcRequest, Object service) {
        Object result;
        try {
            Method method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamTypes());
            result = method.invoke(service, rpcRequest.getParameters());
            logger.info("服务:{} 成功调用方法:{}", rpcRequest.getInterfaceName(), rpcRequest.getMethodName());
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            return RpcResponse.fail(ResponseCode.METHOD_NOT_FOUND, rpcRequest.getRequestId());
        }
        return result;
    }

}



/*
在Java中，Method类是反射机制的一部分，它代表一个类中的一个方法。
可以使用Method类来获取关于方法的信息，如方法名、参数列表、返回类型、修饰符等，
并且可以使用Method类来调用该方法。Method类提供了许多用于获取和调用方法的方法，
如invoke()、getName()、getParameterTypes()、getReturnType()等。
反射机制中的Method类可以使得在运行时动态地获取和调用类中的方法。
 */
