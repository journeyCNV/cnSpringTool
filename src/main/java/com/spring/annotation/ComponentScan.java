package com.spring.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.annotation.Nonnull;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE) //写在类上
public @interface ComponentScan{

    //定义一个 指定扫描路径的属性
    @Nonnull
    String value();

}
