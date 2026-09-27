package pe.edu.galaxy.training.java.quarkus.loan.adapter.out.persistence.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import pe.edu.galaxy.training.java.quarkus.loan.domain.model.LoanStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Solicitud de préstamo — maestro de la relación maestro/detalle con
 * {@link LoanRequestItemEntity}. requesterId es un id plano hacia
 * community_db: no hay clave foránea entre bases.
 */
@Entity
@Table(name = "loan_requests")
public class LoanRequestEntity extends PanacheEntity {

    @Column(name = "requester_id", nullable = false)
    public Long requesterId;

    @Column(name = "requested_from", nullable = false)
    public LocalDate requestedFrom;

    @Column(name = "requested_until", nullable = false)
    public LocalDate requestedUntil;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    public LoanStatus status;

    @Column(nullable = false, columnDefinition = "text")
    public String reason;

    @Column(name = "created_at", nullable = false)
    public Instant createdAt;

    @OneToMany(mappedBy = "loanRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    public List<LoanRequestItemEntity> items = new ArrayList<>();
}
