package com.spring.beansfactory.support;

public class BeanSupport {
    private Object instance;
    private String[] beanNames;

    public BeanSupport(Object instance,String[] beanNames){
        this.instance = instance;
        this.beanNames = beanNames;
    }
}
