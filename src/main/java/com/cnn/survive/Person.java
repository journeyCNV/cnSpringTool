package com.cnn.survive;

import com.spring.annotation.Component;

@Component("person")
public class Person {
    public String name = "";

    public void setName(String name) {
        this.name = name;
    }
}
