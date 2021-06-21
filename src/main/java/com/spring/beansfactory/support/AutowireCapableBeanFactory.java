package com.spring.beansfactory.support;

import com.spring.annotation.Autowired;
import com.spring.beansfactory.BeansException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
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


    /**
     * Bean选择哪个构造方法来进行实例化
     * @param beanName
     * @param clazz
     * @return
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws InstantiationException
     */
    public BeanSupport createBeanInstance(String beanName,Class<?> clazz) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        //先解析构造函数
        //有几个构造函数，有没有带@Autowired的，有没有无参构造
        //如果没有无参构造并且有多个构造没有@Autowired抛出异常

        Constructor<?>[] cons = clazz.getDeclaredConstructors();

        int consLength = cons.length;

        ArrayList<Object> paramList = new ArrayList<>();
        ArrayList<String> paramBeanNames = new ArrayList<>();
        boolean mayHasBean = false; //是否可能有依赖的Bean存在
        
        if(consLength==1){
            //如果只有一个构造函数,那只能用这个。
            //Class[] params = cons[0].getParameterTypes();
            Parameter[] params = cons[0].getParameters(); //Java8的Parameter类
            if(params==null){ //如果唯一的构造函数是无参构造，直接使用无参
                return new BeanSupport(cons[0].newInstance(),null,false);
            }
            for (Parameter param : params) {
                if(param.getType().isPrimitive()) { //如果是基本类型
                    if (param.getType() == Boolean.class) {
                        paramList.add(false);
                    } else {
                        paramList.add(0);
                    }
                }else {
                    paramList.add(null);
                    paramBeanNames.add(param.getName());
                    mayHasBean = true;
                }
            }
            if(!mayHasBean)
                return new BeanSupport(cons[0].newInstance(paramList.toArray()),null,mayHasBean);
            String[] beanNames = new String[paramBeanNames.size()];
            return new BeanSupport(cons[0].newInstance(paramList.toArray()),paramBeanNames.toArray(beanNames),mayHasBean);
        }else {
            int countAuto = 0;
            boolean hasNoParamCon = false;
            Constructor<?> recCon = null;
            int recAutoTrue = 0;
            int recAutoFalse = 0;
            for (Constructor<?> con : cons) {
                if(con.isAnnotationPresent(Autowired.class)) {
                    if(con.getDeclaredAnnotation(Autowired.class).required()){
                        recAutoTrue++;
                        recCon = con;
                    }else {
                        recAutoFalse++;
                    }
                    countAuto++;
                    continue;
                }
                if(con.getParameters().length==0) {
                    hasNoParamCon = true;
                    recCon = con;
                }
            }
            if(countAuto==0&&hasNoParamCon){
                return new BeanSupport(recCon.newInstance(),null,false);
            }
            if(recAutoTrue==1&&recAutoFalse==0){
                //只有一个Autowired注解是true,并且没有其他
                //处理recCon,解析参数
                return null;
            }
            if(recAutoTrue==0&&recAutoFalse>=0){
                //要在一堆false中进行选举，选能加载Bean更多的那个
                return null;
            }
        }
        throw  new BeansException("无法确定使用 "+beanName+" Bean的哪个构造方法");
    }

}
