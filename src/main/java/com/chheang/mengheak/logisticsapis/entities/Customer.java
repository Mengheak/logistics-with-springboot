package com.chheang.mengheak.logisticsapis.entities;

import com.chheang.mengheak.logisticsapis.entities.auth.UserAccount;
import com.chheang.mengheak.logisticsapis.entities.base.AuditableEntity;
import com.chheang.mengheak.logisticsapis.entities.embeddable.Address;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

/** The party that books shipments and gets billed for them. */
@Entity
@Table(name = "customers", indexes = {
        @Index(name = "idx_customers_code", columnList = "customer_code")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Customer extends AuditableEntity {

    @Id
    @GeneratedValue
    @Column(name = "customer_id")
    private UUID id;

    @Column(name = "customer_code", nullable = false, unique = true, length = 30)
    private String customerCode;

    @Column(nullable = false, length = 160)
    private String name;

    @Column(length = 120)
    private String email;

    @Column(length = 30)
    private String phone;

    @Embedded
    private Address billingAddress;

    /** Optional login, set when the customer uses the self-service portal. */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private UserAccount userAccount;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;
}
