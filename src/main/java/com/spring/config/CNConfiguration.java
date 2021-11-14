package com.spring.config;

import com.spring.mvc.server.TomcatSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author nuo
 * @create 2021/11/14
 * 全局配置
 */
public class CNConfiguration {

    private static Logger log = LoggerFactory.getLogger(CNConfiguration.class);

    private String resourcePath = "src/main/resources/"; //资源目录

    private String viewPath = "/templates"; //jsp目录

    private String assetPath = "/static";

    private int serverPort = 8090;

    //tomcat docBase 目录
    private String docBase = "";

    //tomcat contextPath 目录
    private String contextPath = "";

    public String getResourcePath() {
        return resourcePath;
    }

    public String getViewPath() {
        return viewPath;
    }

    public String getAssetPath() {
        return assetPath;
    }

    public int getServerPort() {
        return serverPort;
    }

    public String getDocBase() {
        return docBase;
    }

    public String getContextPath() {
        return contextPath;
    }
}
