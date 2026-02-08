package com.javawallet.domain.exception.domain;

public class InsufficientFundsException extends DomainException {
    public InsufficientFundsException(String walletId) {
        super("Insufficient funds in wallet: " + walletId);
    }
}