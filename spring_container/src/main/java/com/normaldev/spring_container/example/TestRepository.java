package com.normaldev.spring_container.example;

import com.normaldev.spring_container.stereotype.MyComponent;

import java.util.Arrays;
import java.util.List;

/**
 *
 * @author junnukim1007gmail.com
 * @date 25. 12. 24.
 */
@MyComponent
public class TestRepository {
    public List<String> findAll() {
        return Arrays.asList(
                "Hello",
                "This",
                "is",
                "Test Repository"
        );
    }
}
