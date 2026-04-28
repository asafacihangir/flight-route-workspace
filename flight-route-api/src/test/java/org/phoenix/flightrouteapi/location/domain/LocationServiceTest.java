package org.phoenix.flightrouteapi.location.domain;

import org.phoenix.flightrouteapi.location.repository.LocationRepository;
import org.phoenix.flightrouteapi.location.service.dtos.LocationVM;
import org.phoenix.flightrouteapi.location.service.mapper.LocationMapper;
import org.phoenix.flightrouteapi.location.service.LocationService;
import org.phoenix.flightrouteapi.location.web.dtos.LocationRequest;
import org.phoenix.flightrouteapi.shared.exceptions.DomainException;
import org.phoenix.flightrouteapi.shared.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocationServiceTest {

    private static final String NAME = "Sabiha";
    private static final String COUNTRY = "Turkiye";
    private static final String CITY = "Istanbul";
    private static final String CODE = "SAW";

    @Mock
    private LocationRepository repository;

    private LocationService service;

    @BeforeEach
    void setUp() {
        service = new LocationService(repository, new LocationMapper());
    }

    private LocationRequest request(String code) {
        return new LocationRequest(NAME, COUNTRY, CITY, code);
    }

    private LocationEntity persisted(long id, String name, String country, String city, String code) {
        LocationEntity entity = new LocationEntity(name, country, city, code);
        ReflectionTestUtils.setField(entity, "id", id);
        return entity;
    }

    @Test
    void findAll_returnsAllLocationsAsViewModels() {
        when(repository.findAll()).thenReturn(List.of(
                persisted(1L, NAME, COUNTRY, CITY, CODE),
                persisted(2L, "Istanbul Airport", COUNTRY, CITY, "IST")
        ));

        List<LocationVM> result = service.findAll();

        assertThat(result).containsExactly(
                new LocationVM(1L, NAME, COUNTRY, CITY, CODE),
                new LocationVM(2L, "Istanbul Airport", COUNTRY, CITY, "IST")
        );
    }

    @Test
    void findById_whenExists_returnsLocation() {
        when(repository.findById(1L)).thenReturn(Optional.of(persisted(1L, NAME, COUNTRY, CITY, CODE)));

        LocationVM vm = service.findById(1L);

        assertThat(vm.id()).isEqualTo(1L);
        assertThat(vm.code()).isEqualTo(CODE);
    }

    @Test
    void findById_whenMissing_throwsResourceNotFound() {
        when(repository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(42L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("42");
    }

    @Test
    void create_whenValid_persistsNormalizedEntity() {
        when(repository.existsByCodeIgnoreCase("saw")).thenReturn(false);
        when(repository.save(any(LocationEntity.class))).thenAnswer(inv -> {
            LocationEntity e = inv.getArgument(0);
            ReflectionTestUtils.setField(e, "id", 10L);
            return e;
        });

        LocationVM created = service.create(request("saw"));

        assertThat(created.id()).isEqualTo(10L);
        assertThat(created.code()).isEqualTo(CODE);
        assertThat(created.name()).isEqualTo(NAME);
        verify(repository).save(any(LocationEntity.class));
    }

    @Test
    void create_whenCodeAlreadyExists_throwsDomainException() {
        when(repository.existsByCodeIgnoreCase(CODE)).thenReturn(true);

        assertThatThrownBy(() -> service.create(request(CODE)))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining(CODE);

        verify(repository, never()).save(any());
    }

    @ParameterizedTest
    @CsvSource({
            "'',Turkiye,Istanbul,SAW,name",
            "Sabiha,'',Istanbul,SAW,country",
            "Sabiha,Turkiye,'',SAW,city",
            "Sabiha,Turkiye,Istanbul,'',code"
    })
    void create_whenAnyFieldBlank_throwsIllegalArgument(String name, String country, String city, String code, String field) {
        assertThatThrownBy(() -> service.create(new LocationRequest(name, country, city, code)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(field);

        verify(repository, never()).save(any());
    }

    @Test
    void update_whenCodeUnchanged_updatesOtherFields() {
        LocationEntity existing = persisted(5L, "Old Name", "OldCountry", "OldCity", CODE);
        when(repository.findById(5L)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        LocationVM updated = service.update(5L, new LocationRequest("New Name", COUNTRY, CITY, CODE));

        assertThat(updated.name()).isEqualTo("New Name");
        assertThat(updated.country()).isEqualTo(COUNTRY);
        assertThat(updated.city()).isEqualTo(CITY);
        assertThat(updated.code()).isEqualTo(CODE);
        verify(repository, never()).existsByCodeIgnoreCase(any());
        verify(repository).save(existing);
    }

    @Test
    void update_whenNewCodeFree_changesCode() {
        LocationEntity existing = persisted(5L, NAME, COUNTRY, CITY, CODE);
        when(repository.findById(5L)).thenReturn(Optional.of(existing));
        when(repository.existsByCodeIgnoreCase("SAW2")).thenReturn(false);
        when(repository.save(existing)).thenReturn(existing);

        LocationVM updated = service.update(5L, request("SAW2"));

        assertThat(updated.code()).isEqualTo("SAW2");
        assertThat(existing.getCode()).isEqualTo("SAW2");
        verify(repository).save(existing);
    }

    @Test
    void update_whenNewCodeTaken_throwsDomainException() {
        LocationEntity existing = persisted(5L, NAME, COUNTRY, CITY, CODE);
        when(repository.findById(5L)).thenReturn(Optional.of(existing));
        when(repository.existsByCodeIgnoreCase("IST")).thenReturn(true);

        assertThatThrownBy(() -> service.update(5L, request("IST")))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("IST");

        verify(repository, never()).save(any());
    }

    @Test
    void update_whenMissing_throwsResourceNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(99L, request("XYZ")))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void delete_whenExists_removesEntity() {
        LocationEntity existing = persisted(7L, NAME, COUNTRY, CITY, CODE);
        when(repository.findById(7L)).thenReturn(Optional.of(existing));

        service.delete(7L);

        verify(repository).delete(existing);
    }

    @Test
    void delete_whenMissing_throwsResourceNotFound() {
        when(repository.findById(123L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(123L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("123");

        verify(repository, never()).delete(any());
    }
}
