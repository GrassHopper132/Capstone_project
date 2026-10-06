package org.peopleshores.capstone_project.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(
    name = "locations",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_locations_spot",
        columnNames = {"building", "room", "case_code"}
    )
)
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

    public Location() {
    }

    public Location(String building, String room, String caseCode, boolean climateControlled) {
        this.building = building;
        this.room = room;
        this.caseCode = caseCode;
        this.climateControlled = climateControlled;
    }

    /** Human-readable label, e.g. "Annex / Storage 2 / S2-R3". */
    public String getLabel() {
        return building + " / " + room + " / " + caseCode;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBuilding() {
        return building;
    }

    public void setBuilding(String building) {
        this.building = building;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getCaseCode() {
        return caseCode;
    }

    public void setCaseCode(String caseCode) {
        this.caseCode = caseCode;
    }

    public boolean isClimateControlled() {
        return climateControlled;
    }

    public void setClimateControlled(boolean climateControlled) {
        this.climateControlled = climateControlled;
    }
}