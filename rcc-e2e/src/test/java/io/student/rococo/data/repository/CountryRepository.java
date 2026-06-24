package io.student.rococo.data.repository;

import io.student.rococo.data.entity.CountryEntity;
import io.student.rococo.data.repository.impl.country.CountryRepositoryHibernate;
import io.student.rococo.data.repository.impl.country.CountryRepositoryJdbc;
import io.student.rococo.data.repository.impl.country.CountryRepositorySpringJdbc;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

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

    @NonNull Optional<CountryEntity> findById(@NonNull UUID id);

    @NonNull Optional<CountryEntity> findByName(@NonNull String name);

    @NonNull List<CountryEntity> findAll();
}