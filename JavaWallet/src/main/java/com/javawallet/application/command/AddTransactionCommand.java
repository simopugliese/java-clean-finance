package com.javawallet.application.command;

import com.javawallet.domain.exception.transactionType.InvalidTransactionType;
import com.javawallet.domain.model.*;

import java.time.LocalDateTime;

class AddTransactionCommand implements ICommand {
    private final Wallet wallet;
    private final Transaction transaction;

    AddTransactionCommand(Wallet wallet, Money amount, TransactionType type, Category category, LocalDateTime date, String note) {
        this.wallet = wallet;
        if (type == TransactionType.TRANSFER) throw new InvalidTransactionType("Transaction type cannot be TRANSFER");
        this.transaction = new TransactionBuilder(amount, type)
                .withCategory(category)
                .withDate(date)
                .withNote(note)
                .build();
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
