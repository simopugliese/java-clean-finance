package com.javawallet.application.manager;

import com.javawallet.application.ports.ICategoryRepository;
import com.javawallet.application.ports.IWalletRepository;
import com.javawallet.domain.factory.WalletFactory;
import com.javawallet.domain.model.Category;
import com.javawallet.domain.model.Wallet;

import java.util.Optional;
import java.util.UUID;

public class FinanceManager {

    private final IWalletRepository walletRepository;
    private final ICategoryRepository categoryRepository;
    private final WalletFactory walletFactory;

    public FinanceManager(IWalletRepository wr,
                          ICategoryRepository cr,
                          WalletFactory wf) {
        this.walletRepository = wr;
        this.categoryRepository = cr;
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

    public void addCategory(Category c) { categoryRepository.save(c); }
    public void removeCategory(Category c) { categoryRepository.delete(c); }

    public Optional<Wallet> getWallet(UUID id) {
        return walletRepository.findById(id);
    }
}