package pe.edu.galaxy.training.java.quarkus.notification;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Confirma que un {@link LoanEvent}
 * produce una línea de log con `eventType=...` legible, sin necesitar Kafka
 * de verdad — JUnit puro, igual criterio que `LoanDomainServiceTest` en
 * loan-service: {@code LoanEventConsumer} no tiene dependencias inyectadas,
 * así que no hace falta `@QuarkusTest`. `LoanEventProducerTest` (loan-service)
 * cubre el lado del envío contra Kafka Dev Services real.
 */
class LoanEventConsumerTest {

    private final LoanEventConsumer consumer = new LoanEventConsumer();
    private final List<String> capturedMessages = new ArrayList<>();
    private Logger julLogger;
    private Handler handler;

    @BeforeEach
    void attachHandler() {
        julLogger = Logger.getLogger(LoanEventConsumer.class.getName());
        handler = new Handler() {
            @Override
            public void publish(LogRecord logRecord) {
                capturedMessages.add(logRecord.getMessage());
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() {
            }
        };
        julLogger.addHandler(handler);
    }

    @AfterEach
    void detachHandler() {
        julLogger.removeHandler(handler);
    }

    @Test
    void onLoanEvent_logsEventTypeAndLoanId() {
        LoanEvent event = new LoanEvent(
                "e1",
                "LOAN_APPROVED",
                Instant.parse("2026-09-19T10:00:00Z"),
                15L,
                3L,
                List.of(7L, 12L),
                "APPROVED",
                "req-abc-123");

        consumer.onLoanEvent(event);

        assertTrue(capturedMessages.stream().anyMatch(m -> m.contains("eventType=LOAN_APPROVED")));
        assertTrue(capturedMessages.stream().anyMatch(m -> m.contains("loanId=15")));
    }

    @Test
    void onLoanEvent_putsRequestIdInMdcWhileLogging() {
        List<Object> capturedRequestIds = new ArrayList<>();
        Handler mdcHandler = new Handler() {
            @Override
            public void publish(LogRecord record) {
                // MDC es por-hilo: este handler corre en el mismo hilo y en el
                // mismo instante que LOG.infof(...), antes del finally que lo limpia.
                capturedRequestIds.add(org.jboss.logging.MDC.get("requestId"));
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() {
            }
        };
        julLogger.addHandler(mdcHandler);

        LoanEvent event = new LoanEvent(
                "e2",
                "LOAN_CREATED",
                Instant.parse("2026-09-19T10:00:00Z"),
                16L,
                3L,
                List.of(7L),
                "PENDING",
                "req-xyz-789");
        consumer.onLoanEvent(event);

        julLogger.removeHandler(mdcHandler);
        assertTrue(capturedRequestIds.contains("req-xyz-789"));
    }
}
