package com.spring.beansfactory.config;

public class BeanDefinition {

    public static final String SCOPE_DEFAULT = "";

    private Class clazz;
    private String scope = SCOPE_DEFAULT;

    public BeanDefinition(){
    }

    public BeanDefinition(Class clazz,String scope){
        this.clazz = clazz;
        this.scope = scope;
    }

    public Class getClazz(){
        return clazz;
    }

    public void setClazz(Class clazz){
        this.clazz = clazz;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

}
