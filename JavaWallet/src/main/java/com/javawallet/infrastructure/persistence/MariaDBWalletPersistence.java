package com.javawallet.infrastructure.persistence;

import com.javawallet.application.ports.IWalletRepository;
import com.javawallet.domain.model.Wallet;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.Optional;
import java.util.UUID;

public class MariaDBWalletPersistence implements IWalletRepository {
    private final EntityManagerFactory emf;

    public MariaDBWalletPersistence() {
        this.emf = Persistence.createEntityManagerFactory("finance-unit");
    }

    @Override
    public void save(Wallet wallet) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(wallet);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public void update(Wallet wallet) {
        save(wallet);
    }

    @Override
    public Optional<Wallet> findById(UUID id) {
        try (EntityManager em = emf.createEntityManager()) {
            Wallet w = em.find(Wallet.class, id);
            return Optional.ofNullable(w);
        }
    }

    private void removeWallet(Wallet wallet) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            Wallet toDelete = em.contains(wallet) ? wallet : em.merge(wallet);

            em.remove(toDelete);

            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public boolean removeWallet(UUID id) {
        Optional<Wallet> wallet = findById(id);
        if (wallet.isPresent()) {removeWallet(wallet.get()); return true;}
        return false;
    }
}