package com.spring.mvc.server;

import com.spring.config.CNConfiguration;
import com.spring.mvc.DispatcherServlet;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.servlets.DefaultServlet;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.StandardRoot;
import org.apache.jasper.servlet.JspServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;


/**
 * @author nuo
 * @create 2021/11/14
 *
 * Tomcat服务器支持
 *
 * //TODO 完善解释注释
 */

public class TomcatSupport implements Server{

    private static Logger log = LoggerFactory.getLogger(TomcatSupport.class);

    private Tomcat tomcat;

    public TomcatSupport(CNConfiguration configuration) {
        this.tomcat = new Tomcat();
        tomcat.setBaseDir(configuration.getDocBase());
        tomcat.setPort(configuration.getServerPort());

        try {
            File root = getRoot();
            File webContFolder = new File(root.getAbsolutePath(), configuration.getResourcePath());
            if (!webContFolder.exists()) {
                webContFolder = Files.createTempDirectory("default-doc-base").toFile();
            }
            log.info("Tomcat: configuring app with basedir: [{}]",webContFolder.getAbsolutePath());
            StandardContext ctx = (StandardContext) tomcat.addWebapp(configuration.getContextPath(),webContFolder.getAbsolutePath());
            ctx.setParentClassLoader(this.getClass().getClassLoader());
            WebResourceRoot resourceRoot = new StandardRoot(ctx);
            ctx.setResources(resourceRoot);
            tomcat.addServlet("","jspServlet",new JspServlet()).setLoadOnStartup(3);
            tomcat.addServlet("","defaultServlet",new DefaultServlet()).setLoadOnStartup(1);
            tomcat.addServlet("","dispatcherServlet",new DispatcherServlet()).setLoadOnStartup(0);
            ctx.addServletMappingDecoded("/templates/" + "*", "jspServlet");
            ctx.addServletMappingDecoded("/static/" + "*", "defaultServlet");
            ctx.addServletMappingDecoded("/*", "dispatcherServlet");
        }  catch (IOException e) {
            //TODO 原因再细化一下
            log.error("初始化Tomcat失败 : Files.createTempDirectory(\"default-doc-base\").toFile()",e);
            // e.printStackTrace();
            throw new RuntimeException(e);
        }

    }

    private File getRoot() {
        try {
            File root;
            String runningJarPath = this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI().getPath().replaceAll("\\\\","/");
            int lastIndexOf = runningJarPath.lastIndexOf("/target/");
            if (lastIndexOf < 0) {
                root = new File("");
            } else {
                root = new File(runningJarPath.substring(0, lastIndexOf));
            }
            log.info("Tomcat:application resolved root folder: [{}]",root.getAbsolutePath());
            return root;
        } catch (URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void startServer() throws Exception {
        tomcat.start();
        String address = tomcat.getServer().getAddress();
        int port = tomcat.getConnector().getPort();
        log.info("local address : http://{}:{}", address,port);
        tomcat.getServer().await();
    }

    @Override
    public void stopServer() throws Exception {
        tomcat.stop();
    }
}
