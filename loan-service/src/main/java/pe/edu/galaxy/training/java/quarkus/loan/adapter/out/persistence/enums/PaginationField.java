package pe.edu.galaxy.training.java.quarkus.loan.adapter.out.persistence.enums;

import lombok.Getter;
import pe.edu.galaxy.training.java.quarkus.loan.adapter.out.persistence.utils.PersistenceConstants;

@Getter
public enum PaginationField {
    STATUS(PersistenceConstants.STATUS_FIELD),
    REQUESTER_ID(PersistenceConstants.REQUESTER_ID_FIELD),
    REQUESTED_FROM(PersistenceConstants.REQUESTED_FROM_FIELD, PersistenceConstants.REQUESTED_FROM_QUERY_FIELD),
    REQUESTED_UNTIL(PersistenceConstants.REQUESTED_UNTIL_FIELD, PersistenceConstants.REQUESTED_UNTIL_QUERY_FIELD);

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
