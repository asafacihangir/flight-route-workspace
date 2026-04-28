package org.phoenix.flightrouteapi.transportation.service.dtos;

import java.util.List;

public record RouteVM(List<TransportationVM> legs) {
}
