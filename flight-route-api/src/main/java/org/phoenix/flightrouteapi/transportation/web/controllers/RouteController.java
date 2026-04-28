package org.phoenix.flightrouteapi.transportation.web.controllers;

import org.phoenix.flightrouteapi.transportation.service.RouteService;
import org.phoenix.flightrouteapi.transportation.service.dtos.RouteVM;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/routes")
class RouteController {

    private final RouteService service;

    RouteController(RouteService service) {
        this.service = service;
    }

    @GetMapping
    List<RouteVM> findRoutes(
            @RequestParam Long originId,
            @RequestParam Long destinationId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return service.findRoutes(originId, destinationId, date);
    }
}
