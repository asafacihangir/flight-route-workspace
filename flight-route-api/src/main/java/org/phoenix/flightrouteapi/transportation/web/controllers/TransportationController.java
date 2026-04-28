package org.phoenix.flightrouteapi.transportation.web.controllers;

import org.phoenix.flightrouteapi.transportation.service.dtos.TransportationCmd;
import org.phoenix.flightrouteapi.transportation.service.TransportationService;
import org.phoenix.flightrouteapi.transportation.service.dtos.TransportationVM;
import org.phoenix.flightrouteapi.transportation.web.dtos.TransportationRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/api/transportations")
class TransportationController {

    private final TransportationService service;

    TransportationController(TransportationService service) {
        this.service = service;
    }

    @GetMapping
    List<TransportationVM> list() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    TransportationVM get(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    ResponseEntity<TransportationVM> create(@Valid @RequestBody TransportationRequest request,
                                            UriComponentsBuilder uriBuilder) {
        TransportationVM created = service.create(toCmd(request));
        var location = uriBuilder.path("/api/transportations/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    TransportationVM update(@PathVariable Long id, @Valid @RequestBody TransportationRequest request) {
        return service.update(id, toCmd(request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@PathVariable Long id) {
        service.delete(id);
    }

    private TransportationCmd toCmd(TransportationRequest r) {
        return new TransportationCmd(r.originId(), r.destinationId(), r.type(), r.operatingDays());
    }
}
