package pe.edu.galaxy.training.java.quarkus.community.repository;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import pe.edu.galaxy.training.java.quarkus.community.common.PersistenceConstants;
import pe.edu.galaxy.training.java.quarkus.community.entities.PaginationField;
import pe.edu.galaxy.training.java.quarkus.community.entities.object.ObjectStatus;
import pe.edu.galaxy.training.java.quarkus.community.entities.user.UserEntity;
import pe.edu.galaxy.training.java.quarkus.community.entities.user.UserStatus;
import pe.edu.galaxy.training.java.quarkus.community.exceptions.InvalidFieldValueException;
import pe.edu.galaxy.training.java.quarkus.community.util.QueryUtil;

import java.util.*;

@ApplicationScoped
public class UserRepository implements PanacheRepository<UserEntity> {

    private static final Set<String> SORTABLE_FIELDS = Set.of(
            PersistenceConstants.ID_FIELD,
            PersistenceConstants.NAME_FIELD,
            PersistenceConstants.CREATED_AT_FIELD,
            PersistenceConstants.NEIGHBORHOOD_FIELD);

    public Optional<UserEntity> findByEmail(String email) {
        return find(PersistenceConstants.EMAIL_FIELD, email).firstResultOptional();
    }

    public boolean existsByEmail(String email) {
        return count(PersistenceConstants.EMAIL_FIELD, email) > 0;
    }

    public PanacheQuery<UserEntity> search(String neighborhood, String status, String sort) {
        List<String> conditions = new ArrayList<>();
        Map<String, Object> params = new HashMap<>();

        QueryUtil.addConditionAndParamToQuerySearch(
                neighborhood,
                PaginationField.NEIGHBORHOOD,
                conditions,
                params);
        QueryUtil.addConditionAndParamToQuerySearch(
                status != null && !status.isBlank() ? ObjectStatus.parseStatus(status) : null,
                PaginationField.STATUS,
                conditions,
                params);

        Sort sortObj = SortParser.parse(sort, SORTABLE_FIELDS, PersistenceConstants.ID_FIELD);
        return conditions.isEmpty()
                ? findAll(sortObj)
                : find(String.join(" and ", conditions), sortObj, params);
    }

    private UserStatus parseStatus(String status) {
        try {
            return UserStatus.valueOf(status);
        } catch (IllegalArgumentException _) {
            throw InvalidFieldValueException.invalidEnum(PersistenceConstants.STATUS_FIELD, status);
        }
    }
}
