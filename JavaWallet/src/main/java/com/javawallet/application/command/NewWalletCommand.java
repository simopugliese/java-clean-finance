package com.javawallet.application.command;

import com.javawallet.application.manager.FinanceManager;
import com.javawallet.domain.model.Wallet;

class NewWalletCommand implements ICommand {
    private final FinanceManager financeManager;
    private final Wallet wallet;

    NewWalletCommand(FinanceManager financeManager, Wallet wallet) {
        this.financeManager = financeManager;
        this.wallet = wallet;
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