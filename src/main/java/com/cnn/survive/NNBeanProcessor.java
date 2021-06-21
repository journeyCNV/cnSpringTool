package com.cnn.survive;

import com.spring.beansfactory.config.BeanPostProcessor;
import com.spring.annotation.Component;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

@Component("beanProcessor")
public class NNBeanProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        //System.out.println("初始化前");
        if(beanName.equals("hhhh")){
            ((Hhhh)bean).setBeanName("ohhh");
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName)  {
        //System.out.println("初始化后");

        //匹配 是否需要AOP ……

        /**
         * 实现AOP
         * 在Spring中开启AOP：@EnableAspectJAutoProxy
         * 在Spring源码中,开启AOP就是通过上面的注解最终向容器里注册一个BeanPostProcessor的Bean对象
         */
        if (beanName.equals("food")) {
            Object proxyInstance = Proxy.newProxyInstance(NNBeanProcessor.class.getClassLoader(),
                    bean.getClass().getInterfaces(),
                    new InvocationHandler() {
                        @Override
                        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                            System.out.println("food用到的代理逻辑");
                            /**
                             * 可以找切点，然后执行切点的方法 ……
                             */

                            return method.invoke(bean,args);//这里会调用被代理对象的业务方法
                        }
                    });

            return proxyInstance;
        }
        return bean;
    }
}
