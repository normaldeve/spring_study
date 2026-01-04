package com.normaldev.concurrencycoupon.service;

import com.normaldev.concurrencycoupon.entity.Coupon;
import com.normaldev.concurrencycoupon.entity.CouponIssue;
import com.normaldev.concurrencycoupon.repository.CouponIssueRepository;
import com.normaldev.concurrencycoupon.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 비관적 락을 적용한 코드
 *
 * @author junnukim1007gmail.com
 * @date 26. 1. 3.
 */
@Primary
@Service
@RequiredArgsConstructor
public class OptimisticCouponService implements CouponService{

    private final CouponRepository couponRepository;
    private final CouponIssueRepository issueRepository;

    @Override
    @Retryable(
            retryFor = ObjectOptimisticLockingFailureException.class,
            maxAttempts = 20,
            backoff = @Backoff(delay = 50)
    )
    @Transactional
    public void issue(Long userId, Long couponId) {
        Coupon coupon = couponRepository.findByIdWithOptimisticLock(couponId)
                .orElseThrow(() -> new IllegalArgumentException("쿠폰을 찾을 수 없습니다."));

        if (coupon.getStock() <= 0) {
            throw new IllegalArgumentException("쿠폰 재고가 없습니다.");
        }

        coupon.decrease();

        issueRepository.save(new CouponIssue(1L, userId));
    }

    @Recover
    public void recover(
            ObjectOptimisticLockingFailureException e, Long userId, Long couponId
    ) {
        throw new IllegalStateException("쿠폰 발급 재시도 초과", e);
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getStock(Long couponId) {
        Coupon coupon = couponRepository.findByIdWithOptimisticLock(couponId)
                .orElseThrow(() -> new IllegalArgumentException("쿠폰을 찾을 수 없습니다"));

        return coupon.getStock();
    }
}
