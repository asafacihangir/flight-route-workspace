package org.phoenix.flightrouteapi.transportation.service;

import org.phoenix.flightrouteapi.config.CacheConfig;
import org.phoenix.flightrouteapi.location.repository.LocationRepository;
import org.phoenix.flightrouteapi.shared.exceptions.ResourceNotFoundException;
import org.phoenix.flightrouteapi.transportation.domain.TransportationType;
import org.phoenix.flightrouteapi.transportation.service.dtos.RouteVM;
import org.phoenix.flightrouteapi.transportation.domain.TransportationEntity;
import org.phoenix.flightrouteapi.transportation.repository.TransportationRepository;
import org.phoenix.flightrouteapi.transportation.service.dtos.TransportationVM;
import org.phoenix.flightrouteapi.transportation.service.mapper.TransportationMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RouteService {

    private final TransportationRepository transportationRepository;
    private final LocationRepository locationRepository;
    private final TransportationMapper mapper;
    private final RouteFinder finder;

    RouteService(TransportationRepository transportationRepository,
                 LocationRepository locationRepository,
                 TransportationMapper mapper,
                 RouteFinder finder) {
        this.transportationRepository = transportationRepository;
        this.locationRepository = locationRepository;
        this.mapper = mapper;
        this.finder = finder;
    }

    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.ROUTES_CACHE,
            key = "#originId + '_' + #destinationId + '_' + #date")
    public List<RouteVM> findRoutes(Long originId, Long destinationId, LocalDate date) {
        if (!locationRepository.existsById(originId)) {
            throw new ResourceNotFoundException("Location not found with id: " + originId);
        }
        if (!locationRepository.existsById(destinationId)) {
            throw new ResourceNotFoundException("Location not found with id: " + destinationId);
        }
        List<TransportationEntity> all = transportationRepository.findAllWithLocations();
        return finder.findRoutes(all, originId, destinationId, date).stream()
                .map(legs -> new RouteVM(legs.stream().map(mapper::toVM).toList()))
                .toList();
    }
}
