package com.cnn;

import com.cnn.survive.Food;
import com.cnn.survive.Hhhh;
import com.spring.context.CNApplicationContext;

public class TestSpring {

    public static void main(String[] args){

        //WhatF whatF = new WhatF();
        //System.out.println(whatF.aByte+"----"+whatF.hh+"----"+whatF.name+"----"+whatF.ff);

        CNApplicationContext cnApplicationContext = new CNApplicationContext(TestConfig.class);
        Hhhh hhhh = (Hhhh) cnApplicationContext.getBean("hhhh");
        hhhh.test();

        Food food = (Food) cnApplicationContext.getBean("food");
        food.test();
    }

}
