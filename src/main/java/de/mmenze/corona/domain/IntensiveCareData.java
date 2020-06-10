package de.mmenze.corona.domain;

import lombok.Data;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "intensive_care_data")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class IntensiveCareData {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "date", nullable = false)
    private LocalDate date;

    @NotNull
    @ManyToOne(optional = false)
    private Region district;

    @NotNull
    @ManyToOne(optional = false)
    private Region state;

    @NotNull
    @Column(name = "cases", nullable = false)
    private int cases;

    @NotNull
    @Column(name = "cases_ventilated", nullable = false)
    private int casesVentilated;

    @NotNull
    @Column(name = "intensive_care_units_free", nullable = false)
    private int intensiveCareUnitsFree;

    @NotNull
    @Column(name = "intensive_care_units_occupied", nullable = false)
    private int intensiveCareUnitsOccupied;

}
