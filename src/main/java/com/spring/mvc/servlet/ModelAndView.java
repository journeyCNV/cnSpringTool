package com.spring.mvc.servlet;

import java.util.HashMap;
import java.util.Map;

/**
 * Web MVC 框架中模型和视图的持有者
 * 让控制器可以同时返回模型和视图
 */

public class ModelAndView {
    //页面路径
    private String view;

    //页面data数据
    private Map<String,Object> model = new HashMap<>();

    public ModelAndView setView(String view){
        this.view = view;
        return this;
    }

    public String getView(){
        return view;
    }

    public ModelAndView addObject(String attributeName, Object attributeValue){
        model.put(attributeName,attributeValue);
        return this;
    }

    public ModelAndView addAllObjects(Map<String,?> modelMap){
        model.putAll(modelMap);
        return this;
    }

    public Map<String ,Object> getModel(){
        return model;
    }

}
