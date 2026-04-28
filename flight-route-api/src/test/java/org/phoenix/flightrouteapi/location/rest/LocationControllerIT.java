package org.phoenix.flightrouteapi.location.rest;

import org.phoenix.flightrouteapi.BaseIT;
import org.phoenix.flightrouteapi.location.service.LocationService;
import org.phoenix.flightrouteapi.location.service.dtos.LocationVM;
import org.phoenix.flightrouteapi.location.web.dtos.LocationRequest;
import org.phoenix.flightrouteapi.security.domain.Role;
import org.phoenix.flightrouteapi.transportation.service.TransportationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.ExchangeResult;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LocationControllerIT extends BaseIT {

    @Autowired
    private LocationService locationService;

    @Autowired
    private TransportationService transportationService;

    private String adminToken;
    private String agencyToken;

    @BeforeEach
    void setUp() {
        transportationService.findAll().forEach(vm -> transportationService.delete(vm.id()));
        locationService.findAll().forEach(vm -> locationService.delete(vm.id()));
        adminToken = seedUserAndLogin("admin-it", Role.ADMIN);
        agencyToken = seedUserAndLogin("agency-it", Role.AGENCY);
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    @Test
    void shouldCreateLocationWithIataCode() {
        LocationVM response = restTestClient
                .post()
                .uri("/api/locations")
                .header(HttpHeaders.AUTHORIZATION, bearer(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {
                          "name": "Sabiha Gokcen International Airport",
                          "country": "Turkiye",
                          "city": "Istanbul",
                          "code": "saw"
                        }
                        """)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().exists("Location")
                .returnResult(LocationVM.class)
                .getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.id()).isNotNull();
        assertThat(response.name()).isEqualTo("Sabiha Gokcen International Airport");
        assertThat(response.country()).isEqualTo("Turkiye");
        assertThat(response.city()).isEqualTo("Istanbul");
        assertThat(response.code()).isEqualTo("SAW");
    }

    @Test
    void shouldCreateLocationWithCustomCode() {
        LocationVM response = restTestClient
                .post()
                .uri("/api/locations")
                .header(HttpHeaders.AUTHORIZATION, bearer(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {
                          "name": "Istanbul City Center",
                          "country": "Turkiye",
                          "city": "Istanbul",
                          "code": "CCIST"
                        }
                        """)
                .exchange()
                .expectStatus().isCreated()
                .returnResult(LocationVM.class)
                .getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.code()).isEqualTo("CCIST");
    }

    @ParameterizedTest
    @CsvSource({
            ",Turkiye,Istanbul,SAW,Name",
            "Sabiha,,Istanbul,SAW,Country",
            "Sabiha,Turkiye,,SAW,City",
            "Sabiha,Turkiye,Istanbul,,Code"
    })
    void shouldRejectCreateWhenFieldsMissing(String name, String country, String city, String code, String errorField) {
        record ReqBody(String name, String country, String city, String code) {}

        ExchangeResult result = restTestClient
                .post()
                .uri("/api/locations")
                .header(HttpHeaders.AUTHORIZATION, bearer(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ReqBody(name, country, city, code))
                .exchange()
                .expectStatus().isBadRequest()
                .returnResult();

        String body = new String(result.getResponseBodyContent());
        assertThat(body).contains("%s is required".formatted(errorField));
    }

    @Test
    void shouldRejectDuplicateCodeCaseInsensitive() {
        locationService.create(new LocationRequest("Sabiha", "Turkiye", "Istanbul", "SAW"));

        restTestClient
                .post()
                .uri("/api/locations")
                .header(HttpHeaders.AUTHORIZATION, bearer(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {
                          "name": "Another Name",
                          "country": "Turkiye",
                          "city": "Istanbul",
                          "code": "saw"
                        }
                        """)
                .exchange()
                .expectStatus().isEqualTo(409);
    }

    @Test
    void shouldListLocations() {
        locationService.create(new LocationRequest("Sabiha", "Turkiye", "Istanbul", "SAW"));
        locationService.create(new LocationRequest("Istanbul Airport", "Turkiye", "Istanbul", "IST"));

        List<LocationVM> all = restTestClient
                .get()
                .uri("/api/locations")
                .header(HttpHeaders.AUTHORIZATION, bearer(adminToken))
                .exchange()
                .expectStatus().isOk()
                .returnResult(new ParameterizedTypeReference<List<LocationVM>>() {})
                .getResponseBody();

        assertThat(all).hasSize(2);
        assertThat(all).extracting(LocationVM::code).containsExactlyInAnyOrder("SAW", "IST");
    }

    @Test
    void shouldGetLocationById() {
        LocationVM created = locationService.create(new LocationRequest("Sabiha", "Turkiye", "Istanbul", "SAW"));

        LocationVM found = restTestClient
                .get()
                .uri("/api/locations/{id}", created.id())
                .header(HttpHeaders.AUTHORIZATION, bearer(adminToken))
                .exchange()
                .expectStatus().isOk()
                .returnResult(LocationVM.class)
                .getResponseBody();

        assertThat(found).isNotNull();
        assertThat(found.id()).isEqualTo(created.id());
        assertThat(found.code()).isEqualTo("SAW");
    }

    @Test
    void shouldReturn404WhenGetMissingId() {
        restTestClient
                .get()
                .uri("/api/locations/{id}", 999999)
                .header(HttpHeaders.AUTHORIZATION, bearer(adminToken))
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void shouldUpdateLocation() {
        LocationVM created = locationService.create(new LocationRequest("Sabiha", "Turkiye", "Istanbul", "SAW"));

        LocationVM updated = restTestClient
                .put()
                .uri("/api/locations/{id}", created.id())
                .header(HttpHeaders.AUTHORIZATION, bearer(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {
                          "name": "Sabiha Gokcen Intl",
                          "country": "Turkiye",
                          "city": "Istanbul",
                          "code": "SAW"
                        }
                        """)
                .exchange()
                .expectStatus().isOk()
                .returnResult(LocationVM.class)
                .getResponseBody();

        assertThat(updated).isNotNull();
        assertThat(updated.id()).isEqualTo(created.id());
        assertThat(updated.name()).isEqualTo("Sabiha Gokcen Intl");
    }

    @Test
    void shouldReturn409WhenUpdateToExistingCode() {
        LocationVM saw = locationService.create(new LocationRequest("Sabiha", "Turkiye", "Istanbul", "SAW"));
        locationService.create(new LocationRequest("Istanbul Airport", "Turkiye", "Istanbul", "IST"));

        restTestClient
                .put()
                .uri("/api/locations/{id}", saw.id())
                .header(HttpHeaders.AUTHORIZATION, bearer(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {
                          "name": "Sabiha",
                          "country": "Turkiye",
                          "city": "Istanbul",
                          "code": "IST"
                        }
                        """)
                .exchange()
                .expectStatus().isEqualTo(409);
    }

    @Test
    void shouldReturn404WhenUpdateMissingId() {
        restTestClient
                .put()
                .uri("/api/locations/{id}", 999999)
                .header(HttpHeaders.AUTHORIZATION, bearer(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {
                          "name": "X",
                          "country": "Y",
                          "city": "Z",
                          "code": "XYZ"
                        }
                        """)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void shouldDeleteLocation() {
        LocationVM created = locationService.create(new LocationRequest("Sabiha", "Turkiye", "Istanbul", "SAW"));

        restTestClient
                .delete()
                .uri("/api/locations/{id}", created.id())
                .header(HttpHeaders.AUTHORIZATION, bearer(adminToken))
                .exchange()
                .expectStatus().isNoContent();

        restTestClient
                .get()
                .uri("/api/locations/{id}", created.id())
                .header(HttpHeaders.AUTHORIZATION, bearer(adminToken))
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void shouldReturn404WhenDeleteMissingId() {
        restTestClient
                .delete()
                .uri("/api/locations/{id}", 999999)
                .header(HttpHeaders.AUTHORIZATION, bearer(adminToken))
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void shouldReturn401WhenNoToken() {
        restTestClient
                .get()
                .uri("/api/locations")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void shouldReturn403WhenAgencyListsLocations() {
        restTestClient
                .get()
                .uri("/api/locations")
                .header(HttpHeaders.AUTHORIZATION, bearer(agencyToken))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void shouldReturn403WhenAgencyCreatesLocation() {
        restTestClient
                .post()
                .uri("/api/locations")
                .header(HttpHeaders.AUTHORIZATION, bearer(agencyToken))
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {
                          "name": "Forbidden Airport",
                          "country": "Turkiye",
                          "city": "Istanbul",
                          "code": "FRB"
                        }
                        """)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void shouldReturn403WhenAgencyDeletesLocation() {
        LocationVM created = locationService.create(new LocationRequest("Sabiha", "Turkiye", "Istanbul", "SAW"));

        restTestClient
                .delete()
                .uri("/api/locations/{id}", created.id())
                .header(HttpHeaders.AUTHORIZATION, bearer(agencyToken))
                .exchange()
                .expectStatus().isForbidden();
    }
}
