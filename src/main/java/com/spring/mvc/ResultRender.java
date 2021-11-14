package com.spring.mvc;

import com.alibaba.fastjson.JSON;
import com.spring.annotation.web.ResponseBody;
import com.spring.common.CastUtil;
import com.spring.context.CNApplicationContext;
import com.spring.mvc.server.TomcatSupport;
import com.spring.mvc.servlet.ControllerDefine;
import com.spring.mvc.servlet.ModelAndView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 结果执行器
 */
public class ResultRender {

    private static Logger log = LoggerFactory.getLogger(ResultRender.class);

    private CNApplicationContext appContext;

    public ResultRender(CNApplicationContext appContext) {
        this.appContext = appContext;
    }

    /**
     * 执行Controller的方法
     * 通过反射调用Controller中的方法
     */
    public void invokeController(HttpServletRequest request, HttpServletResponse response, ControllerDefine controllerDefine){
        //获取HttpServletRequest所有参数
        Map<String,String> requestParam = getRequestParams(request);
        //传入的参数值赋值
        List<Object> methodsParams = instanceOfMethodParams(controllerDefine.getMethodParams(),requestParam);

        //TODO
        // 一个Controller里可以有多个请求方法的，要加一下

        //这里直接getName还需要再商榷一下
        Object controller = appContext.getBean(controllerDefine.getControllerClass().getName());
        Method invokeMethod = controllerDefine.getInvokeMethod();
        invokeMethod.setAccessible(true);
        Object result;
        try {
            if(methodsParams.size() == 0){
                result = invokeMethod.invoke(controller);
            }else {
                result = invokeMethod.invoke(controller,methodsParams.toArray());
            }
        } catch (Exception e) {
            //e.printStackTrace();
            throw new RuntimeException(e);
        }
        resultResolver(controllerDefine,result,request,response);
    }

    /**
     * 获取HTTP中的参数
     */
    private Map<String,String> getRequestParams(HttpServletRequest request){
        Map<String,String> paramMap = new HashMap<>();
        request.getParameterMap().forEach(
                (paramName,paramValues)->{
                    if(paramValues.length>0){
                        paramMap.put(paramName,paramValues[0]);
                    }
                }
        );
        return paramMap;
    }

    //TODO
    // 获取path中的参数

    /**
     * 实例化方法参数
     * TODO
     *  传入object ?
     */
    private List<Object> instanceOfMethodParams(Map<String,Class<?>> methodParams,
                                                Map<String,String> requestParams){
        //如果请求参数存在，就赋值
        return methodParams.keySet().stream()
                .map(
                        paramName ->{
                            Class<?> type = methodParams.get(paramName);
                            String requestValue = requestParams.get(paramName);
                            Object value;
                            if(requestValue==null){
                                value = CastUtil.primitiveNull(type);
                            }else{
                                value = CastUtil.convert(type,requestValue);
                            }
                            return value;
                        }
                ).collect(Collectors.toList());
    }

    /**
     * Controller方法执行后返回值解析
     */
    private void resultResolver(ControllerDefine controllerDefine,
                                Object result,
                                HttpServletRequest request,
                                HttpServletResponse response){
        if(result == null)
            return;

        boolean isJson = controllerDefine.getInvokeMethod().isAnnotationPresent(ResponseBody.class);
        if(isJson){ //是Json最好
            //设置响应头
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            //向响应中写入数据
            try(PrintWriter writer = response.getWriter()){
                writer.write(JSON.toJSONString(result));
                writer.flush();
            }catch (IOException e){
                e.printStackTrace(); //暂时这样
                //TODO
                // 异常统一处理
            }
        }else {
            String path;
            if(result instanceof ModelAndView) {
                //TODO
                //我觉得视图这种方式可以抛弃，只做前后端分离
                ModelAndView modelAndView = (ModelAndView) result;
                path = modelAndView.getView();
                Map<String,Object> model = modelAndView.getModel();
                if(!model.isEmpty()) {
                    for (Map.Entry<String,Object> entry : model.entrySet()) {
                        request.setAttribute(entry.getKey(),entry.getValue());
                    }
                }
            }else if (result instanceof String) {
                path = (String) result;
            }else {
                throw new RuntimeException("返回类型不合法！");
            }
            try{
                // forward
                request.getRequestDispatcher("/templates/"+path).forward(request,response);
            } catch (Exception e) {
                log.error("请求转发失败",e);
                e.printStackTrace();
                //TODO
                // 统一异常处理
            }
        }

    }

}
