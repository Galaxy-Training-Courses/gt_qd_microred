package pe.edu.galaxy.training.java.quarkus.notification;

import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;

/**
 * Deserializador Jackson para {@link LoanEvent} (sin Avro/Schema Registry)
 */
public class LoanEventDeserializer extends ObjectMapperDeserializer<LoanEvent> {

    public LoanEventDeserializer() {
        super(LoanEvent.class);
    }
}
