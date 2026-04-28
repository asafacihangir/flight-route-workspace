package org.phoenix.flightrouteapi.location.repository;

import org.phoenix.flightrouteapi.location.domain.LocationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LocationRepository extends JpaRepository<LocationEntity, Long> {

    Optional<LocationEntity> findByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCase(String code);
}