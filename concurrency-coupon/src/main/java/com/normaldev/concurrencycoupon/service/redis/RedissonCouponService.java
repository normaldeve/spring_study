package com.normaldev.concurrencycoupon.service.redis;

import com.normaldev.concurrencycoupon.entity.Coupon;
import com.normaldev.concurrencycoupon.repository.CouponRepository;
import com.normaldev.concurrencycoupon.service.CouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

/**
 *
 * @author junnukim1007gmail.com
 * @date 26. 1. 4.
 */
@Primary
@Slf4j
@Service
@RequiredArgsConstructor
public class RedissonCouponService implements CouponService {

    private final CouponRepository couponRepository;
    private final CouponIssueService couponIssueService;
    private final RedissonClient redissonClient;

    private static final String LOCK_KEY_PREFIX = "coupon:lock:";
    private static final long WAIT_TIME_SECONDS = 5L;  // 락 획득 대기 시간
    private static final long LEASE_TIME_SECONDS = 3L; // 락 자동 해제 시간

    @Override
    public void issue(Long userId, Long couponId) {
        String lockKey = LOCK_KEY_PREFIX + couponId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // tryLock: 락 획득 시도
            // waitTime: 락 획득을 위해 대기할 최대 시간
            // leaseTime: 락을 자동으로 해제할 시간 (Deadlock 방지)
            boolean available = lock.tryLock(WAIT_TIME_SECONDS, LEASE_TIME_SECONDS, TimeUnit.SECONDS);

            if (!available) {
                log.warn("락 획득 실패 - userId: {}, couponId: {}", userId, couponId);
                throw new IllegalStateException("쿠폰 발급 요청이 많습니다. 잠시 후 다시 시도해주세요.");
            }

            // 실제 비즈니스 로직 수행
            couponIssueService.issue(userId, couponId);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("쿠폰 발급 중 인터럽트 발생", e);
        } finally {
            // 락 해제 (현재 스레드가 락을 보유하고 있는 경우에만)
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.debug("락 해제 성공: {}", lockKey);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getStock(Long couponId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new IllegalArgumentException("쿠폰을 찾을 수 없습니다"));

        return coupon.getStock();
    }
}
