package com.spring.mvc.server;

/**
 * @author nuo
 * @create 2021/11/14
 *
 * 服务器统一接口
 * 默认服务器使用Tomcat
 */
public interface Server {
    /**
     * 启动服务器
     */
    void startServer() throws Exception;

    /**
     * 停止服务器
     */
    void stopServer() throws Exception;

}
