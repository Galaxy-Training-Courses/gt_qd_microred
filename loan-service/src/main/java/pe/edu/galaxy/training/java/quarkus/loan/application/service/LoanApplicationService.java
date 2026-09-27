package pe.edu.galaxy.training.java.quarkus.loan.application.service;

import io.smallrye.mutiny.tuples.Tuple2;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import pe.edu.galaxy.training.java.quarkus.loan.application.interceptor.BusinessOperation;
import pe.edu.galaxy.training.java.quarkus.loan.application.port.LoanPage;
import pe.edu.galaxy.training.java.quarkus.loan.application.port.in.CreateLoanCommand;
import pe.edu.galaxy.training.java.quarkus.loan.application.port.in.LoanUseCase;
import pe.edu.galaxy.training.java.quarkus.loan.application.port.out.CommunityClientPort;
import pe.edu.galaxy.training.java.quarkus.loan.application.port.out.LoanEventPublisherPort;
import pe.edu.galaxy.training.java.quarkus.loan.application.port.out.LoanRepositoryPort;
import pe.edu.galaxy.training.java.quarkus.loan.application.port.out.ObjectFact;
import pe.edu.galaxy.training.java.quarkus.loan.domain.exception.ConflictException;
import pe.edu.galaxy.training.java.quarkus.loan.domain.exception.NotFoundException;
import pe.edu.galaxy.training.java.quarkus.loan.domain.model.LoanRequest;
import pe.edu.galaxy.training.java.quarkus.loan.domain.model.LoanRequestItem;
import pe.edu.galaxy.training.java.quarkus.loan.domain.model.LoanStatus;
import pe.edu.galaxy.training.java.quarkus.loan.domain.service.LoanDomainService;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementa {@link LoanUseCase}: orquesta los puertos y el dominio.
 * Reúne los hechos que necesita {@link LoanDomainService} consultando
 * {@link CommunityClientPort} — el dominio nunca toca un puerto.
 * {@code loanDomainService} se instancia directamente, no se inyecta: es una
 * clase de dominio sin estado, y así el dominio no necesita ni siquiera CDI
 * en su classpath — la separación es real, no solo de paquetes
 * {@code create}/{@code approve}/{@code returnLoan} arman la cadena reactiva
 * hacia community-service y la resuelven con {@code .await().indefinitely()}
 * — MP Fault Tolerance ya acota el tiempo (@Timeout/@Retry en el adaptador),
 * así que este `await` no cuelga — antes de entrar al dominio (síncrono, puro)
 * y a la persistencia bloqueante (@Transactional). {@code reject}/{@code cancel}
 * no tocan community y quedan sin cambios.
 * {@code clock} (vía {@code ClockProducer}) reemplaza los `Instant.now()` directos
 * para que las marcas de tiempo sean testeables;
 * {@code @BusinessOperation} marca las 5 operaciones de negocio para que
 * {@code BusinessOperationInterceptor} registre inicio/fin/duración.
 */
@ApplicationScoped
@RequiredArgsConstructor
public class LoanApplicationService implements LoanUseCase {

    private final LoanRepositoryPort loanRepositoryPort;
    private final CommunityClientPort communityClientPort;
    private final LoanEventPublisherPort loanEventPublisherPort;
    private final Clock clock;
    private final LoanDomainService loanDomainService = new LoanDomainService();

