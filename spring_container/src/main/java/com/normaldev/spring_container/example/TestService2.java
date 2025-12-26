package com.normaldev.spring_container.example;

import com.normaldev.spring_container.stereotype.MyComponent;

/**
 *
 * @author junnukim1007gmail.com
 * @date 25. 12. 24.
 */
@MyComponent
public class TestService2 {

    private final TestRepository testRepository;

    public TestService2(TestRepository testRepository) {
        this.testRepository = testRepository;
    }

    public void testMethod() {
        testRepository.findAll().forEach(System.out::println);
    }
}
