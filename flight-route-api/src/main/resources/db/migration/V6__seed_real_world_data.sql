-- Real-world seed data for Flight Route API.
-- Data set covers 6 hub cities (Istanbul, London, New York, Paris, Dubai, Frankfurt)
-- with airports, city-center points, ground transfers and inter-airport flights.
-- Operating days follow ISO-8601 (1=Monday ... 7=Sunday).

-- ---------------------------------------------------------------------------
-- LOCATIONS
-- ---------------------------------------------------------------------------

INSERT INTO locations (name, country, city, code, created_at, updated_at, version, created_by, last_modified_by) VALUES
-- Istanbul
('Istanbul Airport',                  'Türkiye',        'Istanbul',  'IST',   CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), 0, 'system', 'system'),
('Sabiha Gökçen Airport',             'Türkiye',        'Istanbul',  'SAW',   CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), 0, 'system', 'system'),
('Taksim Square',                     'Türkiye',        'Istanbul',  'CCIST', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), 0, 'system', 'system'),
('Kadıköy Pier',                      'Türkiye',        'Istanbul',  'CCKAD', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), 0, 'system', 'system'),
-- London
('London Heathrow Airport',           'United Kingdom', 'London',    'LHR',   CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), 0, 'system', 'system'),
('London Gatwick Airport',            'United Kingdom', 'London',    'LGW',   CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), 0, 'system', 'system'),
('London Stansted Airport',           'United Kingdom', 'London',    'STN',   CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), 0, 'system', 'system'),
('Wembley Stadium',                   'United Kingdom', 'London',    'CCWEM', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), 0, 'system', 'system'),
('Westminster',                       'United Kingdom', 'London',    'CCWES', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), 0, 'system', 'system'),
-- New York
('John F. Kennedy International',     'United States',  'New York',  'JFK',   CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), 0, 'system', 'system'),
('LaGuardia Airport',                 'United States',  'New York',  'LGA',   CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), 0, 'system', 'system'),
('Newark Liberty International',      'United States',  'New York',  'EWR',   CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), 0, 'system', 'system'),
('Times Square',                      'United States',  'New York',  'CCTSQ', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), 0, 'system', 'system'),
-- Paris
('Charles de Gaulle Airport',         'France',         'Paris',     'CDG',   CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), 0, 'system', 'system'),
('Orly Airport',                      'France',         'Paris',     'ORY',   CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), 0, 'system', 'system'),
('Eiffel Tower',                      'France',         'Paris',     'CCEIF', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), 0, 'system', 'system'),
-- Dubai
('Dubai International Airport',       'UAE',            'Dubai',     'DXB',   CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), 0, 'system', 'system'),
('Burj Khalifa',                      'UAE',            'Dubai',     'CCBRJ', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), 0, 'system', 'system'),
-- Frankfurt
('Frankfurt am Main Airport',         'Germany',        'Frankfurt', 'FRA',   CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), 0, 'system', 'system'),
('Römerberg',                         'Germany',        'Frankfurt', 'CCFRA', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), 0, 'system', 'system');

-- ---------------------------------------------------------------------------
-- TRANSPORTATIONS
-- Use sub-selects on location.code so the script does not depend on auto-increment ids.
-- operating_days are stored as comma-separated ISO weekday numbers (1=Mon ... 7=Sun).
-- ---------------------------------------------------------------------------

INSERT INTO transportations (origin_id, destination_id, type, operating_days, created_at, updated_at, version, created_by, last_modified_by)
SELECT o.id, d.id, t.type, t.operating_days, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), 0, 'system', 'system'
FROM locations o
JOIN locations d
JOIN (
    -- ====================== ISTANBUL GROUND TRANSFERS ======================
    SELECT 'CCIST' AS o_code, 'IST'   AS d_code, 'BUS'    AS type, '1,2,3,4,5,6,7' AS operating_days UNION ALL
    SELECT 'CCIST',           'IST',           'SUBWAY',           '1,2,3,4,5,6,7'                  UNION ALL
    SELECT 'CCIST',           'IST',           'UBER',             '1,2,3,4,5,6,7'                  UNION ALL
    SELECT 'CCIST',           'SAW',           'BUS',              '1,2,3,4,5,6,7'                  UNION ALL
    SELECT 'CCIST',           'SAW',           'UBER',             '1,2,3,4,5,6,7'                  UNION ALL
    SELECT 'CCKAD',           'SAW',           'BUS',              '1,2,3,4,5'                      UNION ALL
    SELECT 'CCKAD',           'SAW',           'SUBWAY',           '1,2,3,4,5,6,7'                  UNION ALL
    SELECT 'CCKAD',           'IST',           'UBER',             '1,2,3,4,5,6,7'                  UNION ALL
    SELECT 'IST',             'CCIST',         'BUS',              '1,2,3,4,5,6,7'                  UNION ALL
    SELECT 'IST',             'CCIST',         'SUBWAY',           '1,2,3,4,5,6,7'                  UNION ALL
    SELECT 'IST',             'CCIST',         'UBER',             '1,2,3,4,5,6,7'                  UNION ALL
    SELECT 'SAW',             'CCKAD',         'SUBWAY',           '1,2,3,4,5,6,7'                  UNION ALL
    SELECT 'SAW',             'CCIST',         'BUS',              '1,2,3,4,5,6,7'                  UNION ALL

    -- ====================== LONDON GROUND TRANSFERS ========================
    SELECT 'CCWES',           'LHR',           'SUBWAY',           '1,2,3,4,5,6,7'                  UNION ALL
    SELECT 'CCWES',           'LHR',           'UBER',             '1,2,3,4,5,6,7'                  UNION ALL
    SELECT 'CCWES',           'LGW',           'BUS',              '1,2,3,4,5,6,7'                  UNION ALL
    SELECT 'CCWES',           'STN',           'BUS',              '1,2,3,4,5,6,7'                  UNION ALL
    SELECT 'LHR',             'CCWEM',         'BUS',              '1,2,3,4,5,6,7'                  UNION ALL
    SELECT 'LHR',             'CCWEM',         'UBER',             '1,2,3,4,5,6,7'                  UNION ALL
    SELECT 'LHR',             'CCWES',         'SUBWAY',           '1,2,3,4,5,6,7'                  UNION ALL
    SELECT 'LGW',             'CCWEM',         'UBER',             '1,2,3,4,5,6,7'                  UNION ALL
    SELECT 'LGW',             'CCWES',         'BUS',              '1,2,3,4,5,6,7'                  UNION ALL
    SELECT 'STN',             'CCWES',         'BUS',              '1,2,3,4,5,6,7'                  UNION ALL

    -- ====================== NEW YORK GROUND TRANSFERS ======================
    SELECT 'CCTSQ',           'JFK',           'SUBWAY',           '1,2,3,4,5,6,7'                  UNION ALL
    SELECT 'CCTSQ',           'JFK',           'UBER',             '1,2,3,4,5,6,7'                  UNION ALL
    SELECT 'CCTSQ',           'LGA',           'BUS',              '1,2,3,4,5,6,7'                  UNION ALL
    SELECT 'CCTSQ',           'LGA',           'UBER',             '1,2,3,4,5,6,7'                  UNION ALL
    SELECT 'CCTSQ',           'EWR',           'BUS',              '1,2,3,4,5'                      UNION ALL
    SELECT 'JFK',             'CCTSQ',         'SUBWAY',           '1,2,3,4,5,6,7'                  UNION ALL
    SELECT 'JFK',             'CCTSQ',         'UBER',             '1,2,3,4,5,6,7'                  UNION ALL
    SELECT 'LGA',             'CCTSQ',         'BUS',              '1,2,3,4,5,6,7'                  UNION ALL
    SELECT 'EWR',             'CCTSQ',         'UBER',             '1,2,3,4,5,6,7'                  UNION ALL

    -- ====================== PARIS GROUND TRANSFERS =========================
    SELECT 'CCEIF',           'CDG',           'SUBWAY',           '1,2,3,4,5,6,7'                  UNION ALL
    SELECT 'CCEIF',           'CDG',           'UBER',             '1,2,3,4,5,6,7'                  UNION ALL
    SELECT 'CCEIF',           'ORY',           'BUS',              '1,2,3,4,5,6,7'                  UNION ALL
    SELECT 'CDG',             'CCEIF',         'SUBWAY',           '1,2,3,4,5,6,7'                  UNION ALL
    SELECT 'CDG',             'CCEIF',         'UBER',             '1,2,3,4,5,6,7'                  UNION ALL
    SELECT 'ORY',             'CCEIF',         'BUS',              '1,2,3,4,5,6,7'                  UNION ALL

    -- ====================== DUBAI GROUND TRANSFERS =========================
    SELECT 'CCBRJ',           'DXB',           'SUBWAY',           '1,2,3,4,5,6,7'                  UNION ALL
    SELECT 'CCBRJ',           'DXB',           'UBER',             '1,2,3,4,5,6,7'                  UNION ALL
    SELECT 'DXB',             'CCBRJ',         'SUBWAY',           '1,2,3,4,5,6,7'                  UNION ALL
    SELECT 'DXB',             'CCBRJ',         'UBER',             '1,2,3,4,5,6,7'                  UNION ALL

    -- ====================== FRANKFURT GROUND TRANSFERS =====================
    SELECT 'CCFRA',           'FRA',           'SUBWAY',           '1,2,3,4,5,6,7'                  UNION ALL
    SELECT 'CCFRA',           'FRA',           'BUS',              '1,2,3,4,5,6,7'                  UNION ALL
    SELECT 'FRA',             'CCFRA',         'SUBWAY',           '1,2,3,4,5,6,7'                  UNION ALL
    SELECT 'FRA',             'CCFRA',         'BUS',              '1,2,3,4,5,6,7'                  UNION ALL

    -- ============================ FLIGHTS ==================================
    -- Istanbul <-> London
    SELECT 'IST',             'LHR',           'FLIGHT',           '1,2,3,4,5,6,7'                  UNION ALL
    SELECT 'LHR',             'IST',           'FLIGHT',           '1,2,3,4,5,6,7'                  UNION ALL
    SELECT 'IST',             'LGW',           'FLIGHT',           '1,3,5,7'                        UNION ALL
    SELECT 'LGW',             'IST',           'FLIGHT',           '1,3,5,7'                        UNION ALL
    SELECT 'SAW',             'STN',           'FLIGHT',           '2,4,6'                          UNION ALL
    SELECT 'STN',             'SAW',           'FLIGHT',           '2,4,6'                          UNION ALL
    -- Istanbul <-> Paris
    SELECT 'IST',             'CDG',           'FLIGHT',           '1,2,3,4,5,6,7'                  UNION ALL
    SELECT 'CDG',             'IST',           'FLIGHT',           '1,2,3,4,5,6,7'                  UNION ALL
    SELECT 'SAW',             'ORY',           'FLIGHT',           '1,4,6'                          UNION ALL
    SELECT 'ORY',             'SAW',           'FLIGHT',           '1,4,6'                          UNION ALL
    -- Istanbul <-> Dubai
    SELECT 'IST',             'DXB',           'FLIGHT',           '1,2,3,4,5,6,7'                  UNION ALL
    SELECT 'DXB',             'IST',           'FLIGHT',           '1,2,3,4,5,6,7'                  UNION ALL
    -- Istanbul <-> Frankfurt
    SELECT 'IST',             'FRA',           'FLIGHT',           '1,2,3,4,5,6,7'                  UNION ALL
    SELECT 'FRA',             'IST',           'FLIGHT',           '1,2,3,4,5,6,7'                  UNION ALL
    -- London <-> New York
    SELECT 'LHR',             'JFK',           'FLIGHT',           '1,2,3,4,5,6,7'                  UNION ALL
    SELECT 'JFK',             'LHR',           'FLIGHT',           '1,2,3,4,5,6,7'                  UNION ALL
    SELECT 'LGW',             'EWR',           'FLIGHT',           '1,3,5,7'                        UNION ALL
    SELECT 'EWR',             'LGW',           'FLIGHT',           '1,3,5,7'                        UNION ALL
    -- Paris <-> New York
    SELECT 'CDG',             'JFK',           'FLIGHT',           '1,2,3,4,5,6,7'                  UNION ALL
    SELECT 'JFK',             'CDG',           'FLIGHT',           '1,2,3,4,5,6,7'                  UNION ALL
    -- Frankfurt <-> New York
    SELECT 'FRA',             'JFK',           'FLIGHT',           '2,4,6'                          UNION ALL
    SELECT 'JFK',             'FRA',           'FLIGHT',           '2,4,6'                          UNION ALL
    -- Dubai <-> London / Paris / Frankfurt
    SELECT 'DXB',             'LHR',           'FLIGHT',           '1,2,3,4,5,6,7'                  UNION ALL
    SELECT 'LHR',             'DXB',           'FLIGHT',           '1,2,3,4,5,6,7'                  UNION ALL
    SELECT 'DXB',             'CDG',           'FLIGHT',           '1,3,5,7'                        UNION ALL
    SELECT 'CDG',             'DXB',           'FLIGHT',           '1,3,5,7'                        UNION ALL
    SELECT 'DXB',             'FRA',           'FLIGHT',           '2,4,6'                          UNION ALL
    SELECT 'FRA',             'DXB',           'FLIGHT',           '2,4,6'
) AS t
ON o.code = t.o_code AND d.code = t.d_code;
