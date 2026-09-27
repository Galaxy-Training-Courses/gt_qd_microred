package pe.edu.galaxy.training.java.quarkus.loan.application.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;

import java.time.Clock;

/**
 * Productor CDI de {@link Clock}: facilita probar las
 * marcas de tiempo de {@code LoanApplicationService}/{@code LoanEventProducer}
 * sin depender de {@code Instant.now()} directo.
 * {@code @Singleton} en el método productor, no {@code @ApplicationScoped}:
 * {@code Clock.systemUTC()} es inmutable y sin estado, así que no hay nada
 * que un proxy cliente de {@code @ApplicationScoped} necesite proteger — es
 * la razón concreta que pide el enunciado para elegir un scope sobre otro,
 * no una preferencia arbitraria.
 */
@ApplicationScoped
public class ClockProducer {

    @Produces
    @Singleton
    Clock systemClock() {
        return Clock.systemUTC();
    }
}
