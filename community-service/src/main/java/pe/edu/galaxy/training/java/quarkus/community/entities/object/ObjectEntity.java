package pe.edu.galaxy.training.java.quarkus.community.entities.object;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import pe.edu.galaxy.training.java.quarkus.community.entities.user.UserEntity;

import java.time.Instant;

/**
 * Objeto disponible para préstamo.
 * La relación con el propietario es la única relación jerárquica de community-service
 */
@Entity
@Table(name = "objects")
public class ObjectEntity extends PanacheEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    public UserEntity owner;

    @Column(nullable = false, length = 150)
    public String name;

    @Column(nullable = false, columnDefinition = "text")
    public String description;

    @Column(nullable = false, length = 50)
    public String category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    public ObjectCondition condition;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    public ObjectStatus status;

    @Column(name = "created_at", nullable = false)
    public Instant createdAt;
}
