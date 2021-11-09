package de.mmenze.corona.domain;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import lombok.Data;

@Data
@Entity
@Table(name = "cases")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Cases {
    @SuppressWarnings("unused")
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
