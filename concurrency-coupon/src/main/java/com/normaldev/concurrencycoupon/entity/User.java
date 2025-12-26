package com.normaldev.concurrencycoupon.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * User Entity
 *
 * @author junnukim1007gmail.com
 * @date 25. 12. 26.
 */
@Entity
@Table(name = "user")
public class User {

    @Id
    private Long id;
}
