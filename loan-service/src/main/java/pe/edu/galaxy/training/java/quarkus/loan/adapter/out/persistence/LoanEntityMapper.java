package pe.edu.galaxy.training.java.quarkus.loan.adapter.out.persistence;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pe.edu.galaxy.training.java.quarkus.loan.adapter.out.persistence.entity.LoanRequestEntity;
import pe.edu.galaxy.training.java.quarkus.loan.adapter.out.persistence.entity.LoanRequestItemEntity;
import pe.edu.galaxy.training.java.quarkus.loan.domain.model.LoanRequest;
import pe.edu.galaxy.training.java.quarkus.loan.domain.model.LoanRequestItem;

/**
 * Entidad JPA ↔ dominio. Solo lectura (entity -> domain): construir la
 * entidad para persistir es responsabilidad de
 * {@link LoanRepositoryAdapter#save}, a mano, porque necesita fijar la
 * referencia bidireccional item -> loanRequest que MapStruct no resuelve
 * solo
 */
@Mapper(componentModel = "jakarta-cdi")
public interface LoanEntityMapper {

    @Mapping(target = "withStatus", ignore = true)
    LoanRequest toDomain(LoanRequestEntity entity);

    LoanRequestItem toItemDomain(LoanRequestItemEntity entity);
}
