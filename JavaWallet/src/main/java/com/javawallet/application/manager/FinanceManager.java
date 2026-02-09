package com.javawallet.application.manager;

import com.javawallet.application.ports.ITransactionRepository;
import com.javawallet.application.ports.IWalletRepository;
import com.javawallet.domain.factory.WalletFactory;
import com.javawallet.domain.model.Wallet;

import java.util.Optional;
import java.util.UUID;

public class FinanceManager {

    private final IWalletRepository walletRepository;
    private final ITransactionRepository transactionRepository;
    private final WalletFactory walletFactory;

    public FinanceManager(IWalletRepository wr, ITransactionRepository tr, WalletFactory wf) {
        this.walletRepository = wr;
        this.transactionRepository = tr;
        this.walletFactory = wf;
    }

    public IWalletRepository getWalletRepository() { return walletRepository; }
    public ITransactionRepository getTransactionRepository() { return transactionRepository; }
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