package com.javawallet.infrastructure.persistence;

import com.javawallet.application.ports.IWalletRepository;
import com.javawallet.domain.model.Wallet;

import java.util.Optional;
import java.util.UUID;

public class MariaDBWalletPersistence implements IWalletRepository {
    @Override
    public void save(Wallet wallet) {

    }

    @Override
    public Optional<Wallet> findById(UUID id) {
        return Optional.empty();
    }

    @Override
    public boolean removeWallet(UUID id) {
        return false;
    }

    @Override
    public void update(Wallet wallet) {

    }
}