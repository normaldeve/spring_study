package com.normaldev.concurrencycoupon.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Coupon Entity
 *
 * @author junnukim1007gmail.com
 * @date 25. 12. 26.
 */
@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "coupon")
public class Coupon {

    @Id
    private Long id;

    @Column(nullable = false)
    private int stock;

    @Version
    private Long version;

    public void decrease() {
        this.stock -= 1;
    }
}
