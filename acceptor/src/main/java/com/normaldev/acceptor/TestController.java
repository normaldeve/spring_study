package com.normaldev.acceptor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Test Controller
 *
 * @author junnukim1007gmail.com
 * @date 25. 12. 26.
 */
@RestController
public class TestController {

    @GetMapping("/test")
    public String test() throws InterruptedException {
        // 요청 처리 지연: 10초 sleep으로 동시 연결이 쌓이는 상황 시뮬레이션
        // (실제로는 비즈니스 로직에 따라 조정. 이로 인해 큐가 쌓임)
        Thread.sleep(100);
        return "Processed";
    }
}
