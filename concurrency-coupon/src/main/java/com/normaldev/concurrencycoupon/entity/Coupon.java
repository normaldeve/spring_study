package com.normaldev.concurrencycoupon.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Coupon Entity
 *
 * @author junnukim1007gmail.com
 * @date 25. 12. 26.
 */
@Entity
@Table(name = "coupon")
public class Coupon {

    @Id
    private Long id;

    @Column(nullable = false)
    private int stock;

    protected Coupon() {
    }

    public Coupon(Long id, int stock) {
        this.id = id;
        this.stock = stock;
    }

    public int getStock() {
        return stock;
    }

    public void decrease() {
        this.stock -= 1;
    }
}
