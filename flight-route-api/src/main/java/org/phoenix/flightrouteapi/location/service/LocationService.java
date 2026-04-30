package org.phoenix.flightrouteapi.location.service;

import org.phoenix.flightrouteapi.config.CacheConfig;
import org.phoenix.flightrouteapi.location.domain.LocationEntity;
import org.phoenix.flightrouteapi.location.repository.LocationRepository;
import org.phoenix.flightrouteapi.location.service.dtos.LocationVM;
import org.phoenix.flightrouteapi.location.service.mapper.LocationMapper;
import org.phoenix.flightrouteapi.location.web.dtos.LocationRequest;
import org.phoenix.flightrouteapi.shared.exceptions.DomainException;
import org.phoenix.flightrouteapi.shared.exceptions.ResourceNotFoundException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LocationService {

    private final LocationRepository repository;
    private final LocationMapper mapper;

    public LocationService(LocationRepository repository, LocationMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<LocationVM> findAll() {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt", "id");
        return repository.findAll(sort).stream().map(mapper::toVM).toList();
    }

    @Transactional(readOnly = true)
    public LocationVM findById(Long id) {
        return mapper.toVM(getEntity(id));
    }

    @Transactional
    @CacheEvict(value = CacheConfig.ROUTES_CACHE, allEntries = true)
    public LocationVM create(LocationRequest locationRequest) {
        if (repository.existsByCodeIgnoreCase(locationRequest.code())) {
            throw new DomainException("Location with code '" + locationRequest.code() + "' already exists");
        }
        LocationEntity entity = new LocationEntity(locationRequest.name(), locationRequest.country(), locationRequest.city(), locationRequest.code());
        return mapper.toVM(repository.save(entity));
    }

    @Transactional
    @CacheEvict(value = CacheConfig.ROUTES_CACHE, allEntries = true)
    public LocationVM update(Long id, LocationRequest locationRequest) {
        LocationEntity entity = getEntity(id);
        String newCode = locationRequest.code().trim().toUpperCase();

        if (!newCode.equals(entity.getCode()) && repository.existsByCodeIgnoreCase(newCode)) {
            throw new DomainException("Location with code '" + newCode + "' already exists");
        }
        entity.update(locationRequest.name(), locationRequest.country(), locationRequest.city(), locationRequest.code());
        return mapper.toVM(repository.save(entity));
    }

    @Transactional
    @CacheEvict(value = CacheConfig.ROUTES_CACHE, allEntries = true)
    public void delete(Long id) {
        LocationEntity entity = getEntity(id);
        repository.delete(entity);
    }

    private LocationEntity getEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found with id: " + id));
    }
}
