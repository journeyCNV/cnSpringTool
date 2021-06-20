package com.spring.beansfactory;

/**
 * 模仿Spring
 * InitializingBean接口为bean提供了属性初始化后的处理方法，
 * 它只包括afterPropertiesSet方法，凡是继承该接口的类，在bean的属性初始化后都会执行该方法。
 */
public interface InitializingBean {

    /**
     * spring初始化bean有两种方式：
     * 第一：实现InitializingBean接口，继而实现afterPropertiesSet的方法
     * 第二：反射原理，配置文件使用init-method标签直接注入bean
     *
     * 相同点： 实现注入bean的初始化。
     *
     * 不同点：
     * （1）实现的方式不一致。
     * （2）接口比配置效率高，但是配置消除了对spring的依赖。而实现InitializingBean接口依然采用对spring的依赖。
     */

    //从方法名afterPropertiesSet也可以清楚的理解该方法是在属性设置后才调用的
    void afterPropertiesSet() throws Exception;

}
