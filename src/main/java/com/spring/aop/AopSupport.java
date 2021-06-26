package com.spring.aop;

import com.cnn.survive.NNBeanProcessor;

import java.lang.reflect.Proxy;

public class AopSupport {

    public void proxyCreator(Object bean){
        Object proxyInstance = Proxy.newProxyInstance(NNBeanProcessor.class.getClassLoader(),
                bean.getClass().getInterfaces(),
                (proxy, method, args) -> {
                    //前
                    Object obj = method.invoke(bean,args);//这里会调用被代理对象的业务方法
                    //后
                    return obj;
                });
    }

}
