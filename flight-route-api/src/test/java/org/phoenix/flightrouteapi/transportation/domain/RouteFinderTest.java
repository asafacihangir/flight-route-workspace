package org.phoenix.flightrouteapi.transportation.domain;

import org.phoenix.flightrouteapi.location.domain.LocationEntity;
import org.phoenix.flightrouteapi.transportation.service.RouteFinder;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RouteFinderTest {

    private final RouteFinder finder = new RouteFinder();

    private static final LocalDate WED_2025_03_12 = LocalDate.of(2025, 3, 12);
    private static final LocalDate TUE_2025_03_11 = LocalDate.of(2025, 3, 11);

    private static final Set<Integer> EVERY_DAY = Set.of(1, 2, 3, 4, 5, 6, 7);

    @Test
    void flightOnlyBetweenQueryEndpoints() {
        LocationEntity ist = location(1L, "IST");
        LocationEntity lhr = location(2L, "LHR");

        TransportationEntity flight = transportation(10L, ist, lhr, TransportationType.FLIGHT, EVERY_DAY);

        List<List<TransportationEntity>> routes = finder.findRoutes(List.of(flight), 1L, 2L, WED_2025_03_12);

        assertThat(routes).hasSize(1);
        assertThat(routes.get(0)).containsExactly(flight);
    }

    @Test
    void preFlightThenFlight() {
        LocationEntity taksim = location(1L, "TAK");
        LocationEntity ist = location(2L, "IST");
        LocationEntity lhr = location(3L, "LHR");

        TransportationEntity bus = transportation(10L, taksim, ist, TransportationType.BUS, EVERY_DAY);
        TransportationEntity flight = transportation(11L, ist, lhr, TransportationType.FLIGHT, EVERY_DAY);

        List<List<TransportationEntity>> routes = finder.findRoutes(List.of(bus, flight), 1L, 3L, WED_2025_03_12);

        assertThat(routes).hasSize(1);
        assertThat(routes.get(0)).containsExactly(bus, flight);
    }

    @Test
    void flightThenPostFlight() {
        LocationEntity ist = location(1L, "IST");
        LocationEntity lhr = location(2L, "LHR");
        LocationEntity wembley = location(3L, "WEM");

        TransportationEntity flight = transportation(10L, ist, lhr, TransportationType.FLIGHT, EVERY_DAY);
        TransportationEntity uber = transportation(11L, lhr, wembley, TransportationType.UBER, EVERY_DAY);

        List<List<TransportationEntity>> routes = finder.findRoutes(List.of(flight, uber), 1L, 3L, WED_2025_03_12);

        assertThat(routes).hasSize(1);
        assertThat(routes.get(0)).containsExactly(flight, uber);
    }

    @Test
    void fullTransferChainEmitsCombinatorialRoutes() {
        LocationEntity taksim = location(1L, "TAK");
        LocationEntity ist = location(2L, "IST");
        LocationEntity saw = location(3L, "SAW");
        LocationEntity lhr = location(4L, "LHR");
        LocationEntity wembley = location(5L, "WEM");

        TransportationEntity taksimToIstByUber = transportation(10L, taksim, ist, TransportationType.UBER, EVERY_DAY);
        TransportationEntity taksimToIstBySubway = transportation(11L, taksim, ist, TransportationType.SUBWAY, EVERY_DAY);
        TransportationEntity taksimToSawByBus = transportation(12L, taksim, saw, TransportationType.BUS, EVERY_DAY);
        TransportationEntity istToLhr = transportation(13L, ist, lhr, TransportationType.FLIGHT, EVERY_DAY);
        TransportationEntity sawToLhr = transportation(14L, saw, lhr, TransportationType.FLIGHT, EVERY_DAY);
        TransportationEntity lhrToWembleyByBus = transportation(15L, lhr, wembley, TransportationType.BUS, EVERY_DAY);
        TransportationEntity lhrToWembleyByUber = transportation(16L, lhr, wembley, TransportationType.UBER, EVERY_DAY);

        List<TransportationEntity> all = List.of(
                taksimToIstByUber, taksimToIstBySubway, taksimToSawByBus,
                istToLhr, sawToLhr,
                lhrToWembleyByBus, lhrToWembleyByUber
        );

        List<List<TransportationEntity>> routes = finder.findRoutes(all, 1L, 5L, WED_2025_03_12);

        assertThat(routes).hasSize(6);
        assertThat(routes).allSatisfy(r -> assertThat(r).hasSize(3));
        assertThat(routes).contains(List.of(taksimToIstByUber, istToLhr, lhrToWembleyByBus));
        assertThat(routes).contains(List.of(taksimToIstByUber, istToLhr, lhrToWembleyByUber));
        assertThat(routes).contains(List.of(taksimToIstBySubway, istToLhr, lhrToWembleyByBus));
        assertThat(routes).contains(List.of(taksimToIstBySubway, istToLhr, lhrToWembleyByUber));
        assertThat(routes).contains(List.of(taksimToSawByBus, sawToLhr, lhrToWembleyByBus));
        assertThat(routes).contains(List.of(taksimToSawByBus, sawToLhr, lhrToWembleyByUber));
    }

    @Test
    void rejectsChainsWithoutFlight() {
        LocationEntity a = location(1L, "A");
        LocationEntity b = location(2L, "B");
        LocationEntity c = location(3L, "C");

        TransportationEntity uber = transportation(10L, a, b, TransportationType.UBER, EVERY_DAY);
        TransportationEntity bus = transportation(11L, b, c, TransportationType.BUS, EVERY_DAY);

        List<List<TransportationEntity>> routes = finder.findRoutes(List.of(uber, bus), 1L, 3L, WED_2025_03_12);

        assertThat(routes).isEmpty();
    }

    @Test
    void filtersUnavailableOnDate() {
        LocationEntity ist = location(1L, "IST");
        LocationEntity lhr = location(2L, "LHR");

        TransportationEntity flight = transportation(10L, ist, lhr, TransportationType.FLIGHT, Set.of(1, 3, 7));

        assertThat(finder.findRoutes(List.of(flight), 1L, 2L, WED_2025_03_12)).hasSize(1);
        assertThat(finder.findRoutes(List.of(flight), 1L, 2L, TUE_2025_03_11)).isEmpty();
    }

    @Test
    void rejectsTwoFlightsBetweenEndpoints() {
        LocationEntity a = location(1L, "A");
        LocationEntity b = location(2L, "B");
        LocationEntity c = location(3L, "C");

        TransportationEntity flight1 = transportation(10L, a, b, TransportationType.FLIGHT, EVERY_DAY);
        TransportationEntity flight2 = transportation(11L, b, c, TransportationType.FLIGHT, EVERY_DAY);

        List<List<TransportationEntity>> routes = finder.findRoutes(List.of(flight1, flight2), 1L, 3L, WED_2025_03_12);

        assertThat(routes).isEmpty();
    }

    private static LocationEntity location(Long id, String code) {
        try {
            java.lang.reflect.Constructor<LocationEntity> ctor = LocationEntity.class
                    .getDeclaredConstructor(String.class, String.class, String.class, String.class);
            ctor.setAccessible(true);
            LocationEntity e = ctor.newInstance("N-" + code, "Country", "City", code);
            setField(e, "id", id);
            return e;
        } catch (ReflectiveOperationException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static TransportationEntity transportation(Long id, LocationEntity origin, LocationEntity destination,
                                                       TransportationType type, Set<Integer> days) {
        TransportationEntity e = new TransportationEntity(origin, destination, type, days);
        setField(e, "id", id);
        return e;
    }

    private static void setField(Object target, String name, Object value) {
        try {
            Field f = findField(target.getClass(), name);
            f.setAccessible(true);
            f.set(target, value);
        } catch (ReflectiveOperationException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static Field findField(Class<?> cls, String name) throws NoSuchFieldException {
        Class<?> c = cls;
        while (c != null) {
            try {
                return c.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
                c = c.getSuperclass();
            }
        }
        throw new NoSuchFieldException(name);
    }
}
