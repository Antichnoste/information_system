package org.example.lab.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.List;
import org.example.lab.model.ImportHistory;

@ApplicationScoped
public class ImportHistoryRepository {
    @PersistenceContext(unitName = "humanity")
    EntityManager em;

    public void save(ImportHistory history) {
        em.persist(history);
    }

    public long count(Long userId) {
        String query = "SELECT COUNT(h) FROM ImportHistory h";
        if (userId != null) {
            query += " WHERE h.user.id = :userId";
        }

        TypedQuery<Long> result = em.createQuery(query, Long.class);
        if (userId != null) {
            result.setParameter("userId", userId);
        }
        return result.getSingleResult();
    }

    public List<ImportHistory> findPage(Long userId, int page, int size) {
        String query = "SELECT h FROM ImportHistory h JOIN FETCH h.user";
        if (userId != null) {
            query += " WHERE h.user.id = :userId";
        }
        query += " ORDER BY h.id DESC";
        TypedQuery<ImportHistory> result = em.createQuery(query, ImportHistory.class);
        if (userId != null) {
            result.setParameter("userId", userId);
        }
        return result.setFirstResult(page * size)
                    .setMaxResults(size)
                    .getResultList();
    }
}
