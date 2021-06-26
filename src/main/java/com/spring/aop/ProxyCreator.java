package com.spring.aop;

import com.spring.annotation.aop.After;
import com.spring.annotation.aop.Before;
import com.spring.beansfactory.config.BeanPostProcessor;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class ProxyCreator implements BeanPostProcessor {

    private final String aspectBeanName; //要增强的目标bean的名字 比如说food

    private final Class clazz; //被@Aspect注解的Class

    public ProxyCreator(String aspectBeanName,Class clazz){
        this.aspectBeanName = aspectBeanName;
        this.clazz = clazz;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        if(beanName.equals(aspectBeanName)) {
            Object proxyInstance = Proxy.newProxyInstance(ProxyCreator.class.getClassLoader(),
                    bean.getClass().getInterfaces(),
                    (proxy, method, args) -> {
                        Method[] classMethods = clazz.getDeclaredMethods();
                        //前置方法
                        //如果带了@before注解
                        //在这里调用
                        for (Method classMethod : classMethods) {
                            if (classMethod.isAnnotationPresent(Before.class))
                                classMethod.invoke(clazz.newInstance());
                        }
                        Object obj = method.invoke(bean, args); //会根据被继承的接口的方法来
                        //后置方法
                        //如果带了@after注解
                        //在这里调用
                        for (Method classMethod : classMethods) {
                            if (classMethod.isAnnotationPresent(After.class))
                                classMethod.invoke(clazz.newInstance());
                        }
                        return obj;
                    });
            return proxyInstance;
        }
        return null;
    }

}
