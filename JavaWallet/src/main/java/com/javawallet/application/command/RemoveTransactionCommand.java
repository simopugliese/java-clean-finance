package com.javawallet.application.command;

import com.javawallet.application.ports.IWalletRepository;
import com.javawallet.domain.model.Transaction;
import com.javawallet.domain.model.Wallet;

import java.util.UUID;

public class RemoveTransactionCommand implements ICommand{
    private final UUID walletId;
    private final Transaction transaction;
    private final IWalletRepository walletRepository;

    public RemoveTransactionCommand(UUID walletId, Transaction transaction, IWalletRepository walletRepository) {
        this.walletId = walletId;
        this.transaction = transaction;
        this.walletRepository = walletRepository;
    }

    @Override
    public void execute() {
        Wallet w = walletRepository.getWalletByUUID(this.walletId).get();
        w.rollbackTransaction(transaction);
        walletRepository.upsertWallet(w);
    }

    @Override
    public void undo() {
        Wallet w = walletRepository.getWalletByUUID(this.walletId).get();
        w.addTransaction(transaction);
        walletRepository.upsertWallet(w);
    }
}
