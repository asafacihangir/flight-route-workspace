package org.phoenix.flightrouteapi.location.service.mapper;

import org.phoenix.flightrouteapi.location.domain.LocationEntity;
import org.phoenix.flightrouteapi.location.service.dtos.LocationVM;
import org.springframework.stereotype.Component;

@Component
public class LocationMapper {

    public LocationVM toVM(LocationEntity e) {
        return new LocationVM(e.getId(), e.getName(), e.getCountry(), e.getCity(), e.getCode());
    }
}
