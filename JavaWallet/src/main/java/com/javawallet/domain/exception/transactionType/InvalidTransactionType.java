package com.javawallet.domain.exception.transactionType;

public class InvalidTransactionType extends IllegalArgumentException {
    public InvalidTransactionType(String message) {
        super(message);
    }
}
