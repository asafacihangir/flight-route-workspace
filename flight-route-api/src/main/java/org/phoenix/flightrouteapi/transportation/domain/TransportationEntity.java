package org.phoenix.flightrouteapi.transportation.domain;

import org.phoenix.flightrouteapi.location.domain.LocationEntity;
import org.phoenix.flightrouteapi.shared.jpa.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "transportations")
public class TransportationEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "origin_id", nullable = false)
    private LocationEntity origin;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "destination_id", nullable = false)
    private LocationEntity destination;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private TransportationType type;

    @Convert(converter = OperatingDaysConverter.class)
    @Column(name = "operating_days", nullable = false, length = 20)
    private Set<Integer> operatingDays = new LinkedHashSet<>();

    protected TransportationEntity() {
    }

    public TransportationEntity(LocationEntity origin, LocationEntity destination, TransportationType type,
                         Set<Integer> operatingDays) {
        this.origin = requireNonNull(origin, "origin");
        this.destination = requireNonNull(destination, "destination");
        this.type = requireNonNull(type, "type");
        this.operatingDays = normalizeDays(operatingDays);
        ensureDistinctEndpoints(this.origin, this.destination);
    }

    public void update(LocationEntity origin, LocationEntity destination, TransportationType type,
                Set<Integer> operatingDays) {
        this.origin = requireNonNull(origin, "origin");
        this.destination = requireNonNull(destination, "destination");
        this.type = requireNonNull(type, "type");
        this.operatingDays = normalizeDays(operatingDays);
        ensureDistinctEndpoints(this.origin, this.destination);
    }

    private static <T> T requireNonNull(T v, String field) {
        if (v == null) {
            throw new IllegalArgumentException(field + " cannot be null");
        }
        return v;
    }

    private static Set<Integer> normalizeDays(Set<Integer> days) {
        if (days == null || days.isEmpty()) {
            throw new IllegalArgumentException("operatingDays cannot be null or empty");
        }
        Set<Integer> copy = new LinkedHashSet<>();
        for (Integer d : days) {
            if (d == null || d < 1 || d > 7) {
                throw new IllegalArgumentException("operatingDays must contain values between 1 and 7");
            }
            copy.add(d);
        }
        return copy;
    }

    private static void ensureDistinctEndpoints(LocationEntity origin, LocationEntity destination) {
        if (origin.getId() != null && origin.getId().equals(destination.getId())) {
            throw new IllegalArgumentException("origin and destination must be different");
        }
    }

    public Long getId() {
        return id;
    }

    public LocationEntity getOrigin() {
        return origin;
    }

    public LocationEntity getDestination() {
        return destination;
    }

    public TransportationType getType() {
        return type;
    }

    public Set<Integer> getOperatingDays() {
        return operatingDays;
    }

    public boolean startsAt(Long locationId) {
        return origin.getId().equals(locationId);
    }

    public boolean endsAt(Long locationId) {
        return destination.getId().equals(locationId);
    }
}
