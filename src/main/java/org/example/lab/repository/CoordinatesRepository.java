package org.example.lab.repository;
import org.example.lab.exception.GlobalExceptionHandler;
import jakarta.ws.rs.WebApplicationException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import org.example.lab.model.Coordinates;

@ApplicationScoped
public class CoordinatesRepository {
    @PersistenceContext(unitName = "humanity")
    EntityManager em;

    public List<Coordinates> findAll() {
        return em.createQuery("SELECT e FROM Coordinates e ORDER BY e.id", Coordinates.class)
                .getResultList();
    }

    public Coordinates findById(long id) {
        Coordinates coordinates = em.find(Coordinates.class, id);
        if (coordinates == null) {
            throw new WebApplicationException(GlobalExceptionHandler.response(404, "Координаты не найдены"));
        }
        return coordinates;
    }

    public Coordinates findByValues(Long x, Float y) {
        List<Coordinates> found = em.createQuery("SELECT c FROM Coordinates c WHERE c.x = :x AND c.y = :y ORDER BY c.id", Coordinates.class)
                .setParameter("x", x)
                .setParameter("y", y)
                .setMaxResults(1)
                .getResultList();
        return found.isEmpty() ? null : found.get(0);
    }

    public void useSerializableIsolation() {
        // вызываю до первого SELECT или INSERT в транзакции. меняем уровень изоляции
        em.createNativeQuery("SET TRANSACTION ISOLATION LEVEL SERIALIZABLE").executeUpdate();
    }

    public void save(Coordinates coordinates) {
        em.persist(coordinates);
    }

    public void flush() {
        em.flush();
    }

    public void delete(Coordinates coordinates) {
        em.remove(coordinates);
    }
}
