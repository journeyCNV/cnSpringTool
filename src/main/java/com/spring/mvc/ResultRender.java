package com.spring.mvc;

import com.alibaba.fastjson.JSON;
import com.spring.annotation.web.ResponseBody;
import com.spring.common.CastUtil;
import com.spring.mvc.servlet.ControllerDefine;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 结果执行器
 */
public class ResultRender {

    /**
     * 执行Controller的方法
     */
    public void invokeController(HttpServletRequest request, HttpServletResponse response, ControllerDefine controllerDefine){
        //获取HttpServletRequest所有参数
        Map<String,String> requestParam = getRequestParams(request);
        //TODO
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

    /**
     * 实例化方法参数
     */
    private List<Object> instanceOfMethodParams(Map<String,Class> methodParams,
                                                Map<String,String> requestParams){
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
        if(isJson){
            //设置响应头
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            //向响应中写入数据
            try(PrintWriter writer = response.getWriter()){
                writer.write(JSON.toJSONString(result));
                writer.flush();
            }catch (IOException e){
                e.printStackTrace(); //暂时这样
            }
        }else {
            //TODO
        }

    }

}
