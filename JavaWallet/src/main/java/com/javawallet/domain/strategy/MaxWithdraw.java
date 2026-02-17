package com.javawallet.domain.strategy;

import com.javawallet.domain.exception.domain.AmountNotAllowed;
import com.javawallet.domain.model.Transaction;
import com.javawallet.domain.model.TransactionType;
import com.javawallet.domain.model.Wallet;

import java.math.BigDecimal;

public class MaxWithdraw implements IRuleStrategy {
    private final BigDecimal maxAmount;

    public MaxWithdraw(BigDecimal maxAmount) {
        this.maxAmount = maxAmount;
    }

    @Override
    public void check(Wallet w, Transaction t) {
        if (t.getType() != TransactionType.WITHDRAWAL) return;
        BigDecimal toWithdraw = t.getMoney().getAmount();
        if (toWithdraw.compareTo(maxAmount) > 0) {
            throw new AmountNotAllowed("Amount too high, you cannot withdrawn it");
        }
    }
}