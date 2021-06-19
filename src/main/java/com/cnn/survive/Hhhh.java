package com.cnn.survive;

import com.spring.Autowired;
import com.spring.BeanNameAware;
import com.spring.Component;
import com.spring.Scope;

@Component("hhhh")
@Scope("prototype")
public class Hhhh implements BeanNameAware {

    @Autowired
    private TestService testService;

    @Autowired
    private Food food;

    private String beanName;

    @Override
    public void setBeanName(String name){
        beanName = name;
    }

    public void test(){
        //希望拿对象的时候已经赋好值了
        System.out.println("hhhh依赖的Bean1："+testService);
        System.out.println("hhhh依赖的Bean2："+food);
        System.out.println("hhhh的Bean名字是："+beanName); //通过回调得到beanName
    }

}
