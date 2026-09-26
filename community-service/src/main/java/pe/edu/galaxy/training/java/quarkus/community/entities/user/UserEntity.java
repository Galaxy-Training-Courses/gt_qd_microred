package pe.edu.galaxy.training.java.quarkus.community.entities.user;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;

import java.time.Instant;

/**
 * Miembro de la comunidad. Nombres y tipos de columna se
 * fijan explícitamente para que la migración Flyway no obligue a tocar esta clase.
 */
@Entity
@Table(name = "users")
public class UserEntity extends PanacheEntity {

    @Column(nullable = false, length = 100)
    public String name;

    @Column(nullable = false, unique = true, length = 150)
    public String email;

    @Column(nullable = false, length = 100)
    public String neighborhood;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    public UserStatus status;

    @Column(name = "created_at", nullable = false)
    public Instant createdAt;
}
