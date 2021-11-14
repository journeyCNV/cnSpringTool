package com.spring.mvc;

import com.spring.context.CNApplicationContext;
import com.spring.mvc.server.TomcatSupport;
import com.spring.mvc.servlet.ControllerDefine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * @author nuo
 * @create 2021/11/13
 * DispatcherServlet 所有 http请求都由此 Servlet转发
 */
public class DispatcherServlet extends HttpServlet {

    private static Logger log = LoggerFactory.getLogger(DispatcherServlet.class);

    private ControllerHandler controllerHandler = new ControllerHandler();
    private ResultRender resultRender;

    public void setResultRender(CNApplicationContext context){
        resultRender = new ResultRender(context);
    }

    /**
     * 执行请求
     *
     * 根据请求的方法和路径获取对应的Controller
     * 再通过Controller执行目标方法
     */
    @Override
    public void service(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");

        String requestMethod = req.getMethod();
        String requestPath = req.getPathInfo();

        log.info("Config: {} {} ",requestMethod,requestPath);

        if(requestPath.endsWith("/")) {
            requestPath = requestPath.substring(0, requestPath.length()-1);
        }

        ControllerDefine controllerDefine = controllerHandler.getController(requestMethod,requestPath);
        if(null == controllerDefine) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        //执行目标处理方法
        resultRender.invokeController(req,resp,controllerDefine);
    }

}
