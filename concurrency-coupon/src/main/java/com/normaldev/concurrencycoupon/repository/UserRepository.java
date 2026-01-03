package com.normaldev.concurrencycoupon.repository;

import com.normaldev.concurrencycoupon.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author junnukim1007gmail.com
 * @date 26. 1. 3.
 */
public interface UserRepository extends JpaRepository<User, Long> {
}
