package de.mmenze.corona.domain;

import lombok.Data;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "cases")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Cases {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "date", nullable = false)
    private LocalDate date;

    @NotNull
    @Column(name = "confirmed", nullable = false)
    private int confirmed;

    @NotNull
    @Column(name = "deaths", nullable = false)
    private int deaths;

    @NotNull
    @Column(name = "recovered", nullable = false)
    private int recovered;

    @NotNull
    @Column(name = "active", nullable = false)
    private int active;

    @NotNull
    @Column(name = "mortality_rate")
    private double mortalityRate;

    @NotNull
    @Column(name = "recovered_rate")
    private double recoveredRate;

    @NotNull
    @ManyToOne(optional = false)
    private Region region;

}
