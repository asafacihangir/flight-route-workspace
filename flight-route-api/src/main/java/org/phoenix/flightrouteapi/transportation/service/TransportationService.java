package org.phoenix.flightrouteapi.transportation.service;

import org.phoenix.flightrouteapi.config.CacheConfig;
import org.phoenix.flightrouteapi.location.domain.LocationEntity;
import org.phoenix.flightrouteapi.location.repository.LocationRepository;
import org.phoenix.flightrouteapi.shared.exceptions.DomainException;
import org.phoenix.flightrouteapi.shared.exceptions.ResourceNotFoundException;
import org.phoenix.flightrouteapi.transportation.service.dtos.TransportationCmd;
import org.phoenix.flightrouteapi.transportation.domain.TransportationEntity;
import org.phoenix.flightrouteapi.transportation.service.dtos.TransportationVM;
import org.phoenix.flightrouteapi.transportation.repository.TransportationRepository;
import org.phoenix.flightrouteapi.transportation.service.mapper.TransportationMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
public class TransportationService {

    private final TransportationRepository repository;
    private final LocationRepository locationRepository;
    private final TransportationMapper mapper;

    TransportationService(TransportationRepository repository,
                          LocationRepository locationRepository,
                          TransportationMapper mapper) {
        this.repository = repository;
        this.locationRepository = locationRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<TransportationVM> findAll() {
        return repository.findAll().stream().map(mapper::toVM).toList();
    }

    @Transactional(readOnly = true)
    public TransportationVM findById(Long id) {
        return mapper.toVM(getEntity(id));
    }

    @Transactional
    @CacheEvict(value = CacheConfig.ROUTES_CACHE, allEntries = true)
    public TransportationVM create(TransportationCmd cmd) {
        validateCmd(cmd);
        LocationEntity origin = getLocation(cmd.originId());
        LocationEntity destination = getLocation(cmd.destinationId());
        TransportationEntity entity = new TransportationEntity(origin, destination, cmd.type(),
                Set.copyOf(cmd.operatingDays()));
        return mapper.toVM(repository.save(entity));
    }

    @Transactional
    @CacheEvict(value = CacheConfig.ROUTES_CACHE, allEntries = true)
    public TransportationVM update(Long id, TransportationCmd cmd) {
        validateCmd(cmd);
        TransportationEntity entity = getEntity(id);
        LocationEntity origin = getLocation(cmd.originId());
        LocationEntity destination = getLocation(cmd.destinationId());
        entity.update(origin, destination, cmd.type(), Set.copyOf(cmd.operatingDays()));
        return mapper.toVM(repository.save(entity));
    }

    @Transactional
    @CacheEvict(value = CacheConfig.ROUTES_CACHE, allEntries = true)
    public void delete(Long id) {
        TransportationEntity entity = getEntity(id);
        repository.delete(entity);
    }

    private void validateCmd(TransportationCmd cmd) {
        if (cmd.originId() == null || cmd.destinationId() == null) {
            throw new DomainException("originId and destinationId are required");
        }
        if (cmd.originId().equals(cmd.destinationId())) {
            throw new DomainException("origin and destination must be different");
        }
        if (cmd.type() == null) {
            throw new DomainException("type is required");
        }
        if (cmd.operatingDays() == null || cmd.operatingDays().isEmpty()) {
            throw new DomainException("operatingDays must contain at least one day");
        }
    }

    private TransportationEntity getEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transportation not found with id: " + id));
    }

    private LocationEntity getLocation(Long id) {
        return locationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found with id: " + id));
    }
}
