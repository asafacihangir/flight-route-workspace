package org.phoenix.flightrouteapi.transportation.domain;

public enum TransportationType {
    FLIGHT,
    BUS,
    SUBWAY,
    UBER;

    public boolean isFlight() {
        return this == FLIGHT;
    }
}
