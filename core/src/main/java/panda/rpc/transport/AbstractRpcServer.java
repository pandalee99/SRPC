package panda.rpc.transport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import panda.rpc.annotation.Service;
import panda.rpc.annotation.ServiceScan;
import panda.rpc.enumeration.RpcError;
import panda.rpc.exception.RpcException;
import panda.rpc.provider.ServiceProvider;
import panda.rpc.registry.ServiceRegistry;
import panda.rpc.util.ReflectUtil;

import java.net.InetSocketAddress;
import java.util.Set;


public abstract class AbstractRpcServer implements RpcServer {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    protected String host;
    protected int port;

    protected ServiceRegistry serviceRegistry;
    protected ServiceProvider serviceProvider;

    public void scanServices() {
        String mainClassName = ReflectUtil.getStackTrace();
        Class<?> startClass;
        try {
            startClass = Class.forName(mainClassName);
            if(!startClass.isAnnotationPresent(ServiceScan.class)) {
                logger.error("启动类缺少 @ServiceScan 注解");
                throw new RpcException(RpcError.SERVICE_SCAN_PACKAGE_NOT_FOUND);
            }
        } catch (ClassNotFoundException e) {
            logger.error("出现未知错误");
            throw new RpcException(RpcError.UNKNOWN_ERROR);
        }
        String basePackage = startClass.getAnnotation(ServiceScan.class).value();
        if("".equals(basePackage)) {
            basePackage = mainClassName.substring(0, mainClassName.lastIndexOf("."));
        }
        Set<Class<?>> classSet = ReflectUtil.getClasses(basePackage);
        for(Class<?> clazz : classSet) {
            if(clazz.isAnnotationPresent(Service.class)) {
                String serviceName = clazz.getAnnotation(Service.class).name();
                Object obj;
                try {
                    obj = clazz.newInstance();
                } catch (InstantiationException | IllegalAccessException e) {
                    logger.error("创建 " + clazz + " 时有错误发生");
                    continue;
                }
                if("".equals(serviceName)) {
                    Class<?>[] interfaces = clazz.getInterfaces();
                    for (Class<?> oneInterface: interfaces){
                        publishService(obj, oneInterface.getCanonicalName());
                    }
                } else {
                    publishService(obj, serviceName);
                    /*
                    这段代码是在判断 Service 注解中的 name 属性是否为空，如果为空，
                    则说明该服务实现类实现了多个接口，并且需要将每个接口都发布成一个独立的服务。
                    所以，代码通过获取该服务实现类的所有接口，然后将每个接口都作为一个独立的服务进行发布。
                    如果 name 属性不为空，则说明只需要将该服务实现类作为一个服务进行发布。
                    此时，代码直接将该服务实现类作为一个服务进行发布。
                     */
                }
            }
        }
    }

    @Override
    public <T> void publishService(T service, String serviceName) {
        serviceProvider.addServiceProvider(service, serviceName);
        serviceRegistry.register(serviceName, new InetSocketAddress(host, port));
    }

}
