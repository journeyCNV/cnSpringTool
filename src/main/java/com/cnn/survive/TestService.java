package com.cnn.survive;

import com.spring.Component;
import com.spring.InitializingBean;
import com.spring.Scope;

@Component("testService")
public class TestService implements InitializingBean {

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("testService的Spring应用程初始化操作");
    }

}
