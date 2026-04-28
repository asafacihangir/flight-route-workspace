package org.phoenix.flightrouteapi.location.web.controllers;

import org.phoenix.flightrouteapi.location.service.LocationService;
import org.phoenix.flightrouteapi.location.service.dtos.LocationVM;
import org.phoenix.flightrouteapi.location.web.dtos.LocationRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
@PreAuthorize("hasRole('ADMIN')")
class LocationController {

    private final LocationService service;

    LocationController(LocationService service) {
        this.service = service;
    }

    @GetMapping
    List<LocationVM> list() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    LocationVM get(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    ResponseEntity<LocationVM> create(@Valid @RequestBody LocationRequest request, UriComponentsBuilder uriBuilder) {
        LocationVM created = service.create(request);
        var location = uriBuilder.path("/api/locations/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    LocationVM update(@PathVariable Long id, @Valid @RequestBody LocationRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@PathVariable Long id) {
        service.delete(id);
    }


}
