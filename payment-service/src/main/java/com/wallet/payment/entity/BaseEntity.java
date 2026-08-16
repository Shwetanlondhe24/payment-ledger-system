package com.wallet.payment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import java.time.Instant;

@MappedSuperclass
public class BaseEntity {

    @Column(updatable = false)
    private Instant createdAt;

    private Instant updatedAt;

    @PrePersist
    public void beforeInsert(){
        createdAt = Instant.now();
    }

    @PreUpdate
    public void beforeUpdate(){
        updatedAt = Instant.now();
    }
}
