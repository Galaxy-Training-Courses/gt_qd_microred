package pe.edu.galaxy.training.java.quarkus.community.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pe.edu.galaxy.training.java.quarkus.community.common.PersistenceConstants;
import pe.edu.galaxy.training.java.quarkus.community.dto.object.ObjectResponse;
import pe.edu.galaxy.training.java.quarkus.community.entities.object.ObjectEntity;

@Mapper(componentModel = "jakarta-cdi")
public interface ObjectMapper {

    @Mapping(source = PersistenceConstants.OWNER_ID_QUERY_FIELD, target = PersistenceConstants.OWNER_ID_FIELD)
    ObjectResponse toResponse(ObjectEntity entity);
}
