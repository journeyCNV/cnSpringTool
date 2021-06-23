package com.cnn;

import com.cnn.survive.Dog;
import com.cnn.survive.Food;
import com.cnn.survive.Hhhh;
import com.spring.context.CNApplicationContext;

import java.util.Arrays;

public class TestSpring {

    public static void main(String[] args){

        CNApplicationContext cnApplicationContext = new CNApplicationContext(TestConfig.class);
        Hhhh hhhh = (Hhhh) cnApplicationContext.getBean("hhhh");
        hhhh.test();

        Food food = (Food) cnApplicationContext.getBean("food");
        food.test();

        System.out.println("******************************");
        Dog dog = (Dog) cnApplicationContext.getBean("dog");
        dog.test();
    }

}
