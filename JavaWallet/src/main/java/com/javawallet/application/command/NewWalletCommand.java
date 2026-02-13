package com.javawallet.application.command;

import com.javawallet.application.manager.FinanceManager;
import com.javawallet.domain.factory.WalletFactory;
import com.javawallet.domain.model.Money;
import com.javawallet.domain.model.Wallet;
import com.javawallet.domain.model.WalletType;

class NewWalletCommand implements ICommand {
    private final FinanceManager financeManager;
    private final Wallet wallet;

    NewWalletCommand(FinanceManager financeManager, String name, WalletType type, Money initialBalance) {
        this.financeManager = financeManager;
        this.wallet = WalletFactory.create(name, type, initialBalance);
    }

    @Override
    public void execute() {
        financeManager.addWallet(this.wallet);
    }

    @Override
    public void undo() {
        financeManager.removeWallet(this.wallet.getId());
    }
}