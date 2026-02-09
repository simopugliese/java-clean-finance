package com.javawallet.infrastructure.persistence;

import com.javawallet.application.ports.ICategoryRepository;
import com.javawallet.domain.exception.linking.LinkedToOtherObjectException;
import com.javawallet.domain.model.Category;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.TypedQuery;

import java.util.Optional;
import java.util.UUID;

public class MariaDBCategoryPersistence implements ICategoryRepository {
    private final EntityManagerFactory emf;

    public MariaDBCategoryPersistence() {
        this.emf = Persistence.createEntityManagerFactory("finance-unit");
    }

    @Override
    public void save(Category category) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(category);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public void delete(Category category) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            Category toRemove = em.contains(category) ? category : em.merge(category);
            em.remove(toRemove);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw new LinkedToOtherObjectException("Cannot delete category used by transactions");
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<Category> findById(UUID id) {
        try (EntityManager em = emf.createEntityManager()) {
            return Optional.ofNullable(em.find(Category.class, id));
        }
    }

    @Override
    public Optional<Category> findByName(String name) {
        try (EntityManager em = emf.createEntityManager()) {
            TypedQuery<Category> q = em.createQuery("SELECT c FROM Category c WHERE c.name = :name", Category.class);
            q.setParameter("name", name);
            return q.getResultStream().findFirst();
        }
    }
}