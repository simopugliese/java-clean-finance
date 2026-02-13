package com.javawallet.application.command;

import com.javawallet.domain.model.*;

import java.time.LocalDateTime;

class MakeTransferCommand implements ICommand {
    private final Wallet walletForWithdraw;
    private final Wallet walletForDeposit;
    private final Transaction transactionWithdraw;
    private final Transaction transactionDeposit;

    MakeTransferCommand(Wallet walletForWithdraw, Wallet walletForDeposit, Money amount, Category category, LocalDateTime date, String note) {
        this.walletForWithdraw = walletForWithdraw;
        this.walletForDeposit = walletForDeposit;

        this.transactionWithdraw = new TransactionBuilder(amount, TransactionType.TRANSFER)
                .withDate(date)
                .withCategory(category)
                .withNote(note)
                .build();
        this.transactionDeposit = new TransactionBuilder(amount, TransactionType.TRANSFER)
                .withDate(date)
                .withCategory(category)
                .withNote(note)
                .build();
    }

    @Override
    public void execute() {
        walletForWithdraw.transferWithdraw(transactionWithdraw);
        try {
            walletForDeposit.transferDeposit(transactionDeposit);
        } catch (Exception e) {
            walletForWithdraw.rollbackTransferWithdraw(transactionWithdraw);
            throw new RuntimeException("Transfer failed: deposit error. Original withdraw rolled back.", e);
        }
    }

    @Override
    public void undo() {
        walletForWithdraw.rollbackTransferWithdraw(transactionWithdraw);
        walletForDeposit.rollbackTransferDeposit(transactionDeposit);
    }
}
