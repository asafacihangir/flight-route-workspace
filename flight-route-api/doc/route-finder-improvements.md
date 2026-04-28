# Route Finder Algorithm — Improvement Spec

**Status:** Draft
**Date:** 2026-04-25
**Scope:** `RouteFinder`, `RouteService`, `TransportationRepository`
**Related:** [route-definition.md](./route-definition.md), [use-cases/uc-03-route-arama.md](./use-cases/uc-03-route-arama.md), [product-vision.md](./product-vision.md)

---

## 1. Background

The current `findRoutes` implementation uses a FLIGHT-centric cartesian join algorithm:

1. Load all transportations from DB.
2. Filter in-memory by `operatingDays.contains(dayOfWeek)`.
3. For each FLIGHT, compute `before × after` transfer combinations.
4. Build routes with structural guarantees (max 3 legs, exactly 1 FLIGHT).

The algorithmic core is sound. Improvements below target **non-functional concerns** (performance, scalability, cache) and one minor algorithmic optimization (HashMap indexing).

---

## 2. Findings & Improvement Items

### 2.1 DB-Level Day Filter Missing

**Location:** `RouteService.java:41` — `transportationRepository.findAll()`

**Problem:** All transportation rows are loaded regardless of the requested date. In-memory filter discards rows whose `operatingDays` do not contain the target day. With a large dataset (thousands of transportations), this causes unnecessary I/O.

**Proposed fix:**
- Add a custom repository method that filters at the DB layer.
- Use PostgreSQL `array_position(operating_days, :day) IS NOT NULL` or a GIN-indexed `@>` containment query.
- Add a GIN index on `operating_days`.

**Acceptance:**
- DB returns only transportations operating on the requested day.
- Verify with EXPLAIN ANALYZE that the GIN index is used.

---

### 2.2 HashMap Index for Transfer Lookup

**Location:** `RouteFinder.java:46-50` — `transferOptions` linearly scans `available` for every FLIGHT.

**Problem:** Current complexity is **O(F × A)** where F = flights and A = available transportations. For each flight, the full available list is streamed twice (once for `before`, once for `after`).

**Proposed fix:** Build two maps once at the start of `findRoutes`:

```java
Map<Long, List<TransportationEntity>> nonFlightsByOrigin =
    available.stream()
        .filter(t -> !t.getType().isFlight())
        .collect(groupingBy(t -> t.getOrigin().getId()));

Map<Long, List<TransportationEntity>> nonFlightsByDestination =
    available.stream()
        .filter(t -> !t.getType().isFlight())
        .collect(groupingBy(t -> t.getDestination().getId()));
```

Lookup becomes O(1). New complexity: **O(F + A)** for index build + **O(F × b × a)** for join, where b/a are average matches per flight (typically small).

**Acceptance:**
- `RouteFinder` no longer scans `available` inside the per-flight loop.
- Existing tests pass without modification.

---

### 2.3 Response Cache Missing

**Location:** `RouteService.findRoutes` — no `@Cacheable` annotation.

**Problem:** UC-03 step 9 explicitly requires caching for high-load scenarios. Repeated identical requests `(originId, destinationId, date)` re-run the full algorithm and DB query.

**Proposed fix:**
- Add Caffeine (in-memory) or Redis cache layer.
- Annotate `findRoutes` with `@Cacheable(value = "routes", key = "#originId + '_' + #destinationId + '_' + #date")`.
- TTL: 1 hour (configurable via `application.properties`).

**Acceptance:**
- Second identical request hits cache (verified via cache metrics or log).
- TTL configurable.

---

### 2.4 Cache Invalidation on Transportation CRUD

**Location:** `TransportationService` — create/update/delete methods.

**Problem:** When admin modifies transportations, cached route results become stale. Agencies receive outdated data until TTL expires.

**Proposed fix:**
- Annotate `TransportationService.create`, `update`, `delete` with `@CacheEvict(value = "routes", allEntries = true)`.
- Same for `LocationService` mutations (location rename or removal affects routes).

**Acceptance:**
- After a transportation update, next route query returns fresh data.
- Integration test verifies cache eviction on CRUD.

---

### 2.5 N+1 Lazy Loading Risk

**Location:** `RouteFinder.java:26-27` — `flight.getOrigin().getId()`, `flight.getDestination().getId()`.

**Problem:** If `TransportationEntity` uses `FetchType.LAZY` for `origin`/`destination` relations, each access triggers a separate SELECT. This causes an N+1 query explosion when iterating over flights.

**Proposed fix:**
- Verify `TransportationEntity` fetch strategy.
- If lazy, replace `findAll()` with a fetch join query:
  ```java
  @Query("SELECT t FROM TransportationEntity t JOIN FETCH t.origin JOIN FETCH t.destination WHERE ...")
  ```
- Or use an entity graph: `@EntityGraph(attributePaths = {"origin", "destination"})`.

**Acceptance:**
- Single SQL query loads all transportations with their location relations.
- Verified via Hibernate SQL log or `spring.jpa.properties.hibernate.generate_statistics=true`.

---

## 3. Out of Scope

- Algorithm rewrite (BFS/Dijkstra) — current FLIGHT-centric join is correct and matches domain constraints (max 3 legs, exactly 1 FLIGHT).
- Pagination of route results — not requested in UC-03.
- Cross-day routing (overnight transfers) — current spec assumes single-day operation.

---

## 4. Implementation Order (Suggested)

1. **2.5 N+1 fix** — Foundation; impacts all subsequent work.
2. **2.1 DB-level day filter** — Largest I/O reduction.
3. **2.2 HashMap index** — Algorithmic optimization on filtered set.
4. **2.3 Cache layer** — UC-03 compliance.
5. **2.4 Cache invalidation** — Required correctness for #2.3.

---

## 5. Validation Checklist

- [x] Existing `RouteFinder` and `RouteService` unit/integration tests still pass (`RouteFinderTest` 7/7).
- [x] New tests cover cache hit/miss and cache eviction on CRUD (`RouteServiceCacheIT`, `RouteServiceN1IT`).
- [~] Performance benchmark — out of scope; deferred.
- [x] Hibernate statistics enabled in test profile; `RouteServiceN1IT` asserts no N+1.

> **Note (2026-04-27):** 2.1 (DB-level day filter) was dropped during apply. The project runs on **MySQL 8** with `operating_days` stored as `VARCHAR(20)` (CSV via `OperatingDaysConverter`); PostgreSQL array operators and GIN indexes are not applicable. `FIND_IN_SET`-based filtering would not be sargable, so the in-memory day filter inside `RouteFinder` was retained. The other improvements (2.2 HashMap index, 2.3 cache, 2.4 invalidation, 2.5 N+1 fix via `findAllWithLocations` + `@EntityGraph`) were applied as designed.

## 6. Cache Configuration

`routes` cache is backed by Caffeine. Configurable via:

| Property | Default | Description |
|---|---|---|
| `app.cache.routes.ttl` | `PT1H` | TTL after write (ISO-8601 duration) |
| `app.cache.routes.max-size` | `10000` | Maximum entries before eviction |

Eviction triggers (all entries invalidated):
- `TransportationService.create / update / delete`
- `LocationService.create / update / delete`
