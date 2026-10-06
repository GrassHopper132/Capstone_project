package org.peopleshores.capstone_project.entity;

/** Lifecycle state of an artifact. Mirrors ck_artifacts_status in schema.sql. */
public enum ArtifactStatus {
    STORED,
    ON_DISPLAY,
    IN_RESTORATION,
    DEACCESSIONED
}
