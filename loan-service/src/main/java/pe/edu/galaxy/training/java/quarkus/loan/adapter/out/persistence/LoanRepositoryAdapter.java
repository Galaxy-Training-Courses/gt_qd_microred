package pe.edu.galaxy.training.java.quarkus.loan.adapter.out.persistence;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import pe.edu.galaxy.training.java.quarkus.loan.adapter.out.persistence.entity.LoanRequestEntity;
import pe.edu.galaxy.training.java.quarkus.loan.adapter.out.persistence.entity.LoanRequestItemEntity;
import pe.edu.galaxy.training.java.quarkus.loan.application.port.LoanPage;
import pe.edu.galaxy.training.java.quarkus.loan.application.port.out.LoanRepositoryPort;
import pe.edu.galaxy.training.java.quarkus.loan.domain.model.LoanRequest;
import pe.edu.galaxy.training.java.quarkus.loan.domain.model.LoanRequestItem;
import pe.edu.galaxy.training.java.quarkus.loan.domain.model.LoanStatus;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Implementa {@link LoanRepositoryPort} sobre Panache.
 * Es el único punto del módulo que sabe que la persistencia es
 * Hibernate ORM bloqueante.
 */
@ApplicationScoped
@RequiredArgsConstructor
public class LoanRepositoryAdapter implements LoanRepositoryPort {

    private final LoanRequestRepository loanRequestRepository;
    private final LoanEntityMapper loanEntityMapper;

    @Override
    public LoanRequest save(LoanRequest loanRequest) {
        LoanRequestEntity entity = loanRequest.id() == null
                ? newEntity(loanRequest)
                : loanRequestRepository.findById(loanRequest.id());
        entity.status = loanRequest.status();
        return loanEntityMapper.toDomain(entity);
    }

    private LoanRequestEntity newEntity(LoanRequest loanRequest) {
        LoanRequestEntity entity = new LoanRequestEntity();
        entity.requesterId = loanRequest.requesterId();
        entity.requestedFrom = loanRequest.requestedFrom();
        entity.requestedUntil = loanRequest.requestedUntil();
        entity.status = loanRequest.status();
        entity.reason = loanRequest.reason();
        entity.createdAt = loanRequest.createdAt();
        for (LoanRequestItem item : loanRequest.items()) {
            LoanRequestItemEntity itemEntity = new LoanRequestItemEntity();
            itemEntity.loanRequest = entity;
            itemEntity.objectId = item.objectId();
            itemEntity.notes = item.notes();
            entity.items.add(itemEntity);
        }
        loanRequestRepository.persist(entity);
        return entity;
    }

    @Override
    public Optional<LoanRequest> findById(Long id) {
        return loanRequestRepository.findByIdOptional(id).map(loanEntityMapper::toDomain);
    }

    @Override
    public LoanPage search(int page, int size, LoanStatus status, Long requesterId, LocalDate from, LocalDate until,
                           String sort) {
        PanacheQuery<LoanRequestEntity> query = loanRequestRepository.search(status, requesterId, from, until, sort);
        query.page(Page.of(page, size));
        var content = query.list().stream()
                .map(loanEntityMapper::toDomain)
                .toList();
        return new LoanPage(content, page, size, query.count(), query.pageCount());
    }
}
