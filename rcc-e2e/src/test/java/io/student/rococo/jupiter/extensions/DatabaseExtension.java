package io.student.rococo.jupiter.extensions;

import jakarta.persistence.EntityManager;
import io.student.rococo.data.mapper.tpl.Connections;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class DatabaseExtension implements SuiteExtension {

    private static final Logger log = Logger.getLogger(DatabaseExtension.class.getName());

    private static final Map<String, List<EntityManager>> entityManagersByDb = new ConcurrentHashMap<>();

    public static void registerEntityManager(EntityManager em, String jdbcUrl) {
        entityManagersByDb.computeIfAbsent(jdbcUrl, k -> new ArrayList<>()).add(em);
    }

    public static void unregisterEntityManager(EntityManager em, String jdbcUrl) {
        List<EntityManager> ems = entityManagersByDb.get(jdbcUrl);
        if (ems != null) {
            ems.remove(em);
        }
    }

    @Override
    public void afterSuite() {
        int closedCount = 0;
        for (Map.Entry<String, List<EntityManager>> entry : entityManagersByDb.entrySet()) {
            String dbUrl = entry.getKey();
            List<EntityManager> ems = entry.getValue();

            for (EntityManager em : ems) {
                if (em != null && em.isOpen()) {
                    try {
                        if (em.getTransaction().isActive()) {
                            em.getTransaction().rollback();
                        }
                        em.close();
                        closedCount++;
                    } catch (Exception e) {
                        //nope
                    }
                }
            }
        }
        entityManagersByDb.clear();

        Connections.closeAllConnections();
    }
}