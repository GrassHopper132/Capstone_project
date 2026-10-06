-- =====================================================================
-- Museum Artifact Manager - seed data for local development and demo
-- Run AFTER schema.sql:  mysql -u root -p < database/seed.sql
--
-- DEMO CREDENTIALS - all four accounts use the password:  Password123!
-- The hash below is BCrypt strength 10. Local development only.
-- =====================================================================

USE museum_db;

SET @pw = '$2b$10$vrsOnwWv81KAfZ281YLkJugAiKTjhwjlhroCAOp8Dbr8F41R2/nr.';

-- ---------------------------------------------------------------------
-- roles
-- ---------------------------------------------------------------------
INSERT INTO roles (id, name) VALUES
                                 (1, 'ADMIN'),
                                 (2, 'CURATOR'),
                                 (3, 'RESTORER');

-- ---------------------------------------------------------------------
-- users
-- ---------------------------------------------------------------------
INSERT INTO users (id, email, password_hash, full_name, role_id, active) VALUES
                                                                             (1, 'admin@museum.org',    @pw, 'Dana Whitfield',  1, TRUE),
                                                                             (2, 'curator@museum.org',  @pw, 'Marisol Ferrer',  2, TRUE),
                                                                             (3, 'curator2@museum.org', @pw, 'Ngozi Adeyemi',   2, TRUE),
                                                                             (4, 'restorer@museum.org', @pw, 'Tomas Lindqvist', 3, TRUE),
                                                                             (5, 'former@museum.org',   @pw, 'Ruth Calloway',   2, FALSE);

-- ---------------------------------------------------------------------
-- collections
-- ---------------------------------------------------------------------
INSERT INTO collections (id, name, description, curator_id) VALUES
                                                                (1, 'Mediterranean Antiquities', 'Greek, Roman, and Etruscan material, 8th c. BCE to 4th c. CE.', 2),
                                                                (2, 'Textiles and Dress',        'Woven and embroidered works, 15th c. to present.',              3),
                                                                (3, 'Works on Paper',            'Prints, drawings, maps, and photographs.',                      2);

-- ---------------------------------------------------------------------
-- locations
-- ---------------------------------------------------------------------
INSERT INTO locations (id, building, room, case_code, climate_controlled) VALUES
                                                                              (1, 'Main',      'Gallery 1',  'G1-A',    FALSE),
                                                                              (2, 'Main',      'Gallery 1',  'G1-B',    FALSE),
                                                                              (3, 'Main',      'Gallery 4',  'G4-A',    TRUE),
                                                                              (4, 'Annex',     'Storage 2',  'S2-R3',   TRUE),
                                                                              (5, 'Annex',     'Storage 2',  'S2-R4',   TRUE),
                                                                              (6, 'Annex',     'Lab',        'LAB-01',  TRUE);

-- ---------------------------------------------------------------------
-- artifacts
-- ---------------------------------------------------------------------
INSERT INTO artifacts
(id, accession_number, title, origin_culture, date_period, material, description, collection_id, location_id, status, acquired_on) VALUES
                                                                                                                                       (1,  '1994.22.7',  'Red-figure amphora',        'Attic Greek',     'c. 480 BCE',  'Terracotta', 'Storage jar with a scene of two runners.',              1, 1, 'ON_DISPLAY',     '1994-06-14'),
                                                                                                                                       (2,  '1994.22.8',  'Black-glaze kylix',         'Attic Greek',     'c. 450 BCE',  'Terracotta', 'Shallow drinking cup, intact foot.',                    1, 1, 'ON_DISPLAY',     '1994-06-14'),
                                                                                                                                       (3,  '2001.10.1',  'Bronze strigil',            'Roman',           '1st c. CE',   'Bronze',     'Curved scraper used at the baths.',                     1, 4, 'STORED',         '2001-03-02'),
                                                                                                                                       (4,  '2001.10.2',  'Terracotta oil lamp',       'Roman',           '2nd c. CE',   'Terracotta', 'Discus lamp with a rosette motif.',                     1, 4, 'STORED',         '2001-03-02'),
                                                                                                                                       (5,  '1987.4.19',  'Etruscan bucchero jug',     'Etruscan',        'c. 600 BCE',  'Ceramic',    'Burnished black ware with incised banding.',            1, 6, 'IN_RESTORATION', '1987-11-30'),
                                                                                                                                       (6,  '1976.31.2',  'Silk brocade panel',        'Ottoman',         '17th c.',     'Silk',       'Metal-wrapped thread on crimson ground.',               2, 3, 'ON_DISPLAY',     '1976-08-21'),
                                                                                                                                       (7,  '1976.31.3',  'Embroidered linen sampler', 'English',         '1789',        'Linen',      'Alphabet sampler signed by a child maker.',             2, 5, 'STORED',         '1976-08-21'),
                                                                                                                                       (8,  '2010.2.11',  'Indigo resist wrapper',     'Yoruba',          '20th c.',     'Cotton',     'Adire eleko, hand-painted cassava resist.',             2, 5, 'STORED',         '2010-01-19'),
                                                                                                                                       (9,  '2010.2.12',  'Wool tapestry fragment',    'Flemish',         'c. 1520',     'Wool',       'Border fragment with millefleurs ground.',              2, 6, 'IN_RESTORATION', '2010-01-19'),
                                                                                                                                       (10, '1968.9.44',  'Etching, harbor at dusk',   'Dutch',           '1642',        'Paper',      'Second state, trimmed to the platemark.',               3, 5, 'STORED',         '1968-04-08'),
                                                                                                                                       (11, '1968.9.45',  'Engraved city map',         'Italian',         '1573',        'Paper',      'Hand-colored plan with a cartouche.',                   3, 5, 'STORED',         '1968-04-08'),
                                                                                                                                       (12, '1999.17.3',  'Albumen print, quarry',     'American',        '1871',        'Paper',      'Mounted survey photograph.',                            3, 3, 'ON_DISPLAY',     '1999-09-27'),
                                                                                                                                       (13, '1999.17.4',  'Graphite study of hands',   'French',          'c. 1880',     'Paper',      'Preparatory sketch, verso blank.',                      3, 5, 'STORED',         '1999-09-27'),
                                                                                                                                       (14, '2018.6.1',   'Lithograph, poster study',  'French',          '1893',        'Paper',      'Trial proof with margin notes.',                        3, 5, 'STORED',         '2018-02-14'),
                                                                                                                                       (15, '1955.1.1',   'Marble votive fragment',    'Cypriot',         '5th c. BCE',  'Marble',     'Upper torso only, surface weathering.',                 1, 2, 'DEACCESSIONED',  '1955-01-05');

