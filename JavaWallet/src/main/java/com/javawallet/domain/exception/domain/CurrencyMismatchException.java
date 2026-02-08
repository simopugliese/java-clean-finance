package com.javawallet.domain.exception.domain;

public class CurrencyMismatchException extends DomainException {
    public CurrencyMismatchException(String firstCurrency, String secondCurrency) {
        super("Cannot operate on different currencies: " + firstCurrency + " vs " + secondCurrency);
    }
}