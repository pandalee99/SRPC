package panda.rpc.factory;

import java.util.HashMap;
import java.util.Map;


/*
单例工厂模式是一种创建型设计模式，它提供了一种创建单例对象的方式。
在这个模式中，一个工厂类负责创建单例对象，并且确保在应用程序的整个生命周期中只存在一个实例。

单例工厂模式的核心思想是将对象的创建过程封装在一个工厂类中，通过调用工厂类的方法来获取单例对象。
这种方法可以使应用程序更加灵活和可扩展，
因为在未来需要更改单例对象时，只需要更改工厂类中的实现即可，而不需要更改整个应用程序的代码。

单例工厂模式在许多应用程序中都被广泛使用，特别是在需要保证系统中只有一个实例的情况下，比如数据库连接池、线程池等。
 */
public class SingletonFactory {

    private static Map<Class, Object> objectMap = new HashMap<>();

    private SingletonFactory() {}

    public static <T> T getInstance(Class<T> clazz) {
        Object instance = objectMap.get(clazz);
        synchronized (clazz) {
            if(instance == null) {
                try {
                    instance = clazz.newInstance();
                    objectMap.put(clazz, instance);
                } catch (IllegalAccessException | InstantiationException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        }
        return clazz.cast(instance);

        /*
在Java中，Class.cast()方法用于将一个对象强制类型转换为另一个类的实例。
它的作用是将一个对象转换为指定类型的对象，并返回转换后的对象。

具体来说，Class.cast()方法接受一个Object类型的参数，然后将这个对象转换为指定的类型。
如果转换成功，返回转换后的对象，否则抛出ClassCastException异常。

例如，假设我们有一个类A和一个类B，B是A的子类，现在有一个对象obj，它是A类型的。
我们可以使用Class.cast()方法将obj强制转换为B类型的对象，代码如下：

css
Copy code
A obj = new B();
B b = B.class.cast(obj);
这里我们先创建了一个B类型的对象obj，然后使用Class.cast()方法将它强制转换为B类型的对象，并将结果保存在变量b中。

需要注意的是，Class.cast()方法只能将一个对象转换为指定类型的对象，
如果转换的类型不正确，就会抛出ClassCastException异常。因此，在使用Class.cast()方法时，
需要确保被转换的对象确实是要转换的类型，否则会导致程序异常。
         */
    }

}
