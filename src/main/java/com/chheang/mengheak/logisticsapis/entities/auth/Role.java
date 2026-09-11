package com.chheang.mengheak.logisticsapis.entities.auth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Role {

    @Id
    @GeneratedValue
    @Column(name = "role_id")
    private UUID id;

    /** ADMIN, DISPATCHER, DRIVER, CUSTOMER - stored without the Spring Security ROLE_ prefix. */
    @Column(nullable = false, unique = true, length = 30)
    private String name;

    @Column(length = 160)
    private String description;
}
