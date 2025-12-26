package com.normaldev.spring_container.example;

import com.normaldev.spring_container.stereotype.MyAutowired;
import com.normaldev.spring_container.stereotype.MyComponent;

/**
 *
 * @author junnukim1007gmail.com
 * @date 25. 12. 24.
 */
@MyComponent
public class TestService {

    @MyAutowired
    private TestRepository testRepository;

    public void testMethod() {
        testRepository.findAll().forEach(System.out::println);
    }
}
