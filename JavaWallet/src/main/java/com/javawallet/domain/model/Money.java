package com.javawallet.domain.model;

import com.javawallet.domain.exception.domain.CurrencyMismatchException;
import com.javawallet.domain.exception.domain.InvalidAmountException;
import com.javawallet.domain.exception.domain.InvalidCurrencyException;
import jakarta.persistence.Embeddable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

@Embeddable
public class Money implements Comparable<Money> {

    private BigDecimal amount;
    private String currency;

    protected Money(){}

    private Money(BigDecimal amount, String currency) {
        this.amount = amount.setScale(2, RoundingMode.HALF_EVEN);
        this.currency = currency;
    }

    public static Money of(BigDecimal amount, String currency) {
        if (amount == null){
            throw new InvalidAmountException("amount must not be null");
        }
        if (currency == null) {
            throw new InvalidCurrencyException("currency must not be null");
        }
        return new Money(amount, currency);
    }

    public static Money zero(String currency) {
        return new Money(BigDecimal.ZERO, currency);
    }

    Money add(Money other) {
        checkCurrency(other);
        return new Money(this.amount.add(other.amount), this.currency);
    }

    Money subtract(Money other) {
        checkCurrency(other);
        return new Money(this.amount.subtract(other.amount), this.currency);
    }

    public boolean isPositive() {
        return this.amount.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isNegative() {
        return this.amount.compareTo(BigDecimal.ZERO) < 0;
    }

    private void checkCurrency(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new CurrencyMismatchException(this.currency, other.currency);
        }
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Money money = (Money) o;
        return this.currency.equals(money.currency) &&
                this.amount.compareTo(money.amount) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount.doubleValue(), currency);
    }

    @Override
    public String toString() { return amount.toString() + " " + currency; }

    @Override
    public int compareTo(Money o) {
        checkCurrency(o);
        return this.amount.compareTo(o.amount);
    }
}