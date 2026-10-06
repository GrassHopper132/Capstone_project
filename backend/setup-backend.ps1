# Museum Artifact Manager - backend scaffold
# Run from your repository root:  .\setup-backend.ps1

$ErrorActionPreference = 'Stop'

New-Item -ItemType Directory -Force -Path 'backend' | Out-Null
New-Item -ItemType Directory -Force -Path 'backend\src\main\java\com\museum\artifacts' | Out-Null
New-Item -ItemType Directory -Force -Path 'backend\src\main\java\com\museum\artifacts\domain' | Out-Null
New-Item -ItemType Directory -Force -Path 'backend\src\main\java\com\museum\artifacts\repository' | Out-Null
New-Item -ItemType Directory -Force -Path 'backend\src\main\resources' | Out-Null

$fileContent = @'
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.3.4</version>
        <relativePath/>
    </parent>

    <groupId>com.museum</groupId>
    <artifactId>artifact-manager</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>Museum Artifact Manager</name>
    <description>Collection management system for a mid-size museum</description>

    <properties>
        <java.version>17</java.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>

        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>

        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <!-- JWT: used from Phase 4 onward -->
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
            <version>0.12.6</version>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <version>0.12.6</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <version>0.12.6</version>
            <scope>runtime</scope>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>

            <!-- Coverage report: target/site/jacoco/index.html -->
            <plugin>
                <groupId>org.jacoco</groupId>
                <artifactId>jacoco-maven-plugin</artifactId>
                <version>0.8.12</version>
                <executions>
                    <execution>
                        <goals><goal>prepare-agent</goal></goals>
                    </execution>
                    <execution>
                        <id>report</id>
                        <phase>verify</phase>
                        <goals><goal>report</goal></goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
'@
Set-Content -Path 'backend\pom.xml' -Value $fileContent -Encoding UTF8
Write-Host 'wrote backend\pom.xml'

$fileContent = @'
package com.museum.artifacts;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ArtifactManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ArtifactManagerApplication.class, args);
    }
}
'@
Set-Content -Path 'backend\src\main\java\com\museum\artifacts\ArtifactManagerApplication.java' -Value $fileContent -Encoding UTF8
Write-Host 'wrote backend\src\main\java\com\museum\artifacts\ArtifactManagerApplication.java'

$fileContent = @'
package com.museum.artifacts.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "artifacts")
@Getter
@Setter
@NoArgsConstructor
public class Artifact extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Immutable once assigned. Enforced in the service layer, not here. */
    @NotBlank
    @Size(max = 40)
    @Column(name = "accession_number", nullable = false, unique = true, updatable = false, length = 40)
    private String accessionNumber;

    @NotBlank
    @Size(max = 200)
    @Column(nullable = false, length = 200)
    private String title;

    @Size(max = 120)
    @Column(name = "origin_culture", length = 120)
    private String originCulture;

    @Size(max = 80)
    @Column(name = "date_period", length = 80)
    private String datePeriod;

    @Size(max = 80)
    @Column(length = 80)
    private String material;

    @Size(max = 1000)
    @Column(length = 1000)
    private String description;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "collection_id", nullable = false)
    private MuseumCollection collection;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ArtifactStatus status = ArtifactStatus.STORED;

    @PastOrPresent
    @Column(name = "acquired_on")
    private LocalDate acquiredOn;

    @OneToMany(mappedBy = "artifact", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("inspectedOn DESC")
    private List<ConditionReport> conditionReports = new ArrayList<>();

    @OneToMany(mappedBy = "artifact", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<RestorationJob> restorationJobs = new ArrayList<>();

    @OneToMany(mappedBy = "artifact", fetch = FetchType.LAZY)
    private List<ExhibitionArtifact> exhibitionEntries = new ArrayList<>();

    /** Keeps both sides of the association in step. */
    public void addConditionReport(ConditionReport report) {
        conditionReports.add(report);
        report.setArtifact(this);
    }

    public void addRestorationJob(RestorationJob job) {
        restorationJobs.add(job);
        job.setArtifact(this);
    }
}
'@
Set-Content -Path 'backend\src\main\java\com\museum\artifacts\domain\Artifact.java' -Value $fileContent -Encoding UTF8
Write-Host 'wrote backend\src\main\java\com\museum\artifacts\domain\Artifact.java'

$fileContent = @'
package com.museum.artifacts.domain;

/** Lifecycle state of an artifact. Mirrors ck_artifacts_status in schema.sql. */
public enum ArtifactStatus {
    STORED,
    ON_DISPLAY,
    IN_RESTORATION,
    DEACCESSIONED
}
'@
Set-Content -Path 'backend\src\main\java\com\museum\artifacts\domain\ArtifactStatus.java' -Value $fileContent -Encoding UTF8
Write-Host 'wrote backend\src\main\java\com\museum\artifacts\domain\ArtifactStatus.java'

$fileContent = @'
package com.museum.artifacts.domain;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

/** Timestamp columns every table carries. */
@MappedSuperclass
@Getter
public abstract class Auditable {

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
'@
Set-Content -Path 'backend\src\main\java\com\museum\artifacts\domain\Auditable.java' -Value $fileContent -Encoding UTF8
Write-Host 'wrote backend\src\main\java\com\museum\artifacts\domain\Auditable.java'

$fileContent = @'
package com.museum.artifacts.domain;

/** Conservator's assessment on a condition report, best to worst. */
public enum ConditionGrade {
    EXCELLENT,
    GOOD,
    FAIR,
    POOR,
    CRITICAL
}
'@
Set-Content -Path 'backend\src\main\java\com\museum\artifacts\domain\ConditionGrade.java' -Value $fileContent -Encoding UTF8
Write-Host 'wrote backend\src\main\java\com\museum\artifacts\domain\ConditionGrade.java'

$fileContent = @'
package com.museum.artifacts.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

/** An inspection record. Append-only: never updated after creation. */
@Entity
@Table(name = "condition_reports")
@Getter
@Setter
@NoArgsConstructor
public class ConditionReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "artifact_id", nullable = false)
    private Artifact artifact;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inspector_id", nullable = false)
    private User inspector;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ConditionGrade grade;

    @Size(max = 1000)
    @Column(length = 1000)
    private String notes;

    @NotNull
    @PastOrPresent
    @Column(name = "inspected_on", nullable = false)
    private LocalDate inspectedOn;

    @Column(name = "created_at", insertable = false, updatable = false)
    private java.time.Instant createdAt;

    public ConditionReport(User inspector, ConditionGrade grade, String notes, LocalDate inspectedOn) {
        this.inspector = inspector;
        this.grade = grade;
        this.notes = notes;
        this.inspectedOn = inspectedOn;
    }
}
'@
Set-Content -Path 'backend\src\main\java\com\museum\artifacts\domain\ConditionReport.java' -Value $fileContent -Encoding UTF8
Write-Host 'wrote backend\src\main\java\com\museum\artifacts\domain\ConditionReport.java'

$fileContent = @'
package com.museum.artifacts.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "exhibitions")
@Getter
@Setter
@NoArgsConstructor
public class Exhibition extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 200)
    @Column(nullable = false, length = 200)
    private String title;

    @Size(max = 120)
    @Column(length = 120)
    private String gallery;

    @NotNull
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @NotNull
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @OneToMany(mappedBy = "exhibition", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("displayOrder ASC")
    private List<ExhibitionArtifact> entries = new ArrayList<>();

    @AssertTrue(message = "endDate must be after startDate")
    public boolean isDateRangeValid() {
        return startDate == null || endDate == null || endDate.isAfter(startDate);
    }

    @Transient
    public boolean isActiveOn(LocalDate date) {
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    public void addArtifact(Artifact artifact, int displayOrder) {
        ExhibitionArtifact entry = new ExhibitionArtifact(this, artifact, displayOrder);
        entries.add(entry);
    }
}
'@
Set-Content -Path 'backend\src\main\java\com\museum\artifacts\domain\Exhibition.java' -Value $fileContent -Encoding UTF8
Write-Host 'wrote backend\src\main\java\com\museum\artifacts\domain\Exhibition.java'

$fileContent = @'
package com.museum.artifacts.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.time.Instant;

/**
 * The many-to-many join between Exhibition and Artifact, modelled as its own
 * entity because it carries display_order.
 */
@Entity
@Table(name = "exhibition_artifacts")
@Getter
@Setter
@NoArgsConstructor
public class ExhibitionArtifact {

    @EmbeddedId
    private ExhibitionArtifactId id = new ExhibitionArtifactId();

    @MapsId("exhibitionId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exhibition_id", nullable = false)
    private Exhibition exhibition;

    @MapsId("artifactId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "artifact_id", nullable = false)
    private Artifact artifact;

    @Positive
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    public ExhibitionArtifact(Exhibition exhibition, Artifact artifact, Integer displayOrder) {
        this.exhibition = exhibition;
        this.artifact = artifact;
        this.displayOrder = displayOrder;
        this.id = new ExhibitionArtifactId(exhibition.getId(), artifact.getId());
    }
}
'@
Set-Content -Path 'backend\src\main\java\com\museum\artifacts\domain\ExhibitionArtifact.java' -Value $fileContent -Encoding UTF8
Write-Host 'wrote backend\src\main\java\com\museum\artifacts\domain\ExhibitionArtifact.java'

$fileContent = @'
package com.museum.artifacts.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

/** Composite key for the exhibition_artifacts join table. */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ExhibitionArtifactId implements Serializable {

    @Column(name = "exhibition_id")
    private Long exhibitionId;

    @Column(name = "artifact_id")
    private Long artifactId;
}
'@
Set-Content -Path 'backend\src\main\java\com\museum\artifacts\domain\ExhibitionArtifactId.java' -Value $fileContent -Encoding UTF8
Write-Host 'wrote backend\src\main\java\com\museum\artifacts\domain\ExhibitionArtifactId.java'

$fileContent = @'
package com.museum.artifacts.domain;

/** Lifecycle state of a restoration job. */
public enum JobStatus {
    OPEN,
    IN_PROGRESS,
    CLOSED
}
'@
Set-Content -Path 'backend\src\main\java\com\museum\artifacts\domain\JobStatus.java' -Value $fileContent -Encoding UTF8
Write-Host 'wrote backend\src\main\java\com\museum\artifacts\domain\JobStatus.java'

$fileContent = @'
package com.museum.artifacts.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(
    name = "locations",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_locations_spot",
        columnNames = {"building", "room", "case_code"}
    )
)
@Getter
@Setter
@NoArgsConstructor
public class Location extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 60)
    @Column(nullable = false, length = 60)
    private String building;

    @NotBlank
    @Size(max = 60)
    @Column(nullable = false, length = 60)
    private String room;

    @NotBlank
    @Size(max = 30)
    @Column(name = "case_code", nullable = false, length = 30)
    private String caseCode;

    @Column(name = "climate_controlled", nullable = false)
    private boolean climateControlled = false;

    public Location(String building, String room, String caseCode, boolean climateControlled) {
        this.building = building;
        this.room = room;
        this.caseCode = caseCode;
        this.climateControlled = climateControlled;
    }

    @Transient
    public String getLabel() {
        return building + " / " + room + " / " + caseCode;
    }
}
'@
Set-Content -Path 'backend\src\main\java\com\museum\artifacts\domain\Location.java' -Value $fileContent -Encoding UTF8
Write-Host 'wrote backend\src\main\java\com\museum\artifacts\domain\Location.java'

$fileContent = @'
package com.museum.artifacts.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * A curatorial collection. Named MuseumCollection rather than Collection to
 * avoid colliding with java.util.Collection in every file that imports both.
 */
@Entity
@Table(name = "collections")
@Getter
@Setter
@NoArgsConstructor
public class MuseumCollection extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 120)
    @Column(nullable = false, unique = true, length = 120)
    private String name;

    @Size(max = 500)
    @Column(length = 500)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "curator_id")
    private User curator;

    @OneToMany(mappedBy = "collection", fetch = FetchType.LAZY)
    private List<Artifact> artifacts = new ArrayList<>();

    public MuseumCollection(String name, String description, User curator) {
        this.name = name;
        this.description = description;
        this.curator = curator;
    }
}
'@
Set-Content -Path 'backend\src\main\java\com\museum\artifacts\domain\MuseumCollection.java' -Value $fileContent -Encoding UTF8
Write-Host 'wrote backend\src\main\java\com\museum\artifacts\domain\MuseumCollection.java'

$fileContent = @'
package com.museum.artifacts.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "restoration_jobs")
@Getter
@Setter
@NoArgsConstructor
public class RestorationJob extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "artifact_id", nullable = false)
    private Artifact artifact;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "restorer_id", nullable = false)
    private User restorer;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private JobStatus status = JobStatus.OPEN;

    @NotNull
    @Column(name = "opened_on", nullable = false)
    private LocalDate openedOn;

    @Column(name = "closed_on")
    private LocalDate closedOn;

    @Size(max = 1000)
    @Column(length = 1000)
    private String summary;

    public RestorationJob(User restorer, LocalDate openedOn, String summary) {
        this.restorer = restorer;
        this.openedOn = openedOn;
        this.summary = summary;
    }

    /** Mirrors ck_restoration_jobs_dates so the violation surfaces before the insert. */
    @AssertTrue(message = "closedOn must not be earlier than openedOn")
    public boolean isDateOrderValid() {
        return closedOn == null || openedOn == null || !closedOn.isBefore(openedOn);
    }
}
'@
Set-Content -Path 'backend\src\main\java\com\museum\artifacts\domain\RestorationJob.java' -Value $fileContent -Encoding UTF8
Write-Host 'wrote backend\src\main\java\com\museum\artifacts\domain\RestorationJob.java'

$fileContent = @'
package com.museum.artifacts.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Role extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 20)
    @Column(nullable = false, unique = true, length = 20)
    private String name;

    public Role(String name) {
        this.name = name;
    }
}
'@
Set-Content -Path 'backend\src\main\java\com\museum\artifacts\domain\Role.java' -Value $fileContent -Encoding UTF8
Write-Host 'wrote backend\src\main\java\com\museum\artifacts\domain\Role.java'

$fileContent = @'
package com.museum.artifacts.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Email
    @Size(max = 120)
    @Column(nullable = false, unique = true, length = 120)
    private String email;

    /** BCrypt hash. Never expose this field in a response DTO. */
    @NotBlank
    @Column(name = "password_hash", nullable = false, length = 72)
    private String passwordHash;

    @NotBlank
    @Size(max = 120)
    @Column(name = "full_name", nullable = false, length = 120)
    private String fullName;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Column(nullable = false)
    private boolean active = true;

    public User(String email, String passwordHash, String fullName, Role role) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.role = role;
    }
}
'@
Set-Content -Path 'backend\src\main\java\com\museum\artifacts\domain\User.java' -Value $fileContent -Encoding UTF8
Write-Host 'wrote backend\src\main\java\com\museum\artifacts\domain\User.java'

$fileContent = @'
package com.museum.artifacts.repository;

import com.museum.artifacts.domain.Artifact;
import com.museum.artifacts.domain.ArtifactStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ArtifactRepository extends JpaRepository<Artifact, Long> {

    Optional<Artifact> findByAccessionNumber(String accessionNumber);

    boolean existsByAccessionNumber(String accessionNumber);

    Page<Artifact> findByStatus(ArtifactStatus status, Pageable pageable);

    Page<Artifact> findByCollectionId(Long collectionId, Pageable pageable);

    /**
     * Filtered list endpoint. Every parameter is optional: a null means
     * "do not filter on this field".
     */
    @Query("""
           SELECT a FROM Artifact a
           WHERE (:status IS NULL OR a.status = :status)
             AND (:material IS NULL OR LOWER(a.material) = LOWER(:material))
             AND (:collectionId IS NULL OR a.collection.id = :collectionId)
             AND (:search IS NULL OR LOWER(a.title) LIKE LOWER(CONCAT('%', :search, '%'))
                                  OR LOWER(a.accessionNumber) LIKE LOWER(CONCAT('%', :search, '%')))
           """)
    Page<Artifact> search(@Param("status") ArtifactStatus status,
                          @Param("material") String material,
                          @Param("collectionId") Long collectionId,
                          @Param("search") String search,
                          Pageable pageable);

    /** Detail screen: one query instead of three round trips. */
    @Query("""
           SELECT a FROM Artifact a
           JOIN FETCH a.collection
           JOIN FETCH a.location
           WHERE a.id = :id
           """)
    Optional<Artifact> findDetailById(@Param("id") Long id);

    /** Artifacts with no inspection since the given date. Drives the backlog view. */
    @Query("""
           SELECT a FROM Artifact a
           WHERE a.status <> com.museum.artifacts.domain.ArtifactStatus.DEACCESSIONED
             AND NOT EXISTS (
                   SELECT 1 FROM ConditionReport r
                   WHERE r.artifact = a AND r.inspectedOn >= :since)
           ORDER BY a.accessionNumber ASC
           """)
    List<Artifact> findOverdueForInspection(@Param("since") LocalDate since);

    /** Guards FR-11: refuse to delete an artifact on a live exhibition. */
    @Query("""
           SELECT COUNT(e) > 0 FROM ExhibitionArtifact e
           WHERE e.artifact.id = :artifactId
             AND e.exhibition.endDate >= :today
           """)
    boolean isOnActiveExhibition(@Param("artifactId") Long artifactId,
                                 @Param("today") LocalDate today);

    long countByStatus(ArtifactStatus status);
}
'@
Set-Content -Path 'backend\src\main\java\com\museum\artifacts\repository\ArtifactRepository.java' -Value $fileContent -Encoding UTF8
Write-Host 'wrote backend\src\main\java\com\museum\artifacts\repository\ArtifactRepository.java'

$fileContent = @'
package com.museum.artifacts.repository;

import com.museum.artifacts.domain.ConditionGrade;
import com.museum.artifacts.domain.ConditionReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ConditionReportRepository extends JpaRepository<ConditionReport, Long> {

    Page<ConditionReport> findByArtifactIdOrderByInspectedOnDesc(Long artifactId, Pageable pageable);

    List<ConditionReport> findByGrade(ConditionGrade grade);

    /** Most recent report for one artifact, shown on the detail screen. */
    @Query("""
           SELECT r FROM ConditionReport r
           WHERE r.artifact.id = :artifactId
           ORDER BY r.inspectedOn DESC, r.id DESC
           LIMIT 1
           """)
    Optional<ConditionReport> findLatestForArtifact(@Param("artifactId") Long artifactId);

    @Query("""
           SELECT r FROM ConditionReport r
           JOIN FETCH r.artifact
           WHERE r.grade IN (com.museum.artifacts.domain.ConditionGrade.POOR,
                             com.museum.artifacts.domain.ConditionGrade.CRITICAL)
           ORDER BY r.inspectedOn DESC
           """)
    List<ConditionReport> findRecentConcerns();
}
'@
Set-Content -Path 'backend\src\main\java\com\museum\artifacts\repository\ConditionReportRepository.java' -Value $fileContent -Encoding UTF8
Write-Host 'wrote backend\src\main\java\com\museum\artifacts\repository\ConditionReportRepository.java'

