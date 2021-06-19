package com.cnn;

import com.cnn.survive.Food;
import com.cnn.survive.Hhhh;
import com.spring.CNApplicationContext;

public class TestSpring {

    public static void main(String[] args){
        CNApplicationContext cnApplicationContext = new CNApplicationContext(TestConfig.class);
        //System.out.println(cnApplicationContext.getBean("hhhh"));
        Hhhh hhhh = (Hhhh) cnApplicationContext.getBean("hhhh");
        hhhh.test();
        //System.out.println(cnApplicationContext.getBean("hhhh"));
        //System.out.println(cnApplicationContext.getBean("hhhh"));

        Food food = (Food) cnApplicationContext.getBean("food");
        //System.out.println(food);
        food.test();

    }

}
