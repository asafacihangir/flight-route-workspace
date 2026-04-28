package org.phoenix.flightrouteapi.transportation.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Converter
class OperatingDaysConverter implements AttributeConverter<Set<Integer>, String> {

    @Override
    public String convertToDatabaseColumn(Set<Integer> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return "";
        }
        return new TreeSet<>(attribute).stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
    }

    @Override
    public Set<Integer> convertToEntityAttribute(String dbData) {
        Set<Integer> result = new LinkedHashSet<>();
        if (dbData == null || dbData.isBlank()) {
            return result;
        }
        for (String token : dbData.split(",")) {
            String trimmed = token.trim();
            if (!trimmed.isEmpty()) {
                result.add(Integer.parseInt(trimmed));
            }
        }
        return result;
    }
}