    @Override
    @Transactional
    @BusinessOperation("loan-creation")
    public LoanRequest create(CreateLoanCommand command) {
        LoanRequest candidate = new LoanRequest(
                null,
                command.requesterId(),
                command.requestedFrom(),
                command.requestedUntil(),
                LoanStatus.PENDING,
                command.reason(),
                clock.instant(),
                command.items());

        List<Long> objectIds = command.items().stream()
                .map(LoanRequestItem::objectId)
                .toList();

        Tuple2<Boolean, List<ObjectFact>> facts = communityClientPort.existsUser(command.requesterId())
                .chain(requesterExists -> communityClientPort.fetchObjectFacts(objectIds)
                        .map(objectFacts -> Tuple2.of(requesterExists, objectFacts)))
                .await().indefinitely();

        boolean requesterExists = facts.getItem1();
        Map<Long, Long> objectOwners = facts.getItem2().stream()
                .collect(Collectors.toMap(ObjectFact::objectId, ObjectFact::ownerId));
        Set<Long> unavailableObjectIds = facts.getItem2().stream()
                .filter(ObjectFact::unavailable)
                .map(ObjectFact::objectId)
                .collect(Collectors.toSet());

        loanDomainService.validateCreation(candidate, requesterExists, objectOwners, unavailableObjectIds);

        LoanRequest saved = loanRepositoryPort.save(candidate);
        loanEventPublisherPort.publish("LOAN_CREATED", saved);
        return saved;
    }

    @Override
    public LoanRequest findById(Long id) {
        return getOrThrow(id);
    }

    @Override
    public LoanPage search(int page, int size, LoanStatus status, Long requesterId, LocalDate from, LocalDate until,
                           String sort) {
        return loanRepositoryPort.search(page, size, status, requesterId, from, until, sort);
    }

    @Override
    @Transactional
    @BusinessOperation("loan-approval")
    public LoanRequest approve(Long id) {
        LoanRequest current = getOrThrow(id);
        loanDomainService.validateTransition(current.status(), LoanStatus.APPROVED);

        List<Long> objectIds = current.items().stream()
                .map(LoanRequestItem::objectId)
                .toList();
        List<ObjectFact> facts = communityClientPort.fetchObjectFacts(objectIds).await().indefinitely();
        validateObjectsAvailability(facts);

        for (Long objectId : objectIds) {
            communityClientPort.reserveObject(objectId).await().indefinitely();
        }

        LoanRequest approved = loanRepositoryPort.save(current.withStatus(LoanStatus.APPROVED));
        loanEventPublisherPort.publish("LOAN_APPROVED", approved);
        return approved;
    }

    private static void validateObjectsAvailability(List<ObjectFact> facts) {
        for (ObjectFact fact : facts) {
            if (fact.unavailable()) {
                throw ConflictException.objectUnavailable(fact.objectId());
            }
        }
    }

    @Override
    @Transactional
    @BusinessOperation("loan-rejection")
    public LoanRequest reject(Long id) {
        LoanRequest current = getOrThrow(id);
        loanDomainService.validateTransition(current.status(), LoanStatus.REJECTED);
        LoanRequest rejected = loanRepositoryPort.save(current.withStatus(LoanStatus.REJECTED));
        loanEventPublisherPort.publish("LOAN_REJECTED", rejected);
        return rejected;
    }

    @Override
    @Transactional
    @BusinessOperation("loan-cancellation")
    public LoanRequest cancel(Long id) {
        LoanRequest current = getOrThrow(id);
        loanDomainService.validateTransition(current.status(), LoanStatus.CANCELLED);
        LoanRequest cancelled = loanRepositoryPort.save(current.withStatus(LoanStatus.CANCELLED));
        loanEventPublisherPort.publish("LOAN_CANCELLED", cancelled);
        return cancelled;
    }

    @Override
    @Transactional
    @BusinessOperation("loan-return")
    public LoanRequest returnLoan(Long id) {
        LoanRequest current = getOrThrow(id);
        loanDomainService.validateTransition(current.status(), LoanStatus.RETURNED);

        for (LoanRequestItem item : current.items()) {
            communityClientPort.releaseObject(item.objectId()).await().indefinitely();
        }

        LoanRequest returned = loanRepositoryPort.save(current.withStatus(LoanStatus.RETURNED));
        loanEventPublisherPort.publish("LOAN_RETURNED", returned);
        return returned;
    }

    private LoanRequest getOrThrow(Long id) {
        return loanRepositoryPort.findById(id)
                .orElseThrow(() -> NotFoundException.loanNotFound(id));
    }
}
