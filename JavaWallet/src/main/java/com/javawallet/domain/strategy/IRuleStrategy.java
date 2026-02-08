package com.javawallet.domain.strategy;

import com.javawallet.domain.model.Transaction;
import com.javawallet.domain.model.Wallet;

public interface IRuleStrategy {
    void check(Wallet w, Transaction t);
}