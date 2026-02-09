package com.javawallet.application.service;

import com.javawallet.application.ports.ITransactionRepository;
import com.javawallet.application.ports.IWalletRepository;
import com.javawallet.domain.factory.WalletFactory;

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
}