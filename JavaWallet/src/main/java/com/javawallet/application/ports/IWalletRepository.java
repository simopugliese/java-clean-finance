package com.javawallet.application.ports;

import com.javawallet.domain.model.Transaction;
import com.javawallet.domain.model.Wallet;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface IWalletRepository {
    boolean upsertWallet(Wallet w);
    Collection<Wallet> loadWallets();
    Optional<Wallet> getWalletByUUID(UUID id);
    boolean removeWallet(UUID id);
    Collection<Transaction> loadByWallet(UUID id);
    Collection<Transaction> loadByPeriod(LocalDateTime start, LocalDateTime end);
    Collection<Transaction> loadByWalletAndPeriod(UUID walletId, LocalDateTime start, LocalDateTime end);
    boolean removeTransaction(UUID walletId, UUID transactionID);
}