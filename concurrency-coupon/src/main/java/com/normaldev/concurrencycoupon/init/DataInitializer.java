package com.normaldev.concurrencycoupon.init;

import com.normaldev.concurrencycoupon.entity.Coupon;
import com.normaldev.concurrencycoupon.entity.User;
import com.normaldev.concurrencycoupon.repository.CouponRepository;
import com.normaldev.concurrencycoupon.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

/**
 * App 실행 시 초기 데이터 생성
 *
 * @author junnukim1007gmail.com
 * @date 26. 1. 3.
 */
@Configuration
public class DataInitializer {

    @Bean
    @Transactional
    public CommandLineRunner initData(
            CouponRepository couponRepository,
            UserRepository userRepository
    ) {
        return args -> {
            couponRepository.deleteAll();
            userRepository.deleteAll();

            Coupon coupon = Coupon.builder()
                    .id(1L)
                    .stock(100)
                    .build();

            couponRepository.save(coupon);
            System.out.println("쿠폰 생성 완료 - ID: " + coupon.getId() + ", 재고: " + coupon.getStock());

            for (long i = 0; i <= 100; i++) {
                User user = new User(i);
                userRepository.save(user);
            }

            long userCount = userRepository.count();
            System.out.println("총 사용자 수: " + userCount);
        };
    }
}
