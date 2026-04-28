package org.phoenix.flightrouteapi.location.service.dtos;

public record LocationVM(
        Long id,
        String name,
        String country,
        String city,
        String code
) {
}