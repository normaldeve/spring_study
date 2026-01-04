package com.normaldev.concurrencycoupon.service.db_lock;

import com.normaldev.concurrencycoupon.entity.Coupon;
import com.normaldev.concurrencycoupon.entity.CouponIssue;
import com.normaldev.concurrencycoupon.repository.CouponIssueRepository;
import com.normaldev.concurrencycoupon.repository.CouponRepository;
import com.normaldev.concurrencycoupon.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 비관적 락을 적용한 코드
 *
 * @author junnukim1007gmail.com
 * @date 26. 1. 3.
 */
@Service
@RequiredArgsConstructor
public class PessimisticCouponService implements CouponService {

    private final CouponRepository couponRepository;
    private final CouponIssueRepository issueRepository;

    @Transactional
    public void issue(Long userId, Long couponId) {
        Coupon coupon = couponRepository.findByIdWithPessimisticLock(couponId)
                .orElseThrow(() -> new IllegalArgumentException("쿠폰을 찾을 수 없습니다."));

        if (coupon.getStock() <= 0) {
            throw new IllegalArgumentException("쿠폰 재고가 없습니다.");
        }

        coupon.decrease();

        issueRepository.save(new CouponIssue(1L, userId));
    }

    @Transactional(readOnly = true)
    public Integer getStock(Long couponId) {
        Coupon coupon = couponRepository.findByIdWithPessimisticLock(couponId)
                .orElseThrow(() -> new IllegalArgumentException("쿠폰을 찾을 수 없습니다"));

        return coupon.getStock();
    }
}
