package com.javawallet.application.command;

import com.javawallet.application.manager.FinanceManager;

public class NewWalletCommand implements ICommand{
    private final FinanceManager financeManager;

    public NewWalletCommand(FinanceManager financeManager) {
        this.financeManager = financeManager;
    }


    @Override
    public void exectute() {

    }

    @Override
    public void undo() {

    }
}
