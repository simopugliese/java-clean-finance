package com.javawallet.domain.strategy;

import com.javawallet.domain.exception.domain.InvalidAmountException;
import com.javawallet.domain.model.Transaction;
import com.javawallet.domain.model.TransactionType;
import com.javawallet.domain.model.Wallet;

import java.math.BigDecimal;

public class NegativeBalanceNotAllowed implements  IRuleStrategy {
    @Override
    public void check(Wallet w, Transaction t) {
        if (t.getType() == TransactionType.DEPOSIT) return;
        BigDecimal walletBalance = w.getBalance().getAmount();
        BigDecimal transactionAmount = t.getMoney().getAmount();
        if (walletBalance.compareTo(transactionAmount) < 0) {
            throw new InvalidAmountException("Insufficient funds in wallet: " + w.getId().toString());
        }
    }
}
