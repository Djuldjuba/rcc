package rcc.data.entity.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import rcc.repository.tpl.DataSources;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EntityManagers {

    private EntityManagers() {
    }

    private static final ConcurrentHashMap<String, EntityManagerFactory> emfs = new ConcurrentHashMap<>();

    public static EntityManager em(String jdbcUrl) {
        String persistenceUnitName = getPersistenceUnitName(jdbcUrl);

        EntityManagerFactory emf = emfs.computeIfAbsent(
                persistenceUnitName,
                key -> {
                    DataSources.dataSource(jdbcUrl);
                    return Persistence.createEntityManagerFactory(persistenceUnitName);
                }
        );

        return new ThreadSafeEntityManager(emf.createEntityManager(), jdbcUrl);
    }

    public static <T> T doInTransaction(String jdbcUrl, TransactionAction<T> action) {
        try (EntityManager em = em(jdbcUrl)) {
            var tx = em.getTransaction();
            try {
                tx.begin();
                T result = action.execute(em);
                tx.commit();
                return result;
            } catch (Exception e) {
                if (tx.isActive()) {
                    tx.rollback();
                }
                throw new RuntimeException(e);
            }
        }
    }

    public static <T> T doInQuery(String jdbcUrl, QueryAction<T> action) {
        try (EntityManager em = em(jdbcUrl)) {
            return action.execute(em);
        }
    }

    @FunctionalInterface
    public interface TransactionAction<T> {
        T execute(EntityManager em);
    }

    @FunctionalInterface
    public interface QueryAction<T> {
        T execute(EntityManager em);
    }

    private static String getPersistenceUnitName(String jdbcUrl) {
        if (jdbcUrl.contains("rococo-auth")) {
            return "rococo-auth";
        } else if (jdbcUrl.contains("rococo-api")) {
            return "rococo-api";
        }
        throw new IllegalArgumentException("Unknown database URL: " + jdbcUrl);
    }
}