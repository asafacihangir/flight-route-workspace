package org.phoenix.flightrouteapi.transportation.service.dtos;

import org.phoenix.flightrouteapi.location.service.dtos.LocationVM;
import org.phoenix.flightrouteapi.transportation.domain.TransportationType;

import java.util.Set;

public record TransportationVM(
        Long id,
        LocationVM origin,
        LocationVM destination,
        TransportationType type,
        Set<Integer> operatingDays
) {
}
