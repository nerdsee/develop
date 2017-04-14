package org.stoevesand.findow.swagger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import io.swagger.jaxrs.config.BeanConfig;

public class Bootstrap extends HttpServlet {
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        System.out.println("Init Bootstrap for Swagger");
        
        BeanConfig beanConfig = new BeanConfig();
        beanConfig.setVersion("1.0.1");
        beanConfig.setTitle("findow");
        beanConfig.setSchemes(new String[]{"https", "http"});
        //beanConfig.setHost("localhost:8080");
        beanConfig.setBasePath("/findow/v1");
        beanConfig.setResourcePackage("org.stoevesand.findow.rest");
        beanConfig.setScan(true);
    }
}