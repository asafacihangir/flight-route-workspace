package org.phoenix.flightrouteapi.transportation.domain;

import org.junit.jupiter.api.Test;
import org.phoenix.flightrouteapi.location.domain.LocationEntity;
import org.phoenix.flightrouteapi.location.service.mapper.LocationMapper;
import org.phoenix.flightrouteapi.transportation.service.dtos.TransportationVM;
import org.phoenix.flightrouteapi.transportation.service.mapper.TransportationMapper;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Constructor;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class TransportationMapperTest {

    private final TransportationMapper mapper = newMapper();

    private static TransportationMapper newMapper() {
        try {
            Constructor<TransportationMapper> ctor =
                    TransportationMapper.class.getDeclaredConstructor(LocationMapper.class);
            ctor.setAccessible(true);
            return ctor.newInstance(new LocationMapper());
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    private static LocationEntity location(long id, String code) {
        LocationEntity entity = new LocationEntity("Loc-" + code, "Country", "City", code);
        ReflectionTestUtils.setField(entity, "id", id);
        return entity;
    }

    @Test
    void toVMCopiesAllFields() {
        LocationEntity origin = location(1L, "saw");
        LocationEntity destination = location(2L, "ist");
        TransportationEntity entity = new TransportationEntity(
                origin, destination, TransportationType.FLIGHT, new LinkedHashSet<>(Set.of(1, 2, 3)));
        ReflectionTestUtils.setField(entity, "id", 10L);

        TransportationVM vm = mapper.toVM(entity);

        assertThat(vm.id()).isEqualTo(10L);
        assertThat(vm.origin().id()).isEqualTo(1L);
        assertThat(vm.origin().code()).isEqualTo("SAW");
        assertThat(vm.destination().id()).isEqualTo(2L);
        assertThat(vm.destination().code()).isEqualTo("IST");
        assertThat(vm.type()).isEqualTo(TransportationType.FLIGHT);
        assertThat(vm.operatingDays()).containsExactlyInAnyOrder(1, 2, 3);
    }

    @Test
    void toVMOperatingDaysAreDefensivelyCopied() {
        LocationEntity origin = location(1L, "saw");
        LocationEntity destination = location(2L, "ist");
        TransportationEntity entity = new TransportationEntity(
                origin, destination, TransportationType.BUS, new LinkedHashSet<>(Set.of(1, 2)));
        ReflectionTestUtils.setField(entity, "id", 11L);

        TransportationVM vm = mapper.toVM(entity);

        assertThat(vm.operatingDays()).isNotSameAs(entity.getOperatingDays());
        assertThat(vm.operatingDays()).containsExactlyInAnyOrderElementsOf(entity.getOperatingDays());
    }
}
