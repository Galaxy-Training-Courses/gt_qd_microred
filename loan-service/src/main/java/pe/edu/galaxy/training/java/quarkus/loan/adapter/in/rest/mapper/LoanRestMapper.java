package pe.edu.galaxy.training.java.quarkus.loan.adapter.in.rest.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pe.edu.galaxy.training.java.quarkus.loan.adapter.in.rest.dto.*;
import pe.edu.galaxy.training.java.quarkus.loan.application.port.LoanPage;
import pe.edu.galaxy.training.java.quarkus.loan.application.port.in.CreateLoanCommand;
import pe.edu.galaxy.training.java.quarkus.loan.domain.model.LoanRequest;
import pe.edu.galaxy.training.java.quarkus.loan.domain.model.LoanRequestItem;

/**
 * DTO de JAX-RS ↔ dominio. Es el único punto del módulo
 * que conoce a la vez las dos formas: la HTTP y la de dominio.
 */
@Mapper(componentModel = "jakarta-cdi")
public interface LoanRestMapper {

    LoanResponse toResponse(LoanRequest domain);

    LoanItemResponse toItemResponse(LoanRequestItem domain);

    @Mapping(target = "id", ignore = true)
    LoanRequestItem toItem(CreateLoanItemRequest dto);

    CreateLoanCommand toCommand(CreateLoanRequest dto);

    PageResponse<LoanResponse> toPageResponse(LoanPage page);
}
