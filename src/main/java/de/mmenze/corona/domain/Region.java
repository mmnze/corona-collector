package de.mmenze.corona.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import de.mmenze.corona.domain.enums.RegionType;
import lombok.Data;

@Data
@Entity
@Table(name = "region")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Region {
    @SuppressWarnings("unused")
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "name", nullable = false)
    private String name;
    @NotNull
    @Column(name = "population", nullable = false)
    private int population;

    @NotNull
    @Column(name = "lat", nullable = false)
    private double lat;
    @NotNull
    @Column(name = "lng", nullable = false)
    private double lng;

    @Column(name = "code")
    private String code;
    @Column(name = "continent")
    private String continent;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "region_type", nullable = false)
    private RegionType regionType;

}