$fileContent = @'
package com.museum.artifacts.repository;

import com.museum.artifacts.domain.ExhibitionArtifact;
import com.museum.artifacts.domain.ExhibitionArtifactId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ExhibitionArtifactRepository
        extends JpaRepository<ExhibitionArtifact, ExhibitionArtifactId> {

    List<ExhibitionArtifact> findByExhibitionIdOrderByDisplayOrderAsc(Long exhibitionId);

    List<ExhibitionArtifact> findByArtifactId(Long artifactId);

    Optional<ExhibitionArtifact> findByExhibitionIdAndArtifactId(Long exhibitionId, Long artifactId);

    void deleteByExhibitionIdAndArtifactId(Long exhibitionId, Long artifactId);

    /** Next free slot when attaching an artifact without an explicit order. */
    @Query("""
           SELECT COALESCE(MAX(e.displayOrder), 0) + 1
           FROM ExhibitionArtifact e
           WHERE e.exhibition.id = :exhibitionId
           """)
    int nextDisplayOrder(@Param("exhibitionId") Long exhibitionId);
}
'@
Set-Content -Path 'backend\src\main\java\com\museum\artifacts\repository\ExhibitionArtifactRepository.java' -Value $fileContent -Encoding UTF8
Write-Host 'wrote backend\src\main\java\com\museum\artifacts\repository\ExhibitionArtifactRepository.java'

$fileContent = @'
package com.museum.artifacts.repository;

import com.museum.artifacts.domain.Exhibition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ExhibitionRepository extends JpaRepository<Exhibition, Long> {

    Page<Exhibition> findByEndDateGreaterThanEqual(LocalDate date, Pageable pageable);

    List<Exhibition> findByStartDateLessThanEqualAndEndDateGreaterThanEqual(LocalDate start, LocalDate end);

    /** Exhibition with its artifact entries loaded, in display order. */
    @Query("""
           SELECT DISTINCT e FROM Exhibition e
           LEFT JOIN FETCH e.entries en
           LEFT JOIN FETCH en.artifact
           WHERE e.id = :id
           """)
    Optional<Exhibition> findDetailById(@Param("id") Long id);
}
'@
Set-Content -Path 'backend\src\main\java\com\museum\artifacts\repository\ExhibitionRepository.java' -Value $fileContent -Encoding UTF8
Write-Host 'wrote backend\src\main\java\com\museum\artifacts\repository\ExhibitionRepository.java'

$fileContent = @'
package com.museum.artifacts.repository;

import com.museum.artifacts.domain.Location;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LocationRepository extends JpaRepository<Location, Long> {

    Optional<Location> findByBuildingAndRoomAndCaseCode(String building, String room, String caseCode);

    List<Location> findByClimateControlledTrue();

    List<Location> findByBuildingOrderByRoomAscCaseCodeAsc(String building);
}
'@
Set-Content -Path 'backend\src\main\java\com\museum\artifacts\repository\LocationRepository.java' -Value $fileContent -Encoding UTF8
Write-Host 'wrote backend\src\main\java\com\museum\artifacts\repository\LocationRepository.java'

$fileContent = @'
package com.museum.artifacts.repository;

import com.museum.artifacts.domain.MuseumCollection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MuseumCollectionRepository extends JpaRepository<MuseumCollection, Long> {

    Optional<MuseumCollection> findByName(String name);

    boolean existsByName(String name);

    Page<MuseumCollection> findByCuratorId(Long curatorId, Pageable pageable);

    /** Collection name plus its artifact count, for the collections list screen. */
    @Query("""
           SELECT c.id, c.name, COUNT(a.id)
           FROM MuseumCollection c
           LEFT JOIN c.artifacts a
           GROUP BY c.id, c.name
           ORDER BY c.name ASC
           """)
    List<Object[]> findAllWithArtifactCounts();

    @Query("SELECT COUNT(a) FROM Artifact a WHERE a.collection.id = :collectionId")
    long countArtifacts(@Param("collectionId") Long collectionId);
}
'@
Set-Content -Path 'backend\src\main\java\com\museum\artifacts\repository\MuseumCollectionRepository.java' -Value $fileContent -Encoding UTF8
Write-Host 'wrote backend\src\main\java\com\museum\artifacts\repository\MuseumCollectionRepository.java'

