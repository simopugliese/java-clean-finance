package com.javawallet.application.command;

import com.javawallet.domain.exception.transactionType.InvalidTransactionType;
import com.javawallet.domain.model.Transaction;
import com.javawallet.domain.model.TransactionType;
import com.javawallet.domain.model.Wallet;

public class MakeTransferCommand implements ICommand {
    private final Wallet walletForWithdraw;
    private final Wallet walletForDeposit;
    private final Transaction transaction;

    public MakeTransferCommand(Wallet walletForWithdraw, Wallet walletForDeposit, Transaction transaction) {
        this.walletForWithdraw = walletForWithdraw;
        this.walletForDeposit = walletForDeposit;
        if (transaction.getType() != TransactionType.TRANSFER) throw  new InvalidTransactionType("Transaction type must be TRANSFER");
        this.transaction = transaction;
    }

    @Override
    public void execute() {
        walletForWithdraw.transferWithdraw(transaction);
        walletForDeposit.transferDeposit(transaction);
    }

    @Override
    public void undo() {
        walletForWithdraw.transferDeposit(transaction);
        walletForDeposit.transferWithdraw(transaction);
    }
}
