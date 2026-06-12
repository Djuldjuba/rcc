package rcc.repository.impl.country;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import rcc.config.Config;
import rcc.data.entity.CountryEntity;
import rcc.data.entity.jpa.EntityManagers;
import rcc.repository.CountryRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CountryRepositoryHibernate implements CountryRepository {

    private static final Config CFG = Config.getInstance();
    private final EntityManager entityManager = EntityManagers.em(CFG.rococoApiUrl());

    @Override
    public Optional<CountryEntity> findById(UUID id) {
        return Optional.ofNullable(entityManager.find(CountryEntity.class, id));
    }

    @Override
    public Optional<CountryEntity> findByName(String name) {
        try {
            CountryEntity country = entityManager.createQuery(
                            "SELECT c FROM CountryEntity c WHERE c.name = :name",
                            CountryEntity.class)
                    .setParameter("name", name)
                    .getSingleResult();
            return Optional.of(country);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<CountryEntity> findAll() {
        return entityManager.createQuery("SELECT c FROM CountryEntity c", CountryEntity.class)
                .getResultList();
    }
}