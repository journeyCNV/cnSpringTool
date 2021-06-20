package com.spring.beansfactory.config;

/**
 * 模仿Spring源码
 *
 * BeanPostProcessor 是一个回调机制的扩展点，
 * 它的核心工作点是在 bean 的初始化前后做一些额外的处理
 * （预初始化 bean 的属性值、注入特定的依赖，甚至扩展生成代理对象等）
 */
public interface BeanPostProcessor {

    /**
     * 初始化前
     * @param bean
     * @param beanName
     * @return
     */
    default Object postProcessBeforeInitialization(Object bean, String beanName) {
        return bean;
    }

    /**
     * 初始化后
     * @param bean
     * @param beanName
     * @return
     */
    default Object postProcessAfterInitialization(Object bean, String beanName)  {
        return bean;
    }

}
