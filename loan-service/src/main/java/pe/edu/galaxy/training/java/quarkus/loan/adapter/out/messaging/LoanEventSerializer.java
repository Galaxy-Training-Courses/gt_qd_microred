package pe.edu.galaxy.training.java.quarkus.loan.adapter.out.messaging;

import io.quarkus.kafka.client.serialization.ObjectMapperSerializer;

/**
 * Serializador Jackson para {@link LoanEvent} (sin Avro/Schema Registry).
 * Kafka necesita una subclase concreta con constructor sin argumentos por
 * tipo de mensaje.
 */
public class LoanEventSerializer extends ObjectMapperSerializer<LoanEvent> {
}
