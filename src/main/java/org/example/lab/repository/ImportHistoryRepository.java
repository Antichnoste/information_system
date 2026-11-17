package org.example.lab.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.example.lab.model.ImportHistory;

@ApplicationScoped
public class ImportHistoryRepository {
    @PersistenceContext(unitName = "humanity")
    EntityManager em;

    public void save(ImportHistory history) {
        em.persist(history);
    }
}
