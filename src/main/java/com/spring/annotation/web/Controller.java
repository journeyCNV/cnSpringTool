package com.spring.annotation.web;

import com.spring.annotation.Component;

import javax.annotation.Nonnull;
import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Controller{
    @Nonnull
    String value() default "";
}
