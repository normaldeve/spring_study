package com.normaldev.concurrencycoupon.service.redis;

import com.normaldev.concurrencycoupon.entity.Coupon;
import com.normaldev.concurrencycoupon.entity.CouponIssue;
import com.normaldev.concurrencycoupon.repository.CouponIssueRepository;
import com.normaldev.concurrencycoupon.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author junnukim1007gmail.com
 * @date 26. 1. 4.
 */
@Service
@RequiredArgsConstructor
public class CouponIssueService {

    private final CouponRepository couponRepository;
    private final CouponIssueRepository issueRepository;

    @Transactional
    public void issue(Long userId, Long couponId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow();

        if (coupon.getStock() <= 0) {
            throw new IllegalArgumentException("재고 없음");
        }

        coupon.decrease();
        issueRepository.save(new CouponIssue(couponId, userId));
    }
}
