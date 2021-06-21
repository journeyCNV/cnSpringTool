package com.spring.beansfactory.support;

public class BeanSupport {
    final private Object instance;
    final private String[] beanNames;
    final private boolean mayHasBean;

    public BeanSupport(Object instance,String[] beanNames,boolean mayHasBean){
        this.instance = instance;
        this.beanNames = beanNames;
        this.mayHasBean = mayHasBean;
    }

    public Object getInstance(){
        return instance;
    }

    public String[] getBeanNames(){
        return beanNames;
    }

    public boolean isMayHasBean(){
        return mayHasBean;
    }
}
