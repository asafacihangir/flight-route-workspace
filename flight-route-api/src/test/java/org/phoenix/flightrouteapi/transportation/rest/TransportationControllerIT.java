package org.phoenix.flightrouteapi.transportation.rest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.phoenix.flightrouteapi.BaseIT;
import org.phoenix.flightrouteapi.location.service.LocationService;
import org.phoenix.flightrouteapi.location.service.dtos.LocationVM;
import org.phoenix.flightrouteapi.location.web.dtos.LocationRequest;
import org.phoenix.flightrouteapi.security.domain.Role;
import org.phoenix.flightrouteapi.transportation.domain.TransportationType;
import org.phoenix.flightrouteapi.transportation.service.TransportationService;
import org.phoenix.flightrouteapi.transportation.service.dtos.TransportationCmd;
import org.phoenix.flightrouteapi.transportation.service.dtos.TransportationVM;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.ExchangeResult;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class TransportationControllerIT extends BaseIT {

    @Autowired
    private LocationService locationService;

    @Autowired
    private TransportationService transportationService;

    private String adminToken;
    private String agencyToken;

    private Long sawId;
    private Long istId;

    @BeforeEach
    void setUp() {
        transportationService.findAll().forEach(vm -> transportationService.delete(vm.id()));
        locationService.findAll().forEach(vm -> locationService.delete(vm.id()));

        adminToken = seedUserAndLogin("admin-it", Role.ADMIN);
        agencyToken = seedUserAndLogin("agency-it", Role.AGENCY);

        LocationVM saw = locationService.create(new LocationRequest("Sabiha", "Turkiye", "Istanbul", "SAW"));
        LocationVM ist = locationService.create(new LocationRequest("Istanbul Airport", "Turkiye", "Istanbul", "IST"));
        sawId = saw.id();
        istId = ist.id();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private String createBody(Long originId, Long destinationId, String type, String operatingDaysJson) {
        return """
                {
                  "originId": %s,
                  "destinationId": %s,
                  "type": %s,
                  "operatingDays": %s
                }
                """.formatted(
                originId,
                destinationId,
                type == null ? "null" : "\"" + type + "\"",
                operatingDaysJson);
    }

    @Test
    void shouldCreateTransportation() {
        TransportationVM response = restTestClient
                .post()
                .uri("/api/transportations")
                .header(HttpHeaders.AUTHORIZATION, bearer(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .body(createBody(sawId, istId, "FLIGHT", "[1,2,3]"))
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().exists("Location")
                .returnResult(TransportationVM.class)
                .getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.id()).isNotNull();
        assertThat(response.origin().id()).isEqualTo(sawId);
        assertThat(response.destination().id()).isEqualTo(istId);
        assertThat(response.type()).isEqualTo(TransportationType.FLIGHT);
        assertThat(response.operatingDays()).containsExactlyInAnyOrder(1, 2, 3);
    }

    @Test
    void shouldRejectCreateWhenOriginAndDestinationSame() {
        restTestClient
                .post()
                .uri("/api/transportations")
                .header(HttpHeaders.AUTHORIZATION, bearer(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .body(createBody(sawId, sawId, "FLIGHT", "[1,2]"))
                .exchange()
                .expectStatus().isEqualTo(409);
    }

    @Test
    void shouldRejectCreateWhenOperatingDaysEmpty() {
        ExchangeResult result = restTestClient
                .post()
                .uri("/api/transportations")
                .header(HttpHeaders.AUTHORIZATION, bearer(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .body(createBody(sawId, istId, "FLIGHT", "[]"))
                .exchange()
                .expectStatus().isBadRequest()
                .returnResult();

        String body = new String(result.getResponseBodyContent());
        assertThat(body).contains("operatingDays");
    }

    @Test
    void shouldRejectCreateWhenTypeMissing() {
        ExchangeResult result = restTestClient
                .post()
                .uri("/api/transportations")
                .header(HttpHeaders.AUTHORIZATION, bearer(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .body(createBody(sawId, istId, null, "[1]"))
                .exchange()
                .expectStatus().isBadRequest()
                .returnResult();

        String body = new String(result.getResponseBodyContent());
        assertThat(body).contains("type");
    }

    @Test
    void shouldRejectCreateWhenOriginIdMissing() {
        restTestClient
                .post()
                .uri("/api/transportations")
                .header(HttpHeaders.AUTHORIZATION, bearer(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {
                          "destinationId": %s,
                          "type": "FLIGHT",
                          "operatingDays": [1,2]
                        }
                        """.formatted(istId))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void shouldReturn404WhenOriginLocationMissing() {
        restTestClient
                .post()
                .uri("/api/transportations")
                .header(HttpHeaders.AUTHORIZATION, bearer(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .body(createBody(999999L, istId, "FLIGHT", "[1]"))
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void shouldListTransportations() {
        transportationService.create(new TransportationCmd(sawId, istId, TransportationType.FLIGHT, Set.of(1, 2)));
        transportationService.create(new TransportationCmd(istId, sawId, TransportationType.BUS, Set.of(3)));

        List<TransportationVM> all = restTestClient
                .get()
                .uri("/api/transportations")
                .header(HttpHeaders.AUTHORIZATION, bearer(adminToken))
                .exchange()
                .expectStatus().isOk()
                .returnResult(new ParameterizedTypeReference<List<TransportationVM>>() {})
                .getResponseBody();

        assertThat(all).hasSize(2);
        assertThat(all).extracting(TransportationVM::type)
                .containsExactlyInAnyOrder(TransportationType.FLIGHT, TransportationType.BUS);
    }

    @Test
    void shouldGetTransportationById() {
        TransportationVM created = transportationService.create(
                new TransportationCmd(sawId, istId, TransportationType.FLIGHT, Set.of(1, 2)));

        TransportationVM found = restTestClient
                .get()
                .uri("/api/transportations/{id}", created.id())
                .header(HttpHeaders.AUTHORIZATION, bearer(adminToken))
                .exchange()
                .expectStatus().isOk()
                .returnResult(TransportationVM.class)
                .getResponseBody();

        assertThat(found).isNotNull();
        assertThat(found.id()).isEqualTo(created.id());
        assertThat(found.type()).isEqualTo(TransportationType.FLIGHT);
    }

    @Test
    void shouldReturn404WhenGetMissingId() {
        restTestClient
                .get()
                .uri("/api/transportations/{id}", 999999)
                .header(HttpHeaders.AUTHORIZATION, bearer(adminToken))
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void shouldUpdateTransportation() {
        TransportationVM created = transportationService.create(
                new TransportationCmd(sawId, istId, TransportationType.FLIGHT, Set.of(1, 2)));

        TransportationVM updated = restTestClient
                .put()
                .uri("/api/transportations/{id}", created.id())
                .header(HttpHeaders.AUTHORIZATION, bearer(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .body(createBody(sawId, istId, "BUS", "[3,4,5]"))
                .exchange()
                .expectStatus().isOk()
                .returnResult(TransportationVM.class)
                .getResponseBody();

        assertThat(updated).isNotNull();
        assertThat(updated.id()).isEqualTo(created.id());
        assertThat(updated.type()).isEqualTo(TransportationType.BUS);
        assertThat(updated.operatingDays()).containsExactlyInAnyOrder(3, 4, 5);
    }

    @Test
    void shouldReturn404WhenUpdateMissingId() {
        restTestClient
                .put()
                .uri("/api/transportations/{id}", 999999)
                .header(HttpHeaders.AUTHORIZATION, bearer(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .body(createBody(sawId, istId, "FLIGHT", "[1]"))
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void shouldDeleteTransportation() {
        TransportationVM created = transportationService.create(
                new TransportationCmd(sawId, istId, TransportationType.FLIGHT, Set.of(1)));

        restTestClient
                .delete()
                .uri("/api/transportations/{id}", created.id())
                .header(HttpHeaders.AUTHORIZATION, bearer(adminToken))
                .exchange()
                .expectStatus().isNoContent();

        restTestClient
                .get()
                .uri("/api/transportations/{id}", created.id())
                .header(HttpHeaders.AUTHORIZATION, bearer(adminToken))
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void shouldReturn404WhenDeleteMissingId() {
        restTestClient
                .delete()
                .uri("/api/transportations/{id}", 999999)
                .header(HttpHeaders.AUTHORIZATION, bearer(adminToken))
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void shouldReturn401WhenNoToken() {
        restTestClient
                .get()
                .uri("/api/transportations")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void shouldReturn403WhenAgencyListsTransportations() {
        restTestClient
                .get()
                .uri("/api/transportations")
                .header(HttpHeaders.AUTHORIZATION, bearer(agencyToken))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void shouldReturn403WhenAgencyCreatesTransportation() {
        restTestClient
                .post()
                .uri("/api/transportations")
                .header(HttpHeaders.AUTHORIZATION, bearer(agencyToken))
                .contentType(MediaType.APPLICATION_JSON)
                .body(createBody(sawId, istId, "FLIGHT", "[1]"))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void shouldReturn403WhenAgencyDeletesTransportation() {
        TransportationVM created = transportationService.create(
                new TransportationCmd(sawId, istId, TransportationType.FLIGHT, Set.of(1)));

        restTestClient
                .delete()
                .uri("/api/transportations/{id}", created.id())
                .header(HttpHeaders.AUTHORIZATION, bearer(agencyToken))
                .exchange()
                .expectStatus().isForbidden();
    }
}
