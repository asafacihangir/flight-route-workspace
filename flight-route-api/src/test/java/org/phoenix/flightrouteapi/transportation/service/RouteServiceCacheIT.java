package org.phoenix.flightrouteapi.transportation.service;

import org.phoenix.flightrouteapi.BaseIT;
import org.phoenix.flightrouteapi.config.CacheConfig;
import org.phoenix.flightrouteapi.location.domain.LocationEntity;
import org.phoenix.flightrouteapi.location.repository.LocationRepository;
import org.phoenix.flightrouteapi.location.service.LocationService;
import org.phoenix.flightrouteapi.location.web.dtos.LocationRequest;
import org.phoenix.flightrouteapi.transportation.domain.TransportationEntity;
import org.phoenix.flightrouteapi.transportation.domain.TransportationType;
import org.phoenix.flightrouteapi.transportation.repository.TransportationRepository;
import org.phoenix.flightrouteapi.transportation.service.dtos.RouteVM;
import org.phoenix.flightrouteapi.transportation.service.dtos.TransportationCmd;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RouteServiceCacheIT extends BaseIT {

    private static final LocalDate WED = LocalDate.of(2025, 3, 12);

    @Autowired
    private RouteService routeService;
    @Autowired
    private TransportationService transportationService;
    @Autowired
    private LocationService locationService;
    @Autowired
    private TransportationRepository transportationRepository;
    @Autowired
    private LocationRepository locationRepository;
    @Autowired
    private CacheManager cacheManager;

    private Long originId;
    private Long destinationId;
    private Long transportationId;

    @BeforeEach
    void seed() {
        cacheManager.getCache(CacheConfig.ROUTES_CACHE).clear();
        transportationRepository.deleteAll();
        locationRepository.deleteAll();

        LocationEntity ist = locationRepository.save(new LocationEntity("Istanbul Airport", "TR", "Istanbul", "IST"));
        LocationEntity lhr = locationRepository.save(new LocationEntity("London Heathrow", "GB", "London", "LHR"));

        TransportationEntity flight = transportationRepository.save(
                new TransportationEntity(ist, lhr, TransportationType.FLIGHT, Set.of(1, 2, 3, 4, 5, 6, 7)));

        originId = ist.getId();
        destinationId = lhr.getId();
        transportationId = flight.getId();
    }

    @Test
    void secondCallIsServedFromCache() {
        List<RouteVM> first = routeService.findRoutes(originId, destinationId, WED);
        Object cached = cacheManager.getCache(CacheConfig.ROUTES_CACHE)
                .get(originId + "_" + destinationId + "_" + WED).get();

        assertThat(first).isNotEmpty();
        assertThat(cached).isEqualTo(first);
    }

    @Test
    void transportationUpdateEvictsCache() {
        routeService.findRoutes(originId, destinationId, WED);
        assertThat(cacheManager.getCache(CacheConfig.ROUTES_CACHE)
                .get(originId + "_" + destinationId + "_" + WED)).isNotNull();

        transportationService.update(transportationId,
                new TransportationCmd(originId, destinationId, TransportationType.FLIGHT, Set.of(1, 3, 5)));

        assertThat(cacheManager.getCache(CacheConfig.ROUTES_CACHE)
                .get(originId + "_" + destinationId + "_" + WED)).isNull();
    }

    @Test
    void locationUpdateEvictsCache() {
        routeService.findRoutes(originId, destinationId, WED);
        assertThat(cacheManager.getCache(CacheConfig.ROUTES_CACHE)
                .get(originId + "_" + destinationId + "_" + WED)).isNotNull();

        locationService.update(originId,
                new LocationRequest("Istanbul Renamed", "TR", "Istanbul", "IST"));

        assertThat(cacheManager.getCache(CacheConfig.ROUTES_CACHE)
                .get(originId + "_" + destinationId + "_" + WED)).isNull();
    }
}
