package com.normaldev.concurrencycoupon.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * User Entity
 *
 * @author junnukim1007gmail.com
 * @date 25. 12. 26.
 */
@Entity
@Table(name = "user")
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    private Long id;
}
