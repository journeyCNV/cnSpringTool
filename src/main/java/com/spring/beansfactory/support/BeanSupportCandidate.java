package com.spring.beansfactory.support;

import java.lang.reflect.Constructor;
import java.util.ArrayList;

public class BeanSupportCandidate {
    final private ArrayList<Constructor<?>> candidates;
    final private int size;
    final private Constructor<?> noParamCon;

    protected BeanSupportCandidate(ArrayList candidates,int size,Constructor noParamCon){
        this.candidates = candidates;
        this.size = size;
        this.noParamCon = noParamCon;
    }

    public ArrayList<Constructor<?>> getCandidates(){
        return candidates;
    }

    public int getSize(){
        return size;
    }

    public Constructor<?> getNoParamCon(){
        return noParamCon;
    }

}
