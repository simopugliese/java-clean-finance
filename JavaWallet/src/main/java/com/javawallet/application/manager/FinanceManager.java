package com.javawallet.application.manager;

import com.javawallet.application.ports.ITransactionRepository;
import com.javawallet.application.ports.IWalletRepository;
import com.javawallet.domain.exception.object.WalletNullException;
import com.javawallet.domain.factory.WalletFactory;
import com.javawallet.domain.model.Wallet;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public class FinanceManager {

    private final IWalletRepository walletRepository;
    private final ITransactionRepository transactionRepository;
    private final WalletFactory walletFactory;

    private Collection<Wallet> wallets;

    public FinanceManager(IWalletRepository wr, ITransactionRepository tr, WalletFactory wf) {
        this.walletRepository = wr;
        this.transactionRepository = tr;
        this.walletFactory = wf;
    }

    public IWalletRepository getWalletRepository() { return walletRepository; }
    public ITransactionRepository getTransactionRepository() { return transactionRepository; }
    public WalletFactory getWalletFactory() { return walletFactory; }

    public void addWallet(Wallet w){
        checkWalletNull();
        wallets.add(w);
    }

    public void removeWallet(Wallet w){
        checkWalletNull();
        wallets.remove(w);
    }

    public Optional<Wallet> getWallet(UUID id) {
        checkWalletNull();
        return wallets.stream().filter(w -> w.getId().equals(id)).findAny();
    }

    public Optional<Wallet> getWallet(String name){
        checkWalletNull();
        return wallets.stream().filter(w -> w.getName().equals(name)).findAny();
    }

    private void checkWalletNull(){
        if (wallets == null) throw new WalletNullException("wallets is null");
    }
}