package com.javawallet.domain.strategy;

import com.javawallet.domain.exception.domain.AmountNotAllowed;
import com.javawallet.domain.model.Transaction;
import com.javawallet.domain.model.TransactionType;
import com.javawallet.domain.model.Wallet;

public class MaxWithdraw implements IRuleStrategy{
    private final double maxAmount;
    public MaxWithdraw(double maxAmount) {
        this.maxAmount = maxAmount;
    }

    @Override
    public void check(Wallet w, Transaction t) {
        if (t.getType() == TransactionType.DEPOSIT) return;
        double toWithdraw = t.getMoney().getAmount().doubleValue();
        if (toWithdraw > maxAmount) throw new AmountNotAllowed("Amount too high, you cannot withdrawn it");
    }
}
