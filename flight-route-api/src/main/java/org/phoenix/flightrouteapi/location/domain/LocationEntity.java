package org.phoenix.flightrouteapi.location.domain;

import org.phoenix.flightrouteapi.shared.jpa.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "locations")
public class LocationEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "country", nullable = false, length = 100)
    private String country;

    @Column(name = "city", nullable = false, length = 100)
    private String city;

    @Column(name = "code", nullable = false, unique = true, length = 20)
    private String code;

    protected LocationEntity() {
    }

    public LocationEntity(String name, String country, String city, String code) {
        this.name = requireNonBlank(name, "name");
        this.country = requireNonBlank(country, "country");
        this.city = requireNonBlank(city, "city");
        this.code = normalizeCode(code);
    }

    public void update(String name, String country, String city, String code) {
        this.name = requireNonBlank(name, "name");
        this.country = requireNonBlank(country, "country");
        this.city = requireNonBlank(city, "city");
        this.code = normalizeCode(code);
    }

    private static String normalizeCode(String code) {
        return requireNonBlank(code, "code").trim().toUpperCase();
    }

    private static String requireNonBlank(String v, String field) {
        if (v == null || v.trim().isBlank()) {
            throw new IllegalArgumentException(field + " cannot be null or blank");
        }
        return v.trim();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCountry() {
        return country;
    }

    public String getCity() {
        return city;
    }

    public String getCode() {
        return code;
    }
}