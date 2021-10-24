package com.cnn;

import com.cnn.survive.Dog;
import com.cnn.survive.Food;
import com.cnn.survive.Hhhh;
import com.spring.context.CNApplicationContext;

import java.util.Arrays;

public class TestSpring {

    public static void main(String[] args){

        CNApplicationContext cnApplicationContext = new CNApplicationContext(TestConfig.class);
        /**
         * 关于 web 的一些设想
         * 将这个初始化上下文的代码再包一层，
         * 然后上下文里给一个是否 有controller的标识 给个回调
         * 如果有, 将这个上下文对象交付给 web 处理器 ， 然后web 处理器去做监听，然后把流程走下去。
         */


        System.out.println("******************************");
        Hhhh hhhh = (Hhhh) cnApplicationContext.getBean("hhhh");
        hhhh.test();

        System.out.println("******************************");

        Food food = (Food) cnApplicationContext.getBean("food");
        food.test();
        food.eat();

        System.out.println("******************************");
        Dog dog = (Dog) cnApplicationContext.getBean("dog");
        dog.test();
    }

}
