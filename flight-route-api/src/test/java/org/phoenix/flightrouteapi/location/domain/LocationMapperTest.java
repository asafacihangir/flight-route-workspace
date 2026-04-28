package org.phoenix.flightrouteapi.location.domain;

import org.phoenix.flightrouteapi.location.service.dtos.LocationVM;
import org.phoenix.flightrouteapi.location.service.mapper.LocationMapper;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class LocationMapperTest {

    private final LocationMapper mapper = new LocationMapper();

    @Test
    void toVMCopiesAllFieldsAndNormalizesCode() {
        LocationEntity entity = new LocationEntity("Sabiha", "Turkiye", "Istanbul", "saw");
        ReflectionTestUtils.setField(entity, "id", 3L);

        LocationVM vm = mapper.toVM(entity);

        assertThat(vm.id()).isEqualTo(3L);
        assertThat(vm.name()).isEqualTo("Sabiha");
        assertThat(vm.country()).isEqualTo("Turkiye");
        assertThat(vm.city()).isEqualTo("Istanbul");
        assertThat(vm.code()).isEqualTo("SAW");
    }
}
