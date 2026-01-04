package com.normaldev.concurrencycoupon.service;

import com.normaldev.concurrencycoupon.entity.Coupon;
import com.normaldev.concurrencycoupon.repository.CouponIssueRepository;
import com.normaldev.concurrencycoupon.repository.CouponRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 쿠폰 발급 동시성 테스트
 *
 * @author junnukim1007gmail.com
 * @date 26. 1. 3.
 */
@SpringBootTest
class CouponServiceTest {

    @Autowired
    private CouponService couponService;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private CouponIssueRepository couponIssueRepository;

    @BeforeEach
    void setUp() {
        couponIssueRepository.deleteAll();
        couponRepository.deleteAll();

        // 재고 100개인 쿠폰 생성
        couponRepository.saveAndFlush(
                Coupon.builder()
                        .id(1L)
                        .stock(100)
                .build());
    }

    @AfterEach
    void tearDown() {
        couponIssueRepository.deleteAll();
        couponRepository.deleteAll();
    }

    @Test
    @DisplayName("100명이 동시에 쿠폰 발급 요청")
    void concurrentIssueTest() throws InterruptedException {
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger success = new AtomicInteger(0);
        AtomicInteger fail = new AtomicInteger(0);

        // 테스트 시간 측정하기
        long startTime = System.nanoTime();

        for (int i = 0; i < threadCount; i++) {
            long userId = i;
            executorService.submit(() -> {
                try {
                    couponService.issue(userId, 1L);
                    success.incrementAndGet();
                } catch (Exception e) {
                    fail.incrementAndGet();
                    System.out.println("발급 실패: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        long endTIme = System.nanoTime();
        long testTime = (endTIme - startTime) / 1_000_000;

        Thread.sleep(1000);

        Coupon coupon = couponRepository.findById(1L).orElseThrow();
        long issuedCount = couponIssueRepository.count();

        System.out.println("=== 테스트 결과 ===");
        System.out.println("성공 카운트: " + success.get());
        System.out.println("실패 카운트: " + fail.get());
        System.out.println("최종 재고: " + coupon.getStock());
        System.out.println("실제 발급된 쿠폰 수: " + issuedCount);
        System.out.println("==================");
        System.out.println("테스트 걸린 시간(ms): " + testTime);
    }

    @Test
    @DisplayName("1000명이 동시에 100개 쿠폰 발급 - 재고 초과 방지 확인")
    void concurrentIssueWithOverStockTest() throws InterruptedException {
        // given
        int threadCount = 1000;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // 테스트 시간 측정하기
        long startTime = System.nanoTime();

        // when
        for (int i = 0; i < threadCount; i++) {
            long userId = i;
            executorService.submit(() -> {
                try {
                    couponService.issue(userId, 1L);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        long endTIme = System.nanoTime();
        long testTime = (endTIme - startTime) / 1_000_000;

        // then
        Thread.sleep(1000);

        Coupon coupon = couponRepository.findById(1L).orElseThrow();
        long issuedCount = couponIssueRepository.count();

        System.out.println("=== 재고 초과 테스트 결과 ===");
        System.out.println("요청 수: " + threadCount);
        System.out.println("성공 카운트: " + successCount.get());
        System.out.println("실패 카운트: " + failCount.get());
        System.out.println("최종 재고: " + coupon.getStock());
        System.out.println("실제 발급된 쿠폰 수: " + issuedCount);
        System.out.println("===========================");
        System.out.println("테스트 걸린 시간(ms): " + testTime);
    }
}