package pe.edu.galaxy.training.java.quarkus.community.mapper;

import org.mapstruct.Mapper;
import pe.edu.galaxy.training.java.quarkus.community.dto.user.UserResponse;
import pe.edu.galaxy.training.java.quarkus.community.entities.user.UserEntity;

/**
 * Entity <-> DTO. MapStruct convierte UserStatus <-> String
 * de forma implícita (conversión estándar enum <-> String).
 */
@Mapper(componentModel = "jakarta-cdi")
public interface UserMapper {

    UserResponse toResponse(UserEntity entity);
}
