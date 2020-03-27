package de.mmenze.corona.domain;

import de.mmenze.corona.domain.enums.RegionType;
import lombok.Data;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Data
@Entity
@Table(name = "region")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Region {
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

    @Column(name = "code", nullable = false)
    private String code;
    @Column(name = "continent", nullable = false)
    private String continent;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "region_type", nullable = false)
    private RegionType regionType;

}
