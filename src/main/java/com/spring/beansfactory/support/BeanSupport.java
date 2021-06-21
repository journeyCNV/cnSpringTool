package com.spring.beansfactory.support;

import java.lang.reflect.Constructor;
import java.util.ArrayList;

public class BeanSupport {
    final private Object instance;
    final private String[] beanNames;
    final private boolean mayHasBean;
    final private BeanSupportCandidate candidates;

    public BeanSupport(Object instance,String[] beanNames,boolean mayHasBean,BeanSupportCandidate candidates){
        this.instance = instance;
        this.beanNames = beanNames;
        this.mayHasBean = mayHasBean;
        this.candidates = candidates;
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

    public BeanSupportCandidate getCandidate(){
        return candidates;
    }
}
