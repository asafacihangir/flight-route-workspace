package org.phoenix.flightrouteapi.transportation.service.dtos;

import org.phoenix.flightrouteapi.transportation.domain.TransportationType;

import java.util.Set;

public record TransportationCmd(
        Long originId,
        Long destinationId,
        TransportationType type,
        Set<Integer> operatingDays
) {
}
