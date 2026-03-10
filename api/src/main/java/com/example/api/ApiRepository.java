package com.example.api;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ApiRepository {
    @PersistenceContext
    private EntityManager em;

    public <T> T save(T entity) {
        return em.merge(entity);
    }

    public <T> T findById(Class<T> clazz, Object id) {
        return em.find(clazz, id);
    }

    public <T> List<T> findAll(Class<T> clazz) {
        var query = em.createQuery("SELECT e FROM " + clazz.getSimpleName() + " e", clazz);
        return query.getResultList();
    }

    public <T> void delete(Class<T> clazz, Object id) {
        T entity = em.find(clazz, id);
        if (entity != null) {
            em.remove(entity);
        }
    }
}
