package pe.edu.galaxy.training.java.quarkus.community.entities;

import lombok.Getter;
import pe.edu.galaxy.training.java.quarkus.community.common.PersistenceConstants;

@Getter
public enum PaginationField {
    CATEGORY(PersistenceConstants.CATEGORY_FIELD),
    CONDITION(PersistenceConstants.CONDITION_FIELD),
    STATUS(PersistenceConstants.STATUS_FIELD),
    OWNER_ID(PersistenceConstants.OWNER_ID_FIELD, PersistenceConstants.OWNER_ID_QUERY_FIELD),
    NEIGHBORHOOD(PersistenceConstants.NEIGHBORHOOD_FIELD);

    private final String fieldName;
    private final String queryFieldName;

    PaginationField(String fieldName) {
        this.fieldName = fieldName;
        this.queryFieldName = fieldName;
    }

    PaginationField(String fieldName, String queryFieldName) {
        this.fieldName = fieldName;
        this.queryFieldName = queryFieldName;
    }
}
