package com.normaldev.concurrencycoupon.entity;

import jakarta.persistence.*;

/**
 * Coupon-User 중간 테이블
 *
 * @author junnukim1007gmail.com
 * @date 25. 12. 26.
 */
@Entity
@Table(
        name = "coupon_issue",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"coupon_id", "user_id"})
        }
)
public class CouponIssue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long couponId;
    private Long userId;

    protected CouponIssue() {}

    public CouponIssue(Long couponId, Long userId) {
        this.couponId = couponId;
        this.userId = userId;
    }
}
