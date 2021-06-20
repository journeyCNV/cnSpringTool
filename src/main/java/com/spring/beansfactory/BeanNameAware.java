package com.spring.beansfactory;

/**
 * 模仿spring的
 * BeanNameAware使对象知道容器中定义的 bean名称
 */
public interface BeanNameAware {

    void setBeanName(String name);

}
