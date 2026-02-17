package com.javawallet.application.command;

import com.javawallet.application.ports.IWalletRepository;
import com.javawallet.domain.exception.object.WalletNotFoundException;
import com.javawallet.domain.model.Wallet;

import java.util.UUID;

public class RemoveWalletCommand implements ICommand{
    private final Wallet wallet;
    private final UUID id;
    private final IWalletRepository walletRepository;

    public RemoveWalletCommand(UUID id, IWalletRepository walletRepository) {
        this.id = id;
        this.walletRepository = walletRepository;
        if (walletRepository.getWalletByUUID(id).isEmpty()) throw new WalletNotFoundException("Not found wallet with id" + id.toString());
        this.wallet = walletRepository.getWalletByUUID(id).get();
    }

    @Override
    public void execute() {
        walletRepository.removeWallet(this.id);
    }

    @Override
    public void undo() {
        walletRepository.upsertWallet(this.wallet);
    }
}
