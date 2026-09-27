package pe.edu.galaxy.training.java.quarkus.loan.adapter.in.rest;

import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import io.quarkus.security.Authenticated;
import lombok.RequiredArgsConstructor;
import pe.edu.galaxy.training.java.quarkus.loan.adapter.in.rest.common.PagingSupport;
import pe.edu.galaxy.training.java.quarkus.loan.adapter.in.rest.dto.CreateLoanRequest;
import pe.edu.galaxy.training.java.quarkus.loan.adapter.in.rest.dto.LoanResponse;
import pe.edu.galaxy.training.java.quarkus.loan.adapter.in.rest.dto.PageResponse;
import pe.edu.galaxy.training.java.quarkus.loan.adapter.in.rest.mapper.LoanRestMapper;
import pe.edu.galaxy.training.java.quarkus.loan.application.port.LoanPage;
import pe.edu.galaxy.training.java.quarkus.loan.application.port.in.LoanUseCase;
import pe.edu.galaxy.training.java.quarkus.loan.domain.model.LoanRequest;
import pe.edu.galaxy.training.java.quarkus.loan.domain.model.LoanStatus;

import java.time.LocalDate;

/**
 * Adaptador de entrada: traduce HTTP a llamadas de {@link LoanUseCase}
 * y DTO ↔ dominio vía {@link LoanRestMapper}; no contiene ninguna regla
 * de negocio — esas viven en el dominio.
 */
@Path("/api/loans")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
public class LoanResource {
    private final LoanUseCase loanUseCase;
    private final LoanRestMapper loanRestMapper;

    @GET
    public PageResponse<LoanResponse> list(@QueryParam("page") @DefaultValue("0") int page,
                                           @QueryParam("size") @DefaultValue("" + PagingSupport.DEFAULT_SIZE) int size,
                                           @QueryParam("status") String status,
                                           @QueryParam("requesterId") Long requesterId,
                                           @QueryParam("from") LocalDate from,
                                           @QueryParam("until") LocalDate until,
                                           @QueryParam("sort") String sort) {
        PagingSupport.validate(page, size);
        LoanStatus parsedStatus = status != null && !status.isBlank() ? LoanStatus.parseStatus(status) : null;
        LoanPage loanPage = loanUseCase.search(page, size, parsedStatus, requesterId, from, until, sort);
        return loanRestMapper.toPageResponse(loanPage);
    }

    @GET
    @Path("/{id}")
    public LoanResponse findById(@PathParam("id") Long id) {
        return loanRestMapper.toResponse(loanUseCase.findById(id));
    }

    @POST
    @Authenticated
    public Response create(@Valid CreateLoanRequest request, @Context UriInfo uriInfo) {
        LoanRequest loanRequest = loanUseCase.create(loanRestMapper.toCommand(request));
        LoanResponse created = loanRestMapper.toResponse(loanRequest);
        return Response
                .created(uriInfo.getAbsolutePathBuilder().path(String.valueOf(created.id())).build())
                .entity(created)
                .build();
    }

    @PATCH
    @Path("/{id}/approve")
    @Authenticated
    public LoanResponse approve(@PathParam("id") Long id) {
        return loanRestMapper.toResponse(loanUseCase.approve(id));
    }

    @PATCH
    @Path("/{id}/reject")
    @Authenticated
    public LoanResponse reject(@PathParam("id") Long id) {
        return loanRestMapper.toResponse(loanUseCase.reject(id));
    }

    @PATCH
    @Path("/{id}/cancel")
    public LoanResponse cancel(@PathParam("id") Long id) {
        return loanRestMapper.toResponse(loanUseCase.cancel(id));
    }

    @PATCH
    @Path("/{id}/return")
    public LoanResponse returnLoan(@PathParam("id") Long id) {
        return loanRestMapper.toResponse(loanUseCase.returnLoan(id));
    }
}
