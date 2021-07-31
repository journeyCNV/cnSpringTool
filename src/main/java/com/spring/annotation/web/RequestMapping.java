package com.spring.annotation.web;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 可以标记在类上或方法上
 * 通常使用在方法上，可以指明请求URL和请求方法
 */

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestMapping {
    String value() default ""; //请求路径
    RequestMethod method() default RequestMethod.GET;//请求方法
}
