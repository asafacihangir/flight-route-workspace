package org.phoenix.flightrouteapi.transportation.web.dtos;

import org.phoenix.flightrouteapi.transportation.domain.TransportationType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record TransportationRequest(
        @NotNull(message = "originId is required")
        Long originId,

        @NotNull(message = "destinationId is required")
        Long destinationId,

        @NotNull(message = "type is required")
        TransportationType type,

        @NotEmpty(message = "operatingDays must not be empty")
        Set<@NotNull @Min(value = 1, message = "operatingDays values must be between 1 and 7")
            @Max(value = 7, message = "operatingDays values must be between 1 and 7") Integer> operatingDays
) {
}
