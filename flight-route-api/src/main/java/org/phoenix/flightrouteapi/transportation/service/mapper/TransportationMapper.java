package org.phoenix.flightrouteapi.transportation.service.mapper;

import org.phoenix.flightrouteapi.location.service.mapper.LocationMapper;
import org.phoenix.flightrouteapi.transportation.domain.TransportationEntity;
import org.phoenix.flightrouteapi.transportation.service.dtos.TransportationVM;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;

@Component
public class TransportationMapper {

    private final LocationMapper locationMapper;

    TransportationMapper(LocationMapper locationMapper) {
        this.locationMapper = locationMapper;
    }

    public TransportationVM toVM(TransportationEntity e) {
        return new TransportationVM(
                e.getId(),
                locationMapper.toVM(e.getOrigin()),
                locationMapper.toVM(e.getDestination()),
                e.getType(),
                new LinkedHashSet<>(e.getOperatingDays())
        );
    }
}
