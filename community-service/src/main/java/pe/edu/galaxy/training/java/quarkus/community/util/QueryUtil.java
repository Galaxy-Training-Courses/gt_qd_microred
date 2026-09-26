package pe.edu.galaxy.training.java.quarkus.community.util;

import pe.edu.galaxy.training.java.quarkus.community.entities.PaginationField;

import java.util.List;
import java.util.Map;

public class QueryUtil {
    public static void addConditionAndParamToQuerySearch(String field,
                                                         PaginationField paginationField,
                                                         List<String> conditions,
                                                         Map<String, Object> params) {
        if (field != null && !field.isBlank()) {
            conditions.add(buildQueryCondition(paginationField));
            params.put(paginationField.getFieldName(), field);
        }
    }

    public static void addConditionAndParamToQuerySearch(Long field,
                                                         PaginationField paginationField,
                                                         List<String> conditions,
                                                         Map<String, Object> params) {
        if (field != null) {
            conditions.add(buildQueryCondition(paginationField));
            params.put(paginationField.getFieldName(), field);
        }
    }

    public static void addConditionAndParamToQuerySearch(Object field,
                                                         PaginationField paginationField,
                                                         List<String> conditions,
                                                         Map<String, Object> params) {
        if (field != null) {
            conditions.add(buildQueryCondition(paginationField));
            params.put(paginationField.getFieldName(), field);
        }
    }

    public static String buildQueryCondition(PaginationField paginationField) {
        return paginationField.getQueryFieldName() + " = :" + paginationField.getFieldName();
    }

    private QueryUtil() {}
}
