package com.cnn.survive;

import com.spring.annotation.Component;
import com.spring.annotation.aop.After;
import com.spring.annotation.aop.Aspect;
import com.spring.annotation.aop.Before;

@Aspect("food") //加这个注解 作用域会设置为prototype
@Component("testAop")
public class TestAop {

    @Before
    public void before(){
        System.out.println("food的前置通知");
    }

    @After
    public void after(){
        System.out.println("food的后置通知");
    }

}
