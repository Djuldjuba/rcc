package io.student.rococo.data.repository.impl.country;

import jakarta.persistence.NoResultException;
import io.student.rococo.config.Config;
import io.student.rococo.data.entity.CountryEntity;
import io.student.rococo.data.mapper.jpa.EntityManagers;
import io.student.rococo.data.repository.CountryRepository;
import org.jspecify.annotations.NonNull;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CountryRepositoryHibernate implements CountryRepository {

    private static final Config CFG = Config.getInstance();

    @Override
    public @NonNull Optional<CountryEntity> findById(@NonNull UUID id) {
        String jdbcUrl = CFG.rococoApiUrl();

        Optional<CountryEntity> result = EntityManagers.doInQuery(jdbcUrl, em ->
                Optional.ofNullable(em.find(CountryEntity.class, id))
        );

        return result != null ? result : Optional.empty();
    }

    @Override
    public @NonNull Optional<CountryEntity> findByName(@NonNull String name) {
        String jdbcUrl = CFG.rococoApiUrl();

        Optional<CountryEntity> result = EntityManagers.doInQuery(jdbcUrl, em -> {
            try {
                CountryEntity country = em.createQuery(
                                "SELECT c FROM CountryEntity c WHERE c.name = :name",
                                CountryEntity.class)
                        .setParameter("name", name)
                        .getSingleResult();
                return Optional.of(country);
            } catch (NoResultException e) {
                return Optional.empty();
            }
        });

        return result != null ? result : Optional.empty();
    }

    @Override
    public @NonNull List<CountryEntity> findAll() {
        String jdbcUrl = CFG.rococoApiUrl();

        List<CountryEntity> result = EntityManagers.doInQuery(jdbcUrl, em ->
                em.createQuery("SELECT c FROM CountryEntity c", CountryEntity.class)
                        .getResultList()
        );

        return result != null ? result : Collections.emptyList();
    }
}