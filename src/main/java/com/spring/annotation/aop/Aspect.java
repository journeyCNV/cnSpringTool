package com.spring.annotation.aop;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Aspect {
    /**
     * 标记在实现代理功能的类上
     */
    public String value() default "";

}
