package com.javawallet.domain.strategy;

import com.javawallet.domain.model.Money;

public interface IRuleStrategy {
    boolean canWithdraw(Money amount);
}