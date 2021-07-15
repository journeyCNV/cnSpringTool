package com.spring.mvc;

import java.lang.reflect.Method;
import java.util.Map;

public class ControllerDefine {
    private Class<?> controllerClass; //类
    private Method invokeMethod; //执行的方法
    private Map<String,Class<?>> methodParams; //方法参数名和对应类型

    public Class<?> getControllerClass() {
        return controllerClass;
    }

    public void setControllerClass(Class<?> controllerClass) {
        this.controllerClass = controllerClass;
    }

    public Method getInvokeMethod() {
        return invokeMethod;
    }

    public void setInvokeMethod(Method invokeMethod) {
        this.invokeMethod = invokeMethod;
    }

    public Map<String, Class<?>> getMethodParams() {
        return methodParams;
    }

    public void setMethodParams(Map<String, Class<?>> methodParams) {
        this.methodParams = methodParams;
    }

    public ControllerDefine(Class<?> controllerClass, Method invokeMethod, Map<String, Class<?>> methodParams) {
        this.controllerClass = controllerClass;
        this.invokeMethod = invokeMethod;
        this.methodParams = methodParams;
    }

}
