package com.jellymold.engine;

import org.restlet.Component;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Amee {

    public static void main(String[] args) throws Exception {
        ApplicationContext springContext = new ClassPathXmlApplicationContext(
                new String[]{
                        "applicationContext.xml",
                        "applicationContext-ml.xml",
                        "applicationContext-server.xml"});
        ((Component) springContext.getBean("top")).start();
    }
}