-- ---------------------------------------------------------------------
-- condition_reports
-- ---------------------------------------------------------------------
INSERT INTO condition_reports (artifact_id, inspector_id, grade, notes, inspected_on) VALUES
                                                                                          (1,  4, 'GOOD',      'Stable. Minor abrasion on the foot ring.',                 '2025-03-11'),
                                                                                          (1,  4, 'GOOD',      'No change since last cycle.',                              '2026-03-09'),
                                                                                          (2,  4, 'EXCELLENT', 'Glaze intact, no losses observed.',                        '2026-03-09'),
                                                                                          (5,  4, 'POOR',      'Two body cracks, previous adhesive failing.',              '2026-01-22'),
                                                                                          (5,  4, 'FAIR',      'Old repair removed, cracks consolidated.',                 '2026-08-14'),
                                                                                          (6,  4, 'FAIR',      'Light fading along the upper selvage.',                    '2026-02-03'),
                                                                                          (9,  4, 'CRITICAL',  'Active fiber loss at the lower edge, handling suspended.',  '2026-07-30'),
                                                                                          (10, 4, 'GOOD',      'Slight foxing in the lower margin.',                       '2025-11-18'),
                                                                                          (12, 4, 'FAIR',      'Mount acidic, recommend rehousing.',                       '2026-05-06'),
                                                                                          (15, 4, 'POOR',      'Surface loss consistent with outdoor storage pre-1955.',   '2024-09-12');

-- ---------------------------------------------------------------------
-- restoration_jobs
-- ---------------------------------------------------------------------
INSERT INTO restoration_jobs (artifact_id, restorer_id, status, opened_on, closed_on, summary) VALUES
                                                                                                   (5,  4, 'IN_PROGRESS', '2026-01-25', NULL,         'Remove failing adhesive, consolidate two body cracks.'),
                                                                                                   (9,  4, 'OPEN',        '2026-08-02', NULL,         'Stabilize lower edge before any further handling.'),
                                                                                                   (12, 4, 'OPEN',        '2026-05-10', NULL,         'Rehouse in archival mount.'),
                                                                                                   (1,  4, 'CLOSED',      '2025-03-15', '2025-04-02', 'Surface cleaned, foot ring abrasion documented only.'),
                                                                                                   (10, 4, 'CLOSED',      '2025-11-20', '2025-12-15', 'Foxing reduced, print rehoused.');

-- ---------------------------------------------------------------------
-- exhibitions
-- ---------------------------------------------------------------------
INSERT INTO exhibitions (id, title, gallery, start_date, end_date) VALUES
                                                                       (1, 'Clay and Bronze: Daily Life in Antiquity', 'Gallery 1', '2026-09-01', '2027-01-31'),
                                                                       (2, 'Thread and Dye',                           'Gallery 4', '2026-10-15', '2027-02-28'),
                                                                       (3, 'Lines on Paper',                           'Gallery 2', '2027-03-15', '2027-07-01');

-- ---------------------------------------------------------------------
-- exhibition_artifacts
-- ---------------------------------------------------------------------
INSERT INTO exhibition_artifacts (exhibition_id, artifact_id, display_order) VALUES
                                                                                 (1, 1,  1),
                                                                                 (1, 2,  2),
                                                                                 (1, 3,  3),
                                                                                 (1, 4,  4),
                                                                                 (2, 6,  1),
                                                                                 (2, 8,  2),
                                                                                 (3, 10, 1),
                                                                                 (3, 11, 2),
                                                                                 (3, 12, 3),
                                                                                 (3, 14, 4);
-- Public visitors. Self-registration creates this role.
INSERT IGNORE INTO roles (name) VALUES ('VISITOR');
