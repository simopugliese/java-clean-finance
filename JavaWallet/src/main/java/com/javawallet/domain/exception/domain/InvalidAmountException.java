package com.javawallet.domain.exception.domain;

public class InvalidAmountException extends DomainException {
    public InvalidAmountException(String message) {
        super(message);
    }
}