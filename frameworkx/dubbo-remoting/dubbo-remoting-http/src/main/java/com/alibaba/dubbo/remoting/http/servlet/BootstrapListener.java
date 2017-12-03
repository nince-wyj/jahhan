//package com.alibaba.dubbo.remoting.http.servlet;
//
//import javax.servlet.ServletContextEvent;
//import javax.servlet.ServletContextListener;
//import javax.servlet.annotation.WebListener;
//
//@WebListener
//public class BootstrapListener implements ServletContextListener {
//
//    public void contextInitialized(ServletContextEvent servletContextEvent) {
//        ServletManager.getInstance().addServletContext(ServletManager.EXTERNAL_SERVER_PORT, servletContextEvent.getServletContext());
//    }
//
//    public void contextDestroyed(ServletContextEvent servletContextEvent) {
//        ServletManager.getInstance().removeServletContext(ServletManager.EXTERNAL_SERVER_PORT);
//    }
//}
