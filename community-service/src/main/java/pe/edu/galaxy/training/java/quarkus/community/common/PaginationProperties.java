package pe.edu.galaxy.training.java.quarkus.community.common;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Parámetro de negocio propio externalizado.
 */
@ApplicationScoped
public class PaginationProperties {

    @ConfigProperty(name = "microred.community.pagination.default-size", defaultValue = "" + PagingSupport.DEFAULT_SIZE)
    int defaultSize;

    public int defaultSize() {
        return defaultSize;
    }
}
