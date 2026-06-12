package rcc.repository;

import rcc.data.entity.CountryEntity;
import rcc.repository.impl.country.CountryRepositoryHibernate;
import rcc.repository.impl.country.CountryRepositoryJdbc;
import rcc.repository.impl.country.CountryRepositorySpringJdbc;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CountryRepository {

    static CountryRepository getInstance() {
        return switch (System.getProperty("repository.impl", "jpa")) {
            case "jdbc" -> new CountryRepositoryJdbc();
            case "spring-jdbc" -> new CountryRepositorySpringJdbc();
            default -> new CountryRepositoryHibernate();
        };
    }

    Optional<CountryEntity> findById(UUID id);

    Optional<CountryEntity> findByName(String name);

    List<CountryEntity> findAll();
}