package com.chheang.mengheak.logisticsapis.entities.auth;

import com.chheang.mengheak.logisticsapis.entities.base.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "user_accounts", indexes = {
        @Index(name = "idx_user_accounts_email", columnList = "email")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class UserAccount extends AuditableEntity {

    @Id
    @GeneratedValue
    @Column(name = "user_id")
    private UUID id;

    @Column(nullable = false, unique = true, length = 120)
    private String email;

    @Column(nullable = false, length = 120)
    private String passwordHash;

    @Column(length = 120)
    private String fullName;

    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = true;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"),
            uniqueConstraints = @UniqueConstraint(name = "uq_user_role", columnNames = {"user_id", "role_id"})
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();
}
