package com.javawallet.domain.factory;

import com.javawallet.domain.model.Money;
import com.javawallet.domain.model.Wallet;
import com.javawallet.domain.model.WalletType;
import com.javawallet.domain.strategy.IRuleStrategy;
import com.javawallet.domain.strategy.MaxWithdraw;
import com.javawallet.domain.strategy.NegativeBalanceNotAllowed;

import java.util.ArrayList;
import java.util.Collection;

public class WalletFactory {

    public static Wallet create(String name, WalletType type, Money initialBalance) {

        Collection<IRuleStrategy> rules = new ArrayList<>();

        switch (type) {
            case DEBITCARD:
                rules.add(new NegativeBalanceNotAllowed());
                rules.add(new MaxWithdraw(1000.00));
                break;

            case CHECKINGACCOUNT:
                rules.add(new NegativeBalanceNotAllowed());
                rules.add(new MaxWithdraw(5000.00));
                break;

            case CREDITCARD:
                rules.add(new MaxWithdraw(2000.00));
                break;

            default:
                throw new IllegalArgumentException("Unsupported wallet type: " + type);
        }

        return new Wallet(
                name,
                type,
                initialBalance,
                rules,
                new ArrayList<>()
        );
    }
}