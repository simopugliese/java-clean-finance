package com.javawallet.domain.factory;

import com.javawallet.domain.model.Money;
import com.javawallet.domain.model.Wallet;
import com.javawallet.domain.model.WalletType;
import com.javawallet.domain.strategy.IRuleStrategy;
import com.javawallet.domain.strategy.NegativeBalanceNotAllowed;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class WalletFactory implements IWalletFactory {

    @Override
    public Wallet create(String name, WalletType type, Money initialBalance) {
        Collection<IRuleStrategy> rules = buildRulesFor(type);

        return new Wallet(
                name,
                type,
                initialBalance,
                rules,
                Collections.emptyList()
        );
    }

    private static Collection<IRuleStrategy> buildRulesFor(WalletType type) {
        return switch (type) {
            case DEBITCARD       -> List.of(new NegativeBalanceNotAllowed());
            case CHECKINGACCOUNT -> List.of(new NegativeBalanceNotAllowed());
            case CREDITCARD      -> List.of(new NegativeBalanceNotAllowed());
        };
    }
}
