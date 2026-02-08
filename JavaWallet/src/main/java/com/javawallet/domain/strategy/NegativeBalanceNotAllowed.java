package com.javawallet.domain.strategy;

import com.javawallet.domain.exception.domain.InvalidAmountException;
import com.javawallet.domain.model.Transaction;
import com.javawallet.domain.model.Wallet;

public class NegativeBalanceNotAllowed implements  IRuleStrategy {
    @Override
    public void check(Wallet w, Transaction t) {
        double walletBalance = w.getBalance().getAmount().doubleValue();
        double transactionAmount = t.getMoney().getAmount().doubleValue();
        if (walletBalance - transactionAmount < 0){
            throw new InvalidAmountException("Insufficient funds in wallet: " + w.getId().toString());
        }
    }
}