$fileContent = @'
package com.museum.artifacts.repository;

import com.museum.artifacts.domain.JobStatus;
import com.museum.artifacts.domain.RestorationJob;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RestorationJobRepository extends JpaRepository<RestorationJob, Long> {

    Page<RestorationJob> findByStatus(JobStatus status, Pageable pageable);

    List<RestorationJob> findByArtifactIdOrderByOpenedOnDesc(Long artifactId);

    Page<RestorationJob> findByRestorerId(Long restorerId, Pageable pageable);

    /** The restorer's work queue: anything not yet closed, oldest first. */
    @Query("""
           SELECT j FROM RestorationJob j
           JOIN FETCH j.artifact
           WHERE j.status <> com.museum.artifacts.domain.JobStatus.CLOSED
           ORDER BY j.openedOn ASC
           """)
    List<RestorationJob> findOpenQueue();

    boolean existsByArtifactIdAndStatusNot(Long artifactId, JobStatus status);

    long countByStatus(JobStatus status);
}
'@
Set-Content -Path 'backend\src\main\java\com\museum\artifacts\repository\RestorationJobRepository.java' -Value $fileContent -Encoding UTF8
Write-Host 'wrote backend\src\main\java\com\museum\artifacts\repository\RestorationJobRepository.java'

$fileContent = @'
package com.museum.artifacts.repository;

import com.museum.artifacts.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(String name);

    boolean existsByName(String name);
}
'@
Set-Content -Path 'backend\src\main\java\com\museum\artifacts\repository\RoleRepository.java' -Value $fileContent -Encoding UTF8
Write-Host 'wrote backend\src\main\java\com\museum\artifacts\repository\RoleRepository.java'

$fileContent = @'
package com.museum.artifacts.repository;

import com.museum.artifacts.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    /** Login path: only active accounts may authenticate. */
    Optional<User> findByEmailAndActiveTrue(String email);

    boolean existsByEmail(String email);

    Page<User> findByActive(boolean active, Pageable pageable);

    @Query("SELECT u FROM User u JOIN FETCH u.role WHERE u.role.name = :roleName AND u.active = true")
    java.util.List<User> findActiveByRoleName(@Param("roleName") String roleName);
}
'@
Set-Content -Path 'backend\src\main\java\com\museum\artifacts\repository\UserRepository.java' -Value $fileContent -Encoding UTF8
Write-Host 'wrote backend\src\main\java\com\museum\artifacts\repository\UserRepository.java'

$fileContent = @'
spring.application.name=artifact-manager

# ---- Datasource ----
# Override locally with application-local.properties (git-ignored) or env vars.
spring.datasource.url=${DB_URL:jdbc:mysql://localhost:3306/museum_db}
spring.datasource.username=${DB_USER:root}
spring.datasource.password=${DB_PASSWORD:changeme}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# ---- JPA ----
# validate, never update: schema.sql is the source of truth.
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.open-in-view=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.show-sql=false

# ---- Pagination ----
spring.data.web.pageable.default-page-size=20
spring.data.web.pageable.max-page-size=100

# ---- JSON ----
spring.jackson.serialization.write-dates-as-timestamps=false

# ---- Server ----
server.port=8080
server.error.include-message=always
'@
Set-Content -Path 'backend\src\main\resources\application.properties' -Value $fileContent -Encoding UTF8
Write-Host 'wrote backend\src\main\resources\application.properties'

Write-Host ''
Write-Host 'Done. 26 files written.'