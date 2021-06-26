package com.cnn.survive;

import com.spring.annotation.Component;

@Component("food")
public class FoodImpl implements Food {

    @Override
    public void test(){
        System.out.println("FoodImpl的代理方法test");
    }

    @Override
    public void eat(){
        System.out.println("foodImpl的代理方法eat");
    }

    public void find(){
        System.out.println("这是foodImpl自己的方法find");
    }

}
