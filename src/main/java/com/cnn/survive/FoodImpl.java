package com.cnn.survive;

import com.spring.annotation.Component;

@Component("food")
public class FoodImpl implements Food {

    @Override
    public void test(){
        System.out.println("FoodImpl的代理方法");
    }

}
