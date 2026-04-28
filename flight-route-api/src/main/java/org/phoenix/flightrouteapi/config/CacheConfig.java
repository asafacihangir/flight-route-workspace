package org.phoenix.flightrouteapi.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@EnableCaching
public class CacheConfig {

    public static final String ROUTES_CACHE = "routes";

    private final Duration routesTtl;
    private final long routesMaxSize;

    public CacheConfig(@Value("${app.cache.routes.ttl:PT1H}") Duration routesTtl,
                       @Value("${app.cache.routes.max-size:10000}") long routesMaxSize) {
        this.routesTtl = routesTtl;
        this.routesMaxSize = routesMaxSize;
    }

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager(ROUTES_CACHE);
        manager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(routesTtl)
                .maximumSize(routesMaxSize));
        return manager;
    }
}
