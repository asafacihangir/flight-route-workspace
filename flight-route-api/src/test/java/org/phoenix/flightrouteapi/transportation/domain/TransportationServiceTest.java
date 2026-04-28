package org.phoenix.flightrouteapi.transportation.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.phoenix.flightrouteapi.location.domain.LocationEntity;
import org.phoenix.flightrouteapi.location.repository.LocationRepository;
import org.phoenix.flightrouteapi.location.service.mapper.LocationMapper;
import org.phoenix.flightrouteapi.shared.exceptions.DomainException;
import org.phoenix.flightrouteapi.shared.exceptions.ResourceNotFoundException;
import org.phoenix.flightrouteapi.transportation.repository.TransportationRepository;
import org.phoenix.flightrouteapi.transportation.service.TransportationService;
import org.phoenix.flightrouteapi.transportation.service.dtos.TransportationCmd;
import org.phoenix.flightrouteapi.transportation.service.dtos.TransportationVM;
import org.phoenix.flightrouteapi.transportation.service.mapper.TransportationMapper;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransportationServiceTest {

    private static final long ORIGIN_ID = 1L;
    private static final long DESTINATION_ID = 2L;

    @Mock
    private TransportationRepository repository;

    @Mock
    private LocationRepository locationRepository;

    private TransportationService service;

    @BeforeEach
    void setUp() throws Exception {
        TransportationMapper mapper = newMapper(new LocationMapper());
        Constructor<TransportationService> ctor =
                TransportationService.class.getDeclaredConstructor(
                        TransportationRepository.class, LocationRepository.class, TransportationMapper.class);
        ctor.setAccessible(true);
        service = ctor.newInstance(repository, locationRepository, mapper);
    }

    private static TransportationMapper newMapper(LocationMapper locationMapper) throws Exception {
        Constructor<TransportationMapper> ctor =
                TransportationMapper.class.getDeclaredConstructor(LocationMapper.class);
        ctor.setAccessible(true);
        return ctor.newInstance(locationMapper);
    }

    private LocationEntity location(long id, String code) {
        LocationEntity entity = new LocationEntity("Loc-" + code, "Country", "City", code);
        ReflectionTestUtils.setField(entity, "id", id);
        return entity;
    }

    private TransportationEntity persisted(long id, LocationEntity origin, LocationEntity destination,
                                           TransportationType type, Set<Integer> days) {
        TransportationEntity entity = new TransportationEntity(origin, destination, type, days);
        ReflectionTestUtils.setField(entity, "id", id);
        return entity;
    }

    private TransportationCmd cmd(Long originId, Long destinationId, TransportationType type, Set<Integer> days) {
        return new TransportationCmd(originId, destinationId, type, days);
    }

    @Test
    void findAll_returnsAllTransportationsAsViewModels() {
        LocationEntity saw = location(ORIGIN_ID, "SAW");
        LocationEntity ist = location(DESTINATION_ID, "IST");
        when(repository.findAll()).thenReturn(List.of(
                persisted(10L, saw, ist, TransportationType.FLIGHT, Set.of(1, 2, 3))
        ));

        List<TransportationVM> result = service.findAll();

        assertThat(result).hasSize(1);
        TransportationVM vm = result.getFirst();
        assertThat(vm.id()).isEqualTo(10L);
        assertThat(vm.origin().id()).isEqualTo(ORIGIN_ID);
        assertThat(vm.destination().id()).isEqualTo(DESTINATION_ID);
        assertThat(vm.type()).isEqualTo(TransportationType.FLIGHT);
        assertThat(vm.operatingDays()).containsExactlyInAnyOrder(1, 2, 3);
    }

    @Test
    void findById_whenExists_returnsTransportation() {
        LocationEntity saw = location(ORIGIN_ID, "SAW");
        LocationEntity ist = location(DESTINATION_ID, "IST");
        when(repository.findById(10L)).thenReturn(Optional.of(
                persisted(10L, saw, ist, TransportationType.BUS, Set.of(1))
        ));

        TransportationVM vm = service.findById(10L);

        assertThat(vm.id()).isEqualTo(10L);
        assertThat(vm.type()).isEqualTo(TransportationType.BUS);
    }

    @Test
    void findById_whenMissing_throwsResourceNotFound() {
        when(repository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(42L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("42");
    }

    @Test
    void create_whenValid_persistsAndReturnsVM() {
        LocationEntity saw = location(ORIGIN_ID, "SAW");
        LocationEntity ist = location(DESTINATION_ID, "IST");
        when(locationRepository.findById(ORIGIN_ID)).thenReturn(Optional.of(saw));
        when(locationRepository.findById(DESTINATION_ID)).thenReturn(Optional.of(ist));
        when(repository.save(any(TransportationEntity.class))).thenAnswer(inv -> {
            TransportationEntity e = inv.getArgument(0);
            ReflectionTestUtils.setField(e, "id", 50L);
            return e;
        });

        TransportationVM created = service.create(
                cmd(ORIGIN_ID, DESTINATION_ID, TransportationType.FLIGHT, Set.of(1, 2, 3)));

        assertThat(created.id()).isEqualTo(50L);
        assertThat(created.origin().id()).isEqualTo(ORIGIN_ID);
        assertThat(created.destination().id()).isEqualTo(DESTINATION_ID);
        assertThat(created.type()).isEqualTo(TransportationType.FLIGHT);
        assertThat(created.operatingDays()).containsExactlyInAnyOrder(1, 2, 3);
        verify(repository).save(any(TransportationEntity.class));
    }

    @Test
    void create_whenOriginMissing_throwsResourceNotFound() {
        when(locationRepository.findById(ORIGIN_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(
                cmd(ORIGIN_ID, DESTINATION_ID, TransportationType.FLIGHT, Set.of(1))))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(String.valueOf(ORIGIN_ID));

        verify(repository, never()).save(any());
    }

    @Test
    void create_whenDestinationMissing_throwsResourceNotFound() {
        LocationEntity saw = location(ORIGIN_ID, "SAW");
        when(locationRepository.findById(ORIGIN_ID)).thenReturn(Optional.of(saw));
        when(locationRepository.findById(DESTINATION_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(
                cmd(ORIGIN_ID, DESTINATION_ID, TransportationType.FLIGHT, Set.of(1))))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(String.valueOf(DESTINATION_ID));

        verify(repository, never()).save(any());
    }

    @Test
    void create_whenOriginAndDestinationSame_throwsDomainException() {
        assertThatThrownBy(() -> service.create(
                cmd(ORIGIN_ID, ORIGIN_ID, TransportationType.FLIGHT, Set.of(1))))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("origin and destination");

        verify(repository, never()).save(any());
    }

    @Test
    void create_whenTypeNull_throwsDomainException() {
        assertThatThrownBy(() -> service.create(
                cmd(ORIGIN_ID, DESTINATION_ID, null, Set.of(1))))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("type");

        verify(repository, never()).save(any());
    }

    @ParameterizedTest
    @NullAndEmptySource
    void create_whenOperatingDaysNullOrEmpty_throwsDomainException(Set<Integer> days) {
        assertThatThrownBy(() -> service.create(
                cmd(ORIGIN_ID, DESTINATION_ID, TransportationType.FLIGHT, days)))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("operatingDays");

        verify(repository, never()).save(any());
    }

    @Test
    void create_whenOriginIdNull_throwsDomainException() {
        assertThatThrownBy(() -> service.create(
                cmd(null, DESTINATION_ID, TransportationType.FLIGHT, Set.of(1))))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("originId");

        verify(repository, never()).save(any());
    }

    @Test
    void create_whenDestinationIdNull_throwsDomainException() {
        assertThatThrownBy(() -> service.create(
                cmd(ORIGIN_ID, null, TransportationType.FLIGHT, Set.of(1))))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("destinationId");

        verify(repository, never()).save(any());
    }

    @Test
    void update_whenValid_updatesEntityAndPersists() {
        LocationEntity saw = location(ORIGIN_ID, "SAW");
        LocationEntity ist = location(DESTINATION_ID, "IST");
        TransportationEntity existing = persisted(10L, saw, ist, TransportationType.BUS, Set.of(1));

        when(repository.findById(10L)).thenReturn(Optional.of(existing));
        when(locationRepository.findById(ORIGIN_ID)).thenReturn(Optional.of(saw));
        when(locationRepository.findById(DESTINATION_ID)).thenReturn(Optional.of(ist));
        when(repository.save(existing)).thenReturn(existing);

        TransportationVM updated = service.update(10L,
                cmd(ORIGIN_ID, DESTINATION_ID, TransportationType.FLIGHT, Set.of(2, 3, 4)));

        assertThat(updated.id()).isEqualTo(10L);
        assertThat(updated.type()).isEqualTo(TransportationType.FLIGHT);
        assertThat(updated.operatingDays()).containsExactlyInAnyOrder(2, 3, 4);
        assertThat(existing.getType()).isEqualTo(TransportationType.FLIGHT);
        verify(repository).save(existing);
    }

    @Test
    void update_whenMissing_throwsResourceNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(99L,
                cmd(ORIGIN_ID, DESTINATION_ID, TransportationType.FLIGHT, Set.of(1))))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");

        verify(repository, never()).save(any());
    }

    @Test
    void update_whenInvalidCmd_doesNotTouchRepository() {
        assertThatThrownBy(() -> service.update(10L,
                cmd(ORIGIN_ID, ORIGIN_ID, TransportationType.FLIGHT, Set.of(1))))
                .isInstanceOf(DomainException.class);

        verify(repository, never()).findById(any());
        verify(repository, never()).save(any());
    }

    @Test
    void delete_whenExists_callsRepositoryDelete() {
        LocationEntity saw = location(ORIGIN_ID, "SAW");
        LocationEntity ist = location(DESTINATION_ID, "IST");
        TransportationEntity existing = persisted(7L, saw, ist, TransportationType.FLIGHT, Set.of(1));
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
