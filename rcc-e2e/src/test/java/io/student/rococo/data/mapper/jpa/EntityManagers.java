package io.student.rococo.data.mapper.jpa;

import io.student.rococo.config.Config;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import io.student.rococo.data.mapper.tpl.DataSources;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EntityManagers {

    private EntityManagers() {
    }

    private static final ConcurrentHashMap<String, EntityManagerFactory> emfs = new ConcurrentHashMap<>();

    public static EntityManager em(String jdbcUrl) {
        System.out.println(">>> JDBC URL = " + jdbcUrl);
        System.out.println(">>> PU = " + getPersistenceUnitName(jdbcUrl));
        System.out.println(">>> test.env = " + System.getProperty("test.env"));
        String persistenceUnitName = getPersistenceUnitName(jdbcUrl);

        EntityManagerFactory emf = emfs.computeIfAbsent(
                persistenceUnitName,
                key -> {
                    DataSources.dataSource(jdbcUrl);

                    Map<String, String> props = new HashMap<>();

                    String p6spyUrl = jdbcUrl.startsWith("jdbc:p6spy:")
                            ? jdbcUrl
                            : "jdbc:p6spy:" + jdbcUrl.substring("jdbc:".length());

                    props.put("jakarta.persistence.jdbc.url", p6spyUrl);
                    props.put("jakarta.persistence.jdbc.user", Config.getInstance().dbUserName());
                    props.put("jakarta.persistence.jdbc.password", Config.getInstance().dbPassword());
                    props.put("jakarta.persistence.jdbc.driver", "com.p6spy.engine.spy.P6SpyDriver");

                    return Persistence.createEntityManagerFactory(persistenceUnitName, props);
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

    public static @Nullable <T> T doInQuery(String jdbcUrl, QueryAction<T> action) {
        try (EntityManager em = em(jdbcUrl)) {
            return action.execute(em);
        }
    }

    @FunctionalInterface
    public interface TransactionAction<T> {
        @Nullable T execute(EntityManager em);
    }

    @FunctionalInterface
    public interface QueryAction<T> {
        @Nullable T execute(EntityManager em);
    }

    private static String getPersistenceUnitName(String jdbcUrl) {
        if (jdbcUrl.contains("rococo-auth")) {
            return "rococo-auth";
        } else if (jdbcUrl.contains("rcc_userdata")) {
            return "rcc-userdata";
        } else if (jdbcUrl.contains("rcc_artist")) {
            return "rcc-artist";
        } else if (jdbcUrl.contains("rcc_painting")) {
            return "rcc-painting";
        } else if (jdbcUrl.contains("rcc_museum")) {
            return "rcc-museum";
        } else if (jdbcUrl.contains("rcc_geo")) {
            return "rcc-geo";
        }
        throw new IllegalArgumentException("Unknown database URL: " + jdbcUrl);
    }
}