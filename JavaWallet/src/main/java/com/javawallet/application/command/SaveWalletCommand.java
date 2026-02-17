package com.javawallet.application.command;

import com.javawallet.application.ports.IWalletRepository;
import com.javawallet.domain.model.Wallet;

public class SaveWalletCommand implements ICommand{
    private final Wallet wallet;
    private final IWalletRepository walletRepository;

    public SaveWalletCommand(Wallet wallet,
                             IWalletRepository walletRepository) {
        this.wallet = wallet;
        this.walletRepository = walletRepository;
    }

    public Wallet getWallet() {
        return wallet;
    }

    @Override
    public void execute() {
        walletRepository.upsertWallet(wallet);
    }

    @Override
    public void undo() {
        walletRepository.removeWallet(wallet.getId());
    }
}
