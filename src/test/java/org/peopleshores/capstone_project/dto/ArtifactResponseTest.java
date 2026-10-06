package org.peopleshores.capstone_project.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.peopleshores.capstone_project.entity.Artifact;
import org.peopleshores.capstone_project.entity.ArtifactStatus;
import org.peopleshores.capstone_project.entity.Location;
import org.peopleshores.capstone_project.entity.MuseumCollection;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * The response DTO is the contract the frontend reads, so every field it
 * promises is checked here, including the two that flatten a relationship
 * (collectionName, locationLabel) and the two that must survive a null
 * relationship without throwing.
 */
@DisplayName("ArtifactResponse")
class ArtifactResponseTest {

    private Artifact fullArtifact() {
        MuseumCollection collection = mock(MuseumCollection.class);
        when(collection.getId()).thenReturn(3L);
        when(collection.getName()).thenReturn("Mediterranean Antiquities");

        Location location = mock(Location.class);
        when(location.getId()).thenReturn(12L);
        when(location.getLabel()).thenReturn("Store 2, Shelf B");

        Artifact a = new Artifact();
        a.setAccessionNumber("1994.22.7");
        a.setTitle("Red-figure amphora");
        a.setOriginCulture("Attic Greek");
        a.setDatePeriod("c. 480 BCE");
        a.setMaterial("Terracotta");
        a.setDescription("Storage jar with figural decoration.");
        a.setStatus(ArtifactStatus.ON_DISPLAY);
        a.setAcquiredOn(LocalDate.of(1994, 6, 14));
        a.setCollection(collection);
        a.setLocation(location);
        a.setImageUrl("https://images.example.org/amphora.jpg");
        return a;
    }

    @Test
    @DisplayName("carries every catalogue field through to the client")
    void mapsAllFields() {
        ArtifactResponse r = ArtifactResponse.from(fullArtifact());

        assertThat(r.accessionNumber()).isEqualTo("1994.22.7");
        assertThat(r.title()).isEqualTo("Red-figure amphora");
        assertThat(r.originCulture()).isEqualTo("Attic Greek");
        assertThat(r.datePeriod()).isEqualTo("c. 480 BCE");
        assertThat(r.material()).isEqualTo("Terracotta");
        assertThat(r.description()).isEqualTo("Storage jar with figural decoration.");
        assertThat(r.acquiredOn()).isEqualTo(LocalDate.of(1994, 6, 14));
        assertThat(r.imageUrl()).isEqualTo("https://images.example.org/amphora.jpg");
    }

    @Test
    @DisplayName("flattens the collection and location into names the UI can print")
    void flattensRelationships() {
        ArtifactResponse r = ArtifactResponse.from(fullArtifact());

        assertThat(r.collectionId()).isEqualTo(3L);
        assertThat(r.collectionName()).isEqualTo("Mediterranean Antiquities");
        assertThat(r.locationId()).isEqualTo(12L);
        assertThat(r.locationLabel()).isEqualTo("Store 2, Shelf B");
    }

    @Test
    @DisplayName("renders the status as a plain string")
    void rendersStatusAsString() {
        assertThat(ArtifactResponse.from(fullArtifact()).status()).isEqualTo("ON_DISPLAY");
    }

    @Test
    @DisplayName("survives an artifact with no status, collection or location")
    void toleratesMissingRelationships() {
        Artifact bare = new Artifact();
        bare.setAccessionNumber("2026.1.1");
        bare.setTitle("Unprocessed accession");

        ArtifactResponse r = ArtifactResponse.from(bare);

        // A new object is STORED before anything saves it: the entity sets that
        // default itself, matching the column default in schema.sql.
        assertThat(r.status()).isEqualTo("STORED");
        assertThat(r.collectionId()).isNull();
        assertThat(r.collectionName()).isNull();
        assertThat(r.locationId()).isNull();
        assertThat(r.locationLabel()).isNull();
    }
}