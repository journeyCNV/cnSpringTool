package com.spring.context;

import com.spring.annotation.aop.Aspect;
import com.spring.aop.ProxyCreator;
import com.spring.beansfactory.BeanNameAware;
import com.spring.beansfactory.InitializingBean;
import com.spring.annotation.Autowired;
import com.spring.annotation.Component;
import com.spring.annotation.ComponentScan;
import com.spring.annotation.Scope;
import com.spring.beansfactory.config.BeanDefinition;
import com.spring.beansfactory.config.BeanPostProcessor;
import com.spring.beansfactory.support.AutowireCapableBeanFactory;
import com.spring.beansfactory.support.BeanSupport;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class CNApplicationContext {
    //配置文件
    final private Class configClass;

    //BeanFactory支持类
    private AutowireCapableBeanFactory beanFactory = new AutowireCapableBeanFactory();

    //单例池
    private ConcurrentHashMap<String,Object> singletonObjects = new ConcurrentHashMap<>();

    //存Bean的定义
    private ConcurrentHashMap<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();

    private List<BeanPostProcessor> beanPostProcessorList = new ArrayList<>();


    public CNApplicationContext(Class configClass){
        this.configClass = configClass;

        scanGetDefine(configClass);

        for(String beanName : beanDefinitionMap.keySet()){
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
            if(beanDefinition.getScope().equals("singleton")){
                Object bean = createBean(beanName,beanDefinition); //单例Bean
                singletonObjects.put(beanName,bean); //放入单例池
            }
        }

    }

    /**
     * 创建Bean
     */
    public Object createBean(String beanName,BeanDefinition beanDefinition){
        Class clazz = beanDefinition.getClazz();
        Object instance = null;

        try {
            BeanSupport beanSupport = beanFactory.createBeanInstance(beanName,clazz);

            //这里需要处理一下 依赖注入-构造注入
            if (beanSupport.getInstance()!=null) { //不用选举，已经确定
                instance = beanSupport.getInstance();
                if (beanSupport.isMayHasBean()) {
                    for (String name : beanSupport.getBeanNames()) {
                        if (containsBean(name)) {
                            Object beanNeed = getBean(name);
                            Field field = clazz.getDeclaredField(name);
                            field.setAccessible(true);
                            field.set(instance,beanNeed);
                        }
                    }
                }
            }else {
                Constructor<?> hasMaxNumBeanCon=null;
                int currentMax=0;
                for (Constructor<?> candidate : beanSupport.getCandidate().getCandidates()) {
                    int count = 0;
                    for (Parameter parameter : candidate.getParameters()) {
                        count += containsBean(parameter.getName())?1:0;
                    }
                    if(count>currentMax){
                        currentMax = count;
                        hasMaxNumBeanCon = candidate;
                    }
                }
                Parameter[] params = hasMaxNumBeanCon.getParameters();
                ArrayList<Object> paramList = new ArrayList<>();
                for (Parameter param : params) {
                    if(param.getType().isPrimitive()) { //如果是基本类型
                        if (param.getType() == Boolean.class) {
                            paramList.add(false);
                        } else {
                            paramList.add(0);
                        }
                    }else {
                        if (containsBean(param.getName())) {
                            paramList.add(getBean(param.getName()));
                        }else{
                            paramList.add(null);
                        }
                    }
                }
                instance = hasMaxNumBeanCon.newInstance(paramList.toArray());
            }


            //依赖注入-属性注入 @Autowired注解
            for (Field declaredField : clazz.getDeclaredFields()) {
                //如果属性上加了Autowired注解，那我就进行属性赋值
                if(declaredField.isAnnotationPresent(Autowired.class)){
                    Object bean = getBean(declaredField.getName());
                    if(bean == null){
                        throw new RuntimeException("无法注入对应的类，目标类型：" + declaredField.getType().getName());
                    }
                    declaredField.setAccessible(true);
                    declaredField.set(instance,bean);
                }
            }

            //Aware回调-依赖注入-回调注入
            if(instance instanceof BeanNameAware){
                ((BeanNameAware) instance).setBeanName(beanName);
            }

            for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                instance = beanPostProcessor.postProcessBeforeInitialization(instance,beanName);
            }

            //初始化
            if(instance instanceof InitializingBean){
                try {
                    ((InitializingBean) instance).afterPropertiesSet();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            //BeanPostProcessor 提供初始化前和初始化后的操作
            //初始化后
            for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                instance = beanPostProcessor.postProcessAfterInitialization(instance,beanName);
            }

            return instance;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 扫描路径，根据注解获取BeanDefinition放入beanDefinitionMap
     */
    private void scanGetDefine(Class configClass) {
        //解析配置类
        //把ComponentScan注解解析-->得到扫描路径-->扫描
        //针对的是类上有没有spring提供的注解，类里的方法有没有spring提供的注解
        ComponentScan componentScan = (ComponentScan) configClass.getDeclaredAnnotation(ComponentScan.class);
        String path = componentScan.value(); //得到了扫描路径
        path = path.replace(".","/");
        //System.out.println(path+"*******");


        //扫描
        //先把路径下的类都找出来
        //类加载 Application ClassLoader
        ClassLoader classLoader = CNApplicationContext.class.getClassLoader();
        URL resource = classLoader.getResource(path);
        //System.out.println(resource.getFile().toString()+"*******");
        File file = new File(resource.getFile());//得到目录

        if(file.isDirectory()) {
            File[] files = file.listFiles();
            for (File f : files) {
                String filename = f.getAbsolutePath();
                if (filename.endsWith(".class")) {
                    String className = filename.substring(filename.indexOf("com"), filename.indexOf(".class"));
                    className = className.replace("\\", ".");
                    //System.out.println("类名"+className);

                    try {
                        Class<?> clazz = classLoader.loadClass(className);
                        if (clazz.isAnnotationPresent(Component.class)) {
                            //当这个类是一个Bean
                            //解析类，判断当前bean是单例bean，还是prototype的bean
                            //解析类生成一个BeanDefinition对象

                            Component componentAnnotation = clazz.getDeclaredAnnotation(Component.class);
                            String beanName = componentAnnotation.value();

                            //当前class对象是否实现了BeanPostProcessor接口
                            if (BeanPostProcessor.class.isAssignableFrom(clazz)) {
                                BeanPostProcessor instanceBpp = (BeanPostProcessor) clazz.getDeclaredConstructor().newInstance();
                                beanPostProcessorList.add(instanceBpp);
                            }

                            /**
                             * 使用AOP只重写了后置方法,和上面的还是不一样的。
                             * 也希望程序员如果用了这个注解就不要再去继承BeanPostProcessor了
                             * 除非是希望在Bean初始化前做一些操作
                             */
                            //当前class对象是否使用@Aspect注解
                            if(clazz.isAnnotationPresent(Aspect.class)){
                                BeanPostProcessor bpp = new ProxyCreator(clazz.getDeclaredAnnotation(Aspect.class).value(),clazz);
                                beanPostProcessorList.add(bpp);
                            }

                            //解析出来的Bean的定义
                            BeanDefinition beanDefinition = new BeanDefinition();
                            beanDefinition.setClazz(clazz);
                            if (clazz.isAnnotationPresent(Scope.class)) {
                                Scope scopeAnnotation = clazz.getDeclaredAnnotation(Scope.class);
                                beanDefinition.setScope(scopeAnnotation.value());
                            } else if(clazz.isAnnotationPresent(Aspect.class)){
                                beanDefinition.setScope("prototype");
                            }
                            else {
                                beanDefinition.setScope("singleton");
                            }
                            //扫描到的所有类的定义
                            beanDefinitionMap.put(beanName, beanDefinition);

                        }
                    } catch (ClassNotFoundException | NoSuchMethodException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * 按名称获取Bean
     */
    public Object getBean(String beanName){
        if(beanDefinitionMap.containsKey(beanName)){
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
            if(beanDefinition.getScope().equals("singleton")){
                //双检锁单例模式
                if(!singletonObjects.containsKey(beanName)){
                    synchronized (CNApplicationContext.class){
                        if(!singletonObjects.containsKey(beanName)){
                            Class beanClazz = beanDefinition.getClazz();
                            //Object bean = beanClazz.getDeclaredConstructor().newInstance();
                            Object bean = createBean(beanName,beanDefinition);
                            singletonObjects.put(beanName,bean);
                        }
                    }
                    //这里记得从单例池拿，而不是上面直接返回
                    return singletonObjects.get(beanName);
                }else {
                    //如果单例池里有从单例池返回
                    Object bean = singletonObjects.get(beanName);
                    return bean;
                }
            }else {
                //要New一个bean 原型bean
                Object bean = createBean(beanName,beanDefinition);
                return bean;
            }
        }else {
            throw new NullPointerException();
        }
    }

    /**
     * TODO
     * 按类型获取Bean
     */

    /**
     * Bean是否存在
     */
    public boolean containsBean(String name){
        if(singletonObjects.containsKey(name))
            return true;
        if(beanDefinitionMap.containsKey(name))
            return true;
        return false;
    }

}

