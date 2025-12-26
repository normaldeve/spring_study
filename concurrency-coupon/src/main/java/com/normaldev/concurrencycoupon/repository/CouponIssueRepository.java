package com.normaldev.concurrencycoupon.repository;

import com.normaldev.concurrencycoupon.entity.CouponIssue;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author junnukim1007gmail.com
 * @date 25. 12. 26.
 */
public interface CouponIssueRepository extends JpaRepository<CouponIssue, Long> {
}
