package org.phoenix.flightrouteapi.transportation.service;

import org.phoenix.flightrouteapi.transportation.domain.TransportationEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class RouteFinder {

    public List<List<TransportationEntity>> findRoutes(List<TransportationEntity> all,
                                                       Long originId,
                                                       Long destinationId,
                                                       LocalDate date) {



        // Taksim

        ///  Tasim - Sabiha (Uber)

        // Sabiha - Londora (Flight)

        //Londora - New York Havalima (Flight)

        // New York Havalimanı - Beyaz Saray (Uber)

        // Beyaz Saray




        //destion'daki önceki fligtları bull

        int dayOfWeek = date.getDayOfWeek().getValue();
        List<TransportationEntity> available = all.stream()
                .filter(t -> t.getOperatingDays().contains(dayOfWeek))
                .toList();

        Map<Long, List<TransportationEntity>> nonFlightsByOrigin = available.stream()
                .filter(t -> !t.getType().isFlight())
                .collect(Collectors.groupingBy(t -> t.getOrigin().getId()));

        List<List<TransportationEntity>> result = new ArrayList<>();
        for (TransportationEntity flight : available) {
            if (!flight.getType().isFlight()) continue;

            List<List<TransportationEntity>> befores = transferOptions(
                    nonFlightsByOrigin, originId, flight.getOrigin().getId());
            List<List<TransportationEntity>> afters = transferOptions(
                    nonFlightsByOrigin, flight.getDestination().getId(), destinationId);

            for (List<TransportationEntity> before : befores) {
                for (List<TransportationEntity> after : afters) {
                    List<TransportationEntity> route = new ArrayList<>(3);
                    route.addAll(before);
                    route.add(flight);
                    route.addAll(after);
                    result.add(route);
                }
            }
        }
        return result;
    }

    private List<List<TransportationEntity>> transferOptions(Map<Long, List<TransportationEntity>> byOrigin,
                                                             Long fromId,
                                                             Long toId) {
        if (fromId.equals(toId)) {
            return List.of(List.of());
        }
        List<TransportationEntity> candidates = byOrigin.getOrDefault(fromId, Collections.emptyList());
        List<List<TransportationEntity>> options = new ArrayList<>(candidates.size());
        for (TransportationEntity t : candidates) {
            if (t.endsAt(toId)) {
                options.add(List.of(t));
            }
        }
        return options;
    }
}
