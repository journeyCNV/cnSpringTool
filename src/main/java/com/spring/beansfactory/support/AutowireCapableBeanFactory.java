package com.spring.beansfactory.support;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

public class AutowireCapableBeanFactory {
    /**
     * 在Spring源码中包含了createBean方法，这里剥离出来放到ApplicationContext中了
     * 虽然ApplicationContext也是import了这个类
     *
     * 实现的功能
     * 1.决定使用哪个构造函数创建Bean对象
     * 2.解决循环依赖
     */



    public Object createBeanInstance(String beanName,Class clazz) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        //先解析构造函数
        //有几个构造函数，有没有带@Autowired的，有没有无参构造
        //如果没有无参构造并且有多个构造没有@Autowired抛出异常

        Constructor[] cons = clazz.getDeclaredConstructors();
        
        if(cons.length==1){
            //如果只有一个构造函数,那只能用这个。
            Class[] params = cons[0].getParameterTypes();
            if(params==null){ //如果唯一的构造函数是无参构造，直接使用无参
                return cons[0].newInstance();
            }

            ArrayList<Object> paramList = new ArrayList<>();
            for (Class param : params) {
                if(param.isPrimitive()) { //如果是基本类型
                    if (param == Boolean.class) {
                        paramList.add(false);
                    } else {
                        paramList.add(0);
                    }
                }else {
                    paramList.add(null);
                }
            }
            return cons[0].newInstance(paramList.toArray());
        }
                
        return null;
    }

}
