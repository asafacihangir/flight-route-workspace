package org.phoenix.flightrouteapi.transportation.service;

import org.phoenix.flightrouteapi.BaseIT;
import org.phoenix.flightrouteapi.location.domain.LocationEntity;
import org.phoenix.flightrouteapi.location.repository.LocationRepository;
import org.phoenix.flightrouteapi.transportation.domain.TransportationEntity;
import org.phoenix.flightrouteapi.transportation.domain.TransportationType;
import org.phoenix.flightrouteapi.transportation.repository.TransportationRepository;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RouteServiceN1IT extends BaseIT {

    @Autowired
    private RouteService routeService;

    @Autowired
    private TransportationRepository transportationRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    private Long originId;
    private Long destinationId;

    @BeforeEach
    void seed() {
        transportationRepository.deleteAll();
        locationRepository.deleteAll();

        LocationEntity ist = locationRepository.save(new LocationEntity("Istanbul Airport", "TR", "Istanbul", "IST"));
        LocationEntity lhr = locationRepository.save(new LocationEntity("London Heathrow", "GB", "London", "LHR"));
        LocationEntity sub = locationRepository.save(new LocationEntity("Taksim", "TR", "Istanbul", "TAK"));
        LocationEntity wem = locationRepository.save(new LocationEntity("Wembley", "GB", "London", "WEM"));

        Set<Integer> everyDay = Set.of(1, 2, 3, 4, 5, 6, 7);
        transportationRepository.save(new TransportationEntity(sub, ist, TransportationType.SUBWAY, everyDay));
        transportationRepository.save(new TransportationEntity(ist, lhr, TransportationType.FLIGHT, everyDay));
        transportationRepository.save(new TransportationEntity(lhr, wem, TransportationType.UBER, everyDay));

        originId = sub.getId();
        destinationId = wem.getId();
    }

    @Test
    void routeListingExecutesSingleTransportationQuery() {
        Statistics stats = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        stats.setStatisticsEnabled(true);
        stats.clear();

        routeService.findRoutes(originId, destinationId, LocalDate.of(2025, 3, 12));

        assertThat(stats.getPrepareStatementCount())
                .as("Repository call must not trigger lazy fetch per row (N+1)")
                .isLessThanOrEqualTo(3L);
    }
}
