package com.javawallet.application.command;

import com.javawallet.domain.exception.transactionType.InvalidTransactionType;
import com.javawallet.domain.model.Transaction;
import com.javawallet.domain.model.TransactionType;
import com.javawallet.domain.model.Wallet;

class AddTransactionCommand implements ICommand {
    private final Wallet wallet;
    private final Transaction transaction;

    AddTransactionCommand(Wallet wallet, Transaction transaction) {
        this.wallet = wallet;
        if (transaction.getType() == TransactionType.TRANSFER) throw new InvalidTransactionType("Transaction type cannot be TRANSFER");
        this.transaction = transaction;
    }

    @Override
    public void execute() {
        wallet.addTransaction(transaction);
    }

    @Override
    public void undo() {
        switch (transaction.getType()){
            case DEPOSIT -> wallet.rollbackDeposit(transaction);
            case WITHDRAW ->  wallet.rollbackWithdraw(transaction);
            case TRANSFER -> throw new InvalidTransactionType("Transaction type cannot be TRANSFER");
        }
    }
}
