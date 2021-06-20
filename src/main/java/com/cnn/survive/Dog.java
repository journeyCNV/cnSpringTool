package com.cnn.survive;

import com.spring.annotation.Autowired;
import com.spring.annotation.Component;

@Component("dog")
public class Dog implements Annimal{

    private String name;
    private int id ;

    private Person master;

    /**
    @Autowired
    public Dog (Person master){
        this.master = master;
    }*/

    public Dog(String name,int id){
        this.name = name;
        this.id = id;
    }

    @Override
    public String getName() {
        return this.name;
    }
}
