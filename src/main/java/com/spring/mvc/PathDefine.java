package com.spring.mvc;

public class PathDefine {

    /**
     * 存储HTTP相关信息
     */

    public PathDefine(String httpMethod, String httpPath) {
        this.httpMethod = httpMethod;
        this.httpPath = httpPath;
    }

    private String httpMethod; //HTTP请求方法
    private String httpPath; //HTTP请求路径

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getHttpPath() {
        return httpPath;
    }

    public void setHttpPath(String httpPath) {
        this.httpPath = httpPath;
    }
}
