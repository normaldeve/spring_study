package com.normaldev.concurrencycoupon.controller;

import com.normaldev.concurrencycoupon.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 *
 * @author junnukim1007gmail.com
 * @date 26. 1. 3.
 */
@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @PostMapping("/issue")
    public ResponseEntity<String> issueCoupon(
            @RequestParam Long userId,
            @RequestParam Long couponId
    ) {

        try {
            couponService.issue(userId, couponId);
            return ResponseEntity.ok("쿠폰 발급 성공");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("쿠폰 발급 실패: " + e.getMessage());
        }
    }

    @GetMapping("/stock")
    public ResponseEntity<Integer> getStock(
            @RequestParam Long couponId
    ) {
        Integer stock = couponService.getStock(couponId);

        return ResponseEntity.ok(stock);
    }
}
