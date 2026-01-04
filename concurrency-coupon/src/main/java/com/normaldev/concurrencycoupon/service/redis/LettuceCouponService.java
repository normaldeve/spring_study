package com.normaldev.concurrencycoupon.service.redis;

import com.normaldev.concurrencycoupon.entity.Coupon;
import com.normaldev.concurrencycoupon.repository.CouponRepository;
import com.normaldev.concurrencycoupon.service.CouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Lettuce로 분산 락 구현
 *
 * @author junnukim1007gmail.com
 * @date 26. 1. 4.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LettuceCouponService implements CouponService {

    private final CouponRepository couponRepository;
    private final CouponIssueService couponIssueService;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String LOCK_KEY_PREFIX = "coupon:lock:";
    private static final long LOCK_TIMEOUT_SECONDS = 3L;
    private static final long RETRY_DELAY_MS = 50L;
    private static final int MAX_RETRY_COUNT = 100;

    @Override
    public void issue(Long userId, Long couponId) {

        String lockKey = LOCK_KEY_PREFIX + couponId;

        if (!acquireLock(lockKey)) {
            throw new IllegalArgumentException("락 획득에 실패하였습니다. 잠시 후 다시 시도해주세요");
        }

        try {
            // 실제 비즈니스 로직 수행
            couponIssueService.issue(userId, couponId);
        } finally {
            // 락 해제
            releaseLock(lockKey);
        }

    }

    private boolean acquireLock(String key) {
        int retryCount = 0;

        while (retryCount < MAX_RETRY_COUNT) {
            Boolean success = redisTemplate.opsForValue()
                    .setIfAbsent(key, "locked", Duration.ofSeconds(LOCK_TIMEOUT_SECONDS));

            if (Boolean.TRUE.equals(success)) {
                return true;
            }

            retryCount++;

            try {
                Thread.sleep(RETRY_DELAY_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalArgumentException("락 획득 중 인터럽트 발생", e);
            }
        }

        return false;
    }

    private void releaseLock(String key) {
        Boolean deleted = redisTemplate.delete(key);
        if (Boolean.TRUE.equals(deleted)) {
            log.debug("락 해제 성공: {}", key);
        }
    }

    @Override
    public Integer getStock(Long couponId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new IllegalArgumentException("쿠폰을 찾을 수 없습니다"));

        return coupon.getStock();
    }
}
