package org.phoenix.flightrouteapi.transportation.repository;

import org.phoenix.flightrouteapi.transportation.domain.TransportationEntity;
import org.phoenix.flightrouteapi.transportation.domain.TransportationType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TransportationRepository extends JpaRepository<TransportationEntity, Long> {

    List<TransportationEntity> findByOriginId(Long originId);

    List<TransportationEntity> findByDestinationId(Long destinationId);

    List<TransportationEntity> findByType(TransportationType type);

    @EntityGraph(attributePaths = {"origin", "destination"})
    @Query("select t from TransportationEntity t")
    List<TransportationEntity> findAllWithLocations();
}
