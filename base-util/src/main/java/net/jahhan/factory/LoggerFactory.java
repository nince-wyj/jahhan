package net.jahhan.factory;

import org.slf4j.Logger;

public class LoggerFactory {

    private static final LoggerFactory loggerFactory = new LoggerFactory();

    public static LoggerFactory getInstance() {
        return loggerFactory;
    }

    public Logger getLogger(@SuppressWarnings("rawtypes") Class clazz) {
        return org.slf4j.LoggerFactory.getLogger(clazz);
    }

    public Logger getLogger(String name) {
        return org.slf4j.LoggerFactory.getLogger(name);
    }
}
