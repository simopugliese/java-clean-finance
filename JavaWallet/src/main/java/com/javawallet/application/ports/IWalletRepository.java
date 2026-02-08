package com.javawallet.application.ports;

import com.javawallet.domain.model.Wallet;
import java.util.Optional;
import java.util.UUID;

public interface IWalletRepository {
    void save(Wallet wallet);
    Optional<Wallet> findById(UUID id);
    void update(Wallet wallet);
}