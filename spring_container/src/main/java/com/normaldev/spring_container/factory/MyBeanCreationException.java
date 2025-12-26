package com.normaldev.spring_container.factory;

/**
 * Custom BeanCreationException
 *
 * @author junnukim1007gmail.com
 * @date 25. 12. 24.
 */
public class MyBeanCreationException extends RuntimeException {
    public MyBeanCreationException(String message) { super(message); }
    public MyBeanCreationException(String message, Throwable cause) { super(message, cause); }
}