package com.cnn.survive;

import com.spring.annotation.Component;
import com.spring.beansfactory.InitializingBean;

@Component("testService")
public class TestService implements InitializingBean {

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("testService的Spring应用程初始化操作");
    }

}
