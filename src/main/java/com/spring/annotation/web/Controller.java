package com.spring.annotation.web;

import com.spring.annotation.Component;

import javax.annotation.Nonnull;
import java.lang.annotation.*;


/**
 * 控制器
 * 添加在类上会自动被认为是一个Bean
 */

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Controller{
    @Nonnull
    String value() default ""; //自定义Bean的名称
}
