package com.normaldev.concurrencycoupon;

import com.normaldev.concurrencycoupon.entity.Coupon;
import com.normaldev.concurrencycoupon.repository.CouponIssueRepository;
import com.normaldev.concurrencycoupon.repository.CouponRepository;
import com.normaldev.concurrencycoupon.service.CouponService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author junnukim1007gmail.com
 * @date 25. 12. 26.
 */
@SpringBootTest
class CouponSynchronizedConcurrencyTest {

    @Autowired
    CouponService couponService;

    @Autowired
    CouponRepository couponRepository;

    @Autowired
    CouponIssueRepository issueRepository;

    @BeforeEach
    void setUp() {
        // 기존 데이터 정리
        issueRepository.deleteAll();
        couponRepository.deleteAll();

        couponRepository.save(new Coupon(1L, 100));
    }

    @Test
    void synchronized로_1000명이_동시에_요청한다() throws Exception {
        int threadCount = 1000;
        ExecutorService executor = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            long userId = i;
            executor.submit(() -> {
                try {
                    couponService.issue(userId);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        Coupon coupon = couponRepository.findById(1L).get();
        long issuedCount = issueRepository.count();

        System.out.println("남은 쿠폰 수 = " + coupon.getStock());
        System.out.println("발급된 쿠폰 수 = " + issuedCount);

    }
}
