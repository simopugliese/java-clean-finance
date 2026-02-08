package com.javawallet.domain.exception.domain;

public abstract class DomainException extends RuntimeException {
    public DomainException(String message) {
        super(message);
    }
}