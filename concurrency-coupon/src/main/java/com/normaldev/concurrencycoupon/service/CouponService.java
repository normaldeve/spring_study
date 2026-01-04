package com.normaldev.concurrencycoupon.service;

/**
 *
 * @author junnukim1007gmail.com
 * @date 26. 1. 3.
 */
public interface CouponService {

    void issue(Long userId, Long couponId);

    Integer getStock(Long couponId);
}
