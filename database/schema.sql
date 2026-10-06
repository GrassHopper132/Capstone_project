-- =====================================================================
-- Museum Artifact Manager - schema
-- MySQL 8.0
-- Run:  mysql -u root -p < database/schema.sql
-- =====================================================================

DROP DATABASE IF EXISTS museum_db;
CREATE DATABASE museum_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
USE museum_db;

-- ---------------------------------------------------------------------
-- roles
-- ---------------------------------------------------------------------
CREATE TABLE roles (
                       id          BIGINT       NOT NULL AUTO_INCREMENT,
                       name        VARCHAR(20)  NOT NULL,
                       created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                       CONSTRAINT pk_roles           PRIMARY KEY (id),
                       CONSTRAINT uq_roles_name      UNIQUE (name),
                       CONSTRAINT ck_roles_name      CHECK (name IN ('ADMIN', 'CURATOR', 'RESTORER'))
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- users
-- ---------------------------------------------------------------------
CREATE TABLE users (
                       id             BIGINT       NOT NULL AUTO_INCREMENT,
                       email          VARCHAR(120) NOT NULL,
                       password_hash  VARCHAR(72)  NOT NULL,
                       full_name      VARCHAR(120) NOT NULL,
                       role_id        BIGINT       NOT NULL,
                       active         BOOLEAN      NOT NULL DEFAULT TRUE,
                       created_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                       CONSTRAINT pk_users        PRIMARY KEY (id),
                       CONSTRAINT uq_users_email  UNIQUE (email),
                       CONSTRAINT fk_users_role   FOREIGN KEY (role_id) REFERENCES roles (id)
) ENGINE = InnoDB;

CREATE INDEX ix_users_role   ON users (role_id);
CREATE INDEX ix_users_active ON users (active);

-- ---------------------------------------------------------------------
-- collections
-- ---------------------------------------------------------------------
CREATE TABLE collections (
                             id          BIGINT       NOT NULL AUTO_INCREMENT,
                             name        VARCHAR(120) NOT NULL,
                             description VARCHAR(500),
                             curator_id  BIGINT,
                             created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                             CONSTRAINT pk_collections          PRIMARY KEY (id),
                             CONSTRAINT uq_collections_name     UNIQUE (name),
                             CONSTRAINT fk_collections_curator  FOREIGN KEY (curator_id) REFERENCES users (id)
                                 ON DELETE SET NULL
) ENGINE = InnoDB;

CREATE INDEX ix_collections_curator ON collections (curator_id);

-- ---------------------------------------------------------------------
-- locations
-- ---------------------------------------------------------------------
CREATE TABLE locations (
                           id                  BIGINT      NOT NULL AUTO_INCREMENT,
                           building            VARCHAR(60) NOT NULL,
                           room                VARCHAR(60) NOT NULL,
                           case_code           VARCHAR(30) NOT NULL,
                           climate_controlled  BOOLEAN     NOT NULL DEFAULT FALSE,
                           created_at          TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           updated_at          TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                           CONSTRAINT pk_locations      PRIMARY KEY (id),
                           CONSTRAINT uq_locations_spot UNIQUE (building, room, case_code)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- artifacts
-- ---------------------------------------------------------------------
CREATE TABLE artifacts (
                           id                BIGINT       NOT NULL AUTO_INCREMENT,
                           accession_number  VARCHAR(40)  NOT NULL,
                           title             VARCHAR(200) NOT NULL,
                           origin_culture    VARCHAR(120),
                           date_period       VARCHAR(80),
                           material          VARCHAR(80),
                           description       VARCHAR(1000),
                           collection_id     BIGINT       NOT NULL,
                           location_id       BIGINT       NOT NULL,
                           status            VARCHAR(20)  NOT NULL DEFAULT 'STORED',
                           acquired_on       DATE,
                           created_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           updated_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                           CONSTRAINT pk_artifacts             PRIMARY KEY (id),
                           CONSTRAINT uq_artifacts_accession   UNIQUE (accession_number),
                           CONSTRAINT fk_artifacts_collection  FOREIGN KEY (collection_id) REFERENCES collections (id),
                           CONSTRAINT fk_artifacts_location    FOREIGN KEY (location_id)   REFERENCES locations (id),
                           CONSTRAINT ck_artifacts_status      CHECK (status IN ('STORED', 'ON_DISPLAY', 'IN_RESTORATION', 'DEACCESSIONED'))
) ENGINE = InnoDB;

CREATE INDEX ix_artifacts_status     ON artifacts (status);
CREATE INDEX ix_artifacts_collection ON artifacts (collection_id);
CREATE INDEX ix_artifacts_location   ON artifacts (location_id);
CREATE INDEX ix_artifacts_material   ON artifacts (material);

-- ---------------------------------------------------------------------
-- condition_reports  (append-only inspection history)
-- ---------------------------------------------------------------------
CREATE TABLE condition_reports (
                                   id            BIGINT       NOT NULL AUTO_INCREMENT,
                                   artifact_id   BIGINT       NOT NULL,
                                   inspector_id  BIGINT       NOT NULL,
                                   grade         VARCHAR(20)  NOT NULL,
                                   notes         VARCHAR(1000),
                                   inspected_on  DATE         NOT NULL,
                                   created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                   CONSTRAINT pk_condition_reports            PRIMARY KEY (id),
                                   CONSTRAINT fk_condition_reports_artifact   FOREIGN KEY (artifact_id)  REFERENCES artifacts (id)
                                       ON DELETE CASCADE,
                                   CONSTRAINT fk_condition_reports_inspector  FOREIGN KEY (inspector_id) REFERENCES users (id),
                                   CONSTRAINT ck_condition_reports_grade      CHECK (grade IN ('EXCELLENT', 'GOOD', 'FAIR', 'POOR', 'CRITICAL'))
) ENGINE = InnoDB;

CREATE INDEX ix_condition_reports_artifact ON condition_reports (artifact_id, inspected_on DESC);

-- ---------------------------------------------------------------------
-- restoration_jobs
-- ---------------------------------------------------------------------
CREATE TABLE restoration_jobs (
                                  id           BIGINT       NOT NULL AUTO_INCREMENT,
                                  artifact_id  BIGINT       NOT NULL,
                                  restorer_id  BIGINT       NOT NULL,
                                  status       VARCHAR(20)  NOT NULL DEFAULT 'OPEN',
                                  opened_on    DATE         NOT NULL,
                                  closed_on    DATE,
                                  summary      VARCHAR(1000),
                                  created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                  updated_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                  CONSTRAINT pk_restoration_jobs           PRIMARY KEY (id),
                                  CONSTRAINT fk_restoration_jobs_artifact  FOREIGN KEY (artifact_id) REFERENCES artifacts (id)
                                      ON DELETE CASCADE,
                                  CONSTRAINT fk_restoration_jobs_restorer  FOREIGN KEY (restorer_id) REFERENCES users (id),
                                  CONSTRAINT ck_restoration_jobs_status    CHECK (status IN ('OPEN', 'IN_PROGRESS', 'CLOSED')),
                                  CONSTRAINT ck_restoration_jobs_dates     CHECK (closed_on IS NULL OR closed_on >= opened_on)
) ENGINE = InnoDB;

CREATE INDEX ix_restoration_jobs_status   ON restoration_jobs (status);
CREATE INDEX ix_restoration_jobs_artifact ON restoration_jobs (artifact_id);

-- ---------------------------------------------------------------------
-- exhibitions
-- ---------------------------------------------------------------------
CREATE TABLE exhibitions (
                             id          BIGINT       NOT NULL AUTO_INCREMENT,
                             title       VARCHAR(200) NOT NULL,
                             gallery     VARCHAR(120),
                             start_date  DATE         NOT NULL,
                             end_date    DATE         NOT NULL,
                             created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                             CONSTRAINT pk_exhibitions        PRIMARY KEY (id),
                             CONSTRAINT ck_exhibitions_dates  CHECK (end_date > start_date)
) ENGINE = InnoDB;

CREATE INDEX ix_exhibitions_dates ON exhibitions (start_date, end_date);

-- ---------------------------------------------------------------------
-- exhibition_artifacts  (many-to-many join with payload)
-- ---------------------------------------------------------------------
CREATE TABLE exhibition_artifacts (
                                      exhibition_id  BIGINT   NOT NULL,
                                      artifact_id    BIGINT   NOT NULL,
                                      display_order  INT      NOT NULL,
                                      created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                      CONSTRAINT pk_exhibition_artifacts             PRIMARY KEY (exhibition_id, artifact_id),
                                      CONSTRAINT fk_exhibition_artifacts_exhibition  FOREIGN KEY (exhibition_id) REFERENCES exhibitions (id)
                                          ON DELETE CASCADE,
                                      CONSTRAINT fk_exhibition_artifacts_artifact    FOREIGN KEY (artifact_id)   REFERENCES artifacts (id),
                                      CONSTRAINT ck_exhibition_artifacts_order       CHECK (display_order > 0)
) ENGINE = InnoDB;

CREATE INDEX ix_exhibition_artifacts_artifact ON exhibition_artifacts (artifact_id);
-- ---------------------------------------------------------------------------
-- Performance indexes.
-- Foreign keys and UNIQUE constraints are indexed by InnoDB automatically, so
-- only non-key filter columns are listed here.
-- ---------------------------------------------------------------------------
CREATE INDEX idx_artifacts_status           ON artifacts (status);
CREATE INDEX idx_artifacts_collection_status ON artifacts (collection_id, status);
CREATE INDEX idx_restoration_jobs_status    ON restoration_jobs (status);
CREATE INDEX idx_exhibitions_date_range     ON exhibitions (start_date, end_date);
