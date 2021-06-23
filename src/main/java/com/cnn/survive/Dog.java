package com.cnn.survive;

import com.spring.annotation.Autowired;
import com.spring.annotation.Component;
import com.spring.annotation.Scope;

@Component("dog")
@Scope("prototype")
public class Dog implements Annimal{

    private String name;
    private int id ;

    private Person person;

    @Autowired(required = false)
    public Dog (Person person){
        this.person = person;
    }

    @Autowired(required = false)
    public Dog(String name,int id){
        this.name = name;
        this.id = id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public void test(){
        System.out.println("Dog的"+person);
    }
}
