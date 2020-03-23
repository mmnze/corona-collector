package de.mmenze.corona.domain;

import lombok.Data;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "delta_cases")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class DeltaCases {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "base_date", nullable = false)
    private LocalDate baseDate;

    @NotNull
    @Column(name = "increase_confirmed_1d", nullable = false)
    private int increaseConfirmed1d;
    @NotNull
    @Column(name = "increase_confirmed_7d", nullable = false)
    private int increaseConfirmed7d;
    @NotNull
    @Column(name = "increase_confirmed_14d", nullable = false)
    private int increaseConfirmed14d;

    @NotNull
    @Column(name = "increase_deaths_1d", nullable = false)
    private int increaseDeaths1d;
    @NotNull
    @Column(name = "increase_deaths_7d", nullable = false)
    private int increaseDeaths7d;
    @NotNull
    @Column(name = "increase_deaths_14d", nullable = false)
    private int increaseDeaths14d;


    @NotNull
    @Column(name = "increase_recovered_1d", nullable = false)
    private int increaseRecovered1d;
    @NotNull
    @Column(name = "increase_recovered_7d", nullable = false)
    private int increaseRecovered7d;
    @NotNull
    @Column(name = "increase_recovered_14d", nullable = false)
    private int increaseRecovered14d;
    @NotNull

    @NotNull
    @Column(name = "delta_active_1d", nullable = false)
    private int deltaActive1d;
    @NotNull
    @Column(name = "delta_active_7d", nullable = false)
    private int deltaActive7d;
    @NotNull
    @Column(name = "delta_active_14d", nullable = false)
    private int deltaActive14d;

    @NotNull
    @Column(name = "doubling_rate", nullable = false)
    private double doublingRate;

    @NotNull
    @ManyToOne(optional = false)
    private Region region;

}
