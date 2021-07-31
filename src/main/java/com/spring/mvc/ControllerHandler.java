package com.spring.mvc;

import com.spring.annotation.web.RequestMapping;
import com.spring.annotation.web.RequestMethod;
import com.spring.mvc.servlet.ControllerDefine;
import com.spring.mvc.servlet.PathDefine;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Controller分发器
 */
public class ControllerHandler {

    private Map<PathDefine, ControllerDefine> pathControllerMap = new ConcurrentHashMap<>();

    public ControllerHandler(){
    }

    /**
     * 使用了@Controller的类调用这个方法
     * @param classSet
     */
    public ControllerHandler(Set<Class<?>> classSet){
        for (Class<?> aClass : classSet) {
            putPathController(aClass);
        }
    }

    /**
     * 获取Controller信息
     */
    public ControllerDefine getController(String requestMethod, String requestPath){
        PathDefine pathDefine = new PathDefine(requestMethod,requestPath);
        return pathControllerMap.get(pathDefine);
    }

    /**
     * 添加信息到requestControllerMap中
     */
    private void putPathController(Class<?> clazz){
        String basePath;
        if(clazz.isAnnotationPresent(RequestMapping.class)){
            RequestMapping requestMapping = clazz.getAnnotation(RequestMapping.class);
            basePath = requestMapping.value();//在类上的注解作为base 路径
        }else {
            //如果只单用了Controller注解，没有指定基础路径，那给一个默认的空路径
            basePath = "";
        }
        //获取Controller类中的所有方法
        Method[] controllerMethods = clazz.getDeclaredMethods();
        for (Method method : controllerMethods) {
            if(method.isAnnotationPresent(RequestMapping.class)){
                //获取这个方法的参数名和参数类型数组
                Parameter[] parameters = method.getParameters();
                Map<String,Class<?>> paramsMap = null;
                if(parameters.length!=0){
                    paramsMap = new HashMap<>();
                    for (Parameter parameter : parameters) {
                        paramsMap.put(parameter.getName(),parameter.getType());
                    }
                }
                RequestMapping methodRequest = method.getAnnotation(RequestMapping.class);
                String methodPath = methodRequest.value(); //方法指定路径
                RequestMethod requestMethod = methodRequest.method(); //请求方法
                PathDefine pathDefine = new PathDefine(requestMethod.toString(),basePath+methodPath);
                if(pathControllerMap.containsKey(pathDefine)){
                    //已经存在该URL
                    throw new RuntimeException("url 重复注册!");
                }
                ControllerDefine controllerDefine = new ControllerDefine(clazz,method,paramsMap);
                pathControllerMap.put(pathDefine,controllerDefine);
            }
        }
    }

}
