package pe.edu.galaxy.training.java.quarkus.loan.adapter.out.persistence.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;

/**
 * Detalle de la solicitud. objectId es un id plano hacia
 * community_db: sin clave foránea entre bases.
 */
@Entity
@Table(name = "loan_request_items")
public class LoanRequestItemEntity extends PanacheEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "loan_request_id", nullable = false)
    public LoanRequestEntity loanRequest;

    @Column(name = "object_id", nullable = false)
    public Long objectId;

    @Column(nullable = true, columnDefinition = "text")
    public String notes;
}
