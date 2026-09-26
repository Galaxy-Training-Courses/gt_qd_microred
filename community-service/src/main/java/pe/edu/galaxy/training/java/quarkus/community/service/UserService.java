package pe.edu.galaxy.training.java.quarkus.community.service;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import pe.edu.galaxy.training.java.quarkus.community.common.PaginationProperties;
import pe.edu.galaxy.training.java.quarkus.community.common.PagingSupport;
import pe.edu.galaxy.training.java.quarkus.community.dto.PageResponse;
import pe.edu.galaxy.training.java.quarkus.community.dto.user.CreateUserRequest;
import pe.edu.galaxy.training.java.quarkus.community.dto.user.UpdateUserRequest;
import pe.edu.galaxy.training.java.quarkus.community.dto.user.UserResponse;
import pe.edu.galaxy.training.java.quarkus.community.entities.user.UserEntity;
import pe.edu.galaxy.training.java.quarkus.community.entities.user.UserStatus;
import pe.edu.galaxy.training.java.quarkus.community.exceptions.UserNotFoundException;
import pe.edu.galaxy.training.java.quarkus.community.exceptions.conflict.DuplicateEmailException;
import pe.edu.galaxy.training.java.quarkus.community.exceptions.conflict.UserHasReservedObjectsException;
import pe.edu.galaxy.training.java.quarkus.community.mapper.UserMapper;
import pe.edu.galaxy.training.java.quarkus.community.repository.ObjectRepository;
import pe.edu.galaxy.training.java.quarkus.community.repository.UserRepository;

import java.time.Instant;

@ApplicationScoped
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final ObjectRepository objectRepository;
    private final UserMapper userMapper;
    private final PaginationProperties paginationProperties;

    public PageResponse<UserResponse> findPage(int page, Integer size, String neighborhood, String status, String sort) {
        int resolvedSize = size != null ? size : paginationProperties.defaultSize();
        PagingSupport.validate(page, resolvedSize);
        PanacheQuery<UserEntity> query = userRepository.search(neighborhood, status, sort);
        return paginate(query, page, resolvedSize);
    }

    public UserResponse findById(Long id) {
        return userMapper.toResponse(getOrThrow(id));
    }

    @Transactional
    public UserResponse create(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateEmailException(request.email());
        }
        UserEntity entity = new UserEntity();
        entity.name = request.name();
        entity.email = request.email();
        entity.neighborhood = request.neighborhood();
        entity.status = UserStatus.ACTIVE;
        entity.createdAt = Instant.now();
        userRepository.persist(entity);
        return userMapper.toResponse(entity);
    }

    @Transactional
    public UserResponse patch(Long id, UpdateUserRequest request) {
        UserEntity entity = getOrThrow(id);
        if (request.neighborhood() != null) {
            entity.neighborhood = request.neighborhood();
        }
        if (request.status() != null) {
            entity.status = UserStatus.parseStatus(request.status());
        }
        return userMapper.toResponse(entity);
    }

    @Transactional
    public void delete(Long id) {
        UserEntity entity = getOrThrow(id);
        if (objectRepository.existsReservedByOwner(id)) {
            throw new UserHasReservedObjectsException(id);
        }
        userRepository.delete(entity);
    }

    UserEntity getOrThrow(Long id) {
        return userRepository.findByIdOptional(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    private PageResponse<UserResponse> paginate(PanacheQuery<UserEntity> query, int page, int size) {
        query.page(Page.of(page, size));
        var content = query.list().stream()
                .map(userMapper::toResponse)
                .toList();
        return new PageResponse<>(content, page, size, query.count(), query.pageCount());
    }
}
