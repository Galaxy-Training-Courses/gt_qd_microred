package pe.edu.galaxy.training.java.quarkus.community.repository;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import pe.edu.galaxy.training.java.quarkus.community.common.PersistenceConstants;
import pe.edu.galaxy.training.java.quarkus.community.entities.PaginationField;
import pe.edu.galaxy.training.java.quarkus.community.entities.object.ObjectCondition;
import pe.edu.galaxy.training.java.quarkus.community.entities.object.ObjectEntity;
import pe.edu.galaxy.training.java.quarkus.community.entities.object.ObjectStatus;
import pe.edu.galaxy.training.java.quarkus.community.util.QueryUtil;

import java.util.*;

@ApplicationScoped
public class ObjectRepository implements PanacheRepository<ObjectEntity> {

    private static final Set<String> SORTABLE_FIELDS = Set.of(
            PersistenceConstants.ID_FIELD,
            PersistenceConstants.NAME_FIELD,
            PersistenceConstants.CREATED_AT_FIELD,
            PersistenceConstants.CATEGORY_FIELD
    );

    public PanacheQuery<ObjectEntity> search(String category, String condition, String status, Long ownerId,
                                             String sort) {
        List<String> conditions = new ArrayList<>();
        Map<String, Object> params = new HashMap<>();

        QueryUtil.addConditionAndParamToQuerySearch(
                category,
                PaginationField.CATEGORY,
                conditions,
                params);
        QueryUtil.addConditionAndParamToQuerySearch(
                condition != null && !condition.isBlank() ? ObjectCondition.parseCondition(condition) : null,
                PaginationField.CONDITION,
                conditions,
                params);
        QueryUtil.addConditionAndParamToQuerySearch(
                status != null && !status.isBlank() ? ObjectStatus.parseStatus(status) : null,
                PaginationField.STATUS,
                conditions,
                params);
        QueryUtil.addConditionAndParamToQuerySearch(
                ownerId,
                PaginationField.OWNER_ID,
                conditions,
                params);

        Sort sortObj = SortParser.parse(sort, SORTABLE_FIELDS, PersistenceConstants.ID_FIELD);
        return conditions.isEmpty()
                ? findAll(sortObj)
                : find(String.join(" and ", conditions), sortObj, params);
    }

    /** docs/api.md §1 — GET /api/users/{id}/objects, con los mismos filtros de paginación. */
    public PanacheQuery<ObjectEntity> searchByOwner(Long ownerId, String category, String condition, String status,
                                                    String sort) {
        return search(category, condition, status, ownerId, sort);
    }

    public boolean existsReservedByOwner(Long ownerId) {
        return count(
                PaginationField.OWNER_ID.getQueryFieldName() + " = :" + PaginationField.OWNER_ID.getFieldName() +
                        " and " +
                        PaginationField.STATUS.getQueryFieldName()  +" = :" + PaginationField.STATUS.getFieldName(),
                Map.of(PersistenceConstants.OWNER_ID_FIELD, ownerId, PersistenceConstants.STATUS_FIELD, ObjectStatus.RESERVED)
        ) > 0;
    }
}
