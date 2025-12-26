package com.normaldev.spring_container.example;

import com.normaldev.spring_container.context.MyApplicationContext;

/**
 *
 * @author junnukim1007gmail.com
 * @date 25. 12. 24.
 */
public class Main {
    public static void main(String[] args) {
        MyApplicationContext ac = new MyApplicationContext("com.normaldev.spring_container.example");

        System.out.println("=========== MyAutowired 주입 버전 ===========");

        TestService bookService = ac.getBean(TestService.class);
        bookService.testMethod();

        System.out.println("=========== 생성자 주입 버전 ===========");

        TestService2 bookService2 = ac.getBean(TestService2.class);
        bookService2.testMethod();
    }
}
