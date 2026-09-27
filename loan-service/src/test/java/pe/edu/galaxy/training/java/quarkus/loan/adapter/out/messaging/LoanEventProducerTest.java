package pe.edu.galaxy.training.java.quarkus.loan.adapter.out.messaging;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pe.edu.galaxy.training.java.quarkus.loan.adapter.in.rest.TestJwt;

import java.time.Duration;
import java.util.List;
import java.util.Properties;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class LoanEventProducerTest {

    @ConfigProperty(name = "kafka.bootstrap.servers")
    String bootstrapServers;

    private KafkaConsumer<Long, String> consumer;

    @BeforeEach
    void subscribeToTopic() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "loan-event-producer-test-" + System.nanoTime());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumer = new KafkaConsumer<>(props);
        consumer.subscribe(List.of("loan-events"));
    }

    @AfterEach
    void closeConsumer() {
        consumer.close();
    }

    @Test
    void create_publishesLoanCreatedEventToKafka() {
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + TestJwt.validToken())
                .body("""
                        {"requesterId":2,"requestedFrom":"2026-11-01","requestedUntil":"2026-11-05",
                         "reason":"Prueba de mensajeria","items":[{"objectId":1,"notes":null}]}
                        """)
        .when()
                .post("/api/loans")
        .then()
                .statusCode(201);

        boolean found = false;
        for (int attempt = 0; attempt < 10 && !found; attempt++) {
            ConsumerRecords<Long, String> records = consumer.poll(Duration.ofSeconds(1));
            for (ConsumerRecord<Long, String> record : records) {
                if (record.value() != null && record.value().contains("\"eventType\":\"LOAN_CREATED\"")) {
                    found = true;
                }
            }
        }

        assertTrue(found, "Se esperaba un mensaje LOAN_CREATED en el topico loan-events");
    }
}
