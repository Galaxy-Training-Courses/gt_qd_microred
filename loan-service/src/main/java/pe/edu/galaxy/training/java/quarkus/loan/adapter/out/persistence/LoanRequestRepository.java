package pe.edu.galaxy.training.java.quarkus.loan.adapter.out.persistence;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import pe.edu.galaxy.training.java.quarkus.loan.adapter.out.persistence.entity.LoanRequestEntity;
import pe.edu.galaxy.training.java.quarkus.loan.adapter.out.persistence.enums.PaginationField;
import pe.edu.galaxy.training.java.quarkus.loan.adapter.out.persistence.utils.PersistenceConstants;
import pe.edu.galaxy.training.java.quarkus.loan.adapter.out.persistence.utils.QueryUtil;
import pe.edu.galaxy.training.java.quarkus.loan.adapter.out.persistence.utils.SortParser;
import pe.edu.galaxy.training.java.quarkus.loan.domain.model.LoanStatus;

import java.time.LocalDate;
import java.util.*;

@ApplicationScoped
public class LoanRequestRepository implements PanacheRepository<LoanRequestEntity> {

    private static final Set<String> SORTABLE_FIELDS = Set.of(
            PersistenceConstants.ID_FIELD,
            PersistenceConstants.REQUESTED_FROM_QUERY_FIELD,
            PersistenceConstants.REQUESTED_UNTIL_QUERY_FIELD,
            PersistenceConstants.CREATED_AT_FIELD
    );

    /**
     * docs/api.md §3 — GET /api/loans: filtros status/requesterId/from/until.
     * `from`/`until` acotan el rango [requestedFrom >= from, requestedUntil
     * <= until]. No pagina: el llamador aplica .page(...). `status` ya llega
     * parseado: el parseo de la cadena HTTP es responsabilidad de
     * adapter/in/rest (LoanRestMapper), no de la persistencia.
     */
    public PanacheQuery<LoanRequestEntity> search(LoanStatus status, Long requesterId, LocalDate from,
                                                  LocalDate until, String sort) {
        List<String> conditions = new ArrayList<>();
        Map<String, Object> params = new HashMap<>();

        QueryUtil.addConditionAndParamToQuerySearch(status, PaginationField.STATUS, conditions, params);
        QueryUtil.addConditionAndParamToQuerySearch(requesterId, PaginationField.REQUESTER_ID, conditions, params);
        QueryUtil.addConditionAndParamToQuerySearch(from, PaginationField.REQUESTED_FROM, conditions, params);
        QueryUtil.addConditionAndParamToQuerySearch(until, PaginationField.REQUESTED_UNTIL, conditions, params);

        Sort sortObj = SortParser.parse(sort, SORTABLE_FIELDS, PersistenceConstants.ID_FIELD);
        return conditions.isEmpty()
                ? findAll(sortObj)
                : find(String.join(" and ", conditions), sortObj, params);
    }
}
