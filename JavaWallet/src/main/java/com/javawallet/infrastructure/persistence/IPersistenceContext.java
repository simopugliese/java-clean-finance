package com.javawallet.infrastructure.persistence;

import com.javawallet.application.ports.ICategoryRepository;
import com.javawallet.application.ports.IWalletRepository;

public class IPersistenceContext {
    private final IWalletRepository walletRepository;
    private final ICategoryRepository categoryRepository;

    public IPersistenceContext(IWalletRepository walletRepository, ICategoryRepository categoryRepository) {
        this.walletRepository = walletRepository;
        this.categoryRepository = categoryRepository;
    }

    public IWalletRepository getWalletRepository() {
        return walletRepository;
    }

    public ICategoryRepository getCategoryRepository() {
        return categoryRepository;
    }
}
