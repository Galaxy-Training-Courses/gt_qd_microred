package pe.edu.galaxy.training.java.quarkus.loan.adapter.out.persistence.utils;

import io.quarkus.panache.common.Sort;
import pe.edu.galaxy.training.java.quarkus.loan.domain.exception.InvalidFieldValueException;

import java.util.Set;

/**
 * Traduce el parámetro `sort` (p.ej. "createdAt" o "createdAt,desc") a un
 * Sort de Panache, validando contra una lista blanca
 */
public final class SortParser {

    private SortParser() {
    }

    public static Sort parse(String sort, Set<String> allowedFields, String defaultField) {
        if (sort == null || sort.isBlank()) {
            return Sort.by(defaultField);
        }
        boolean descending = sort.endsWith(",desc");
        String field = descending ? sort.substring(0, sort.length() - ",desc".length()) : sort;
        if (!allowedFields.contains(field)) {
            throw InvalidFieldValueException.invalidEnum("sort", sort);
        }
        return descending ? Sort.by(field).descending() : Sort.by(field);
    }
}
