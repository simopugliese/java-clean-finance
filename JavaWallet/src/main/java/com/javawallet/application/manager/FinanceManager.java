package com.javawallet.application.manager;

import com.javawallet.application.ports.IWalletRepository;
import com.javawallet.domain.factory.WalletFactory;
import com.javawallet.domain.model.Wallet;

import java.util.Optional;
import java.util.UUID;

public class FinanceManager {

    private final IWalletRepository walletRepository;
    private final WalletFactory walletFactory;

    public FinanceManager(IWalletRepository wr, WalletFactory wf) {
        this.walletRepository = wr;
        this.walletFactory = wf;
    }

    public IWalletRepository getWalletRepository() { return walletRepository; }
    public WalletFactory getWalletFactory() { return walletFactory; }

    public void addWallet(Wallet w){
        walletRepository.save(w);
    }

    public boolean removeWallet(UUID id){
        return walletRepository.removeWallet(id);
    }

    public Optional<Wallet> getWallet(UUID id) {
        return walletRepository.findById(id);
    }
}