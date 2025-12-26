package com.normaldev.concurrencycoupon.service;

import com.normaldev.concurrencycoupon.entity.Coupon;
import com.normaldev.concurrencycoupon.entity.CouponIssue;
import com.normaldev.concurrencycoupon.repository.CouponIssueRepository;
import com.normaldev.concurrencycoupon.repository.CouponRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author junnukim1007gmail.com
 * @date 25. 12. 26.
 */
@Service
public class CouponService {

    private final CouponRepository couponRepository;
    private final CouponIssueRepository issueRepository;

    public CouponService(
            CouponRepository couponRepository,
            CouponIssueRepository issueRepository
    ) {
        this.couponRepository = couponRepository;
        this.issueRepository = issueRepository;
    }

    @Transactional
    public void issue(Long userId) {
        Coupon coupon = couponRepository.findById(1L)
                .orElseThrow();

        if (coupon.getStock() <= 0) {
            return;
        }

        coupon.decrease();

        issueRepository.save(new CouponIssue(1L, userId));
    }
}
