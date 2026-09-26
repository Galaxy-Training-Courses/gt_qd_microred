package pe.edu.galaxy.training.java.quarkus.community.service;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import pe.edu.galaxy.training.java.quarkus.community.common.PaginationProperties;
import pe.edu.galaxy.training.java.quarkus.community.common.PagingSupport;
import pe.edu.galaxy.training.java.quarkus.community.dto.*;
import pe.edu.galaxy.training.java.quarkus.community.dto.object.CreateObjectRequest;
import pe.edu.galaxy.training.java.quarkus.community.dto.object.ObjectResponse;
import pe.edu.galaxy.training.java.quarkus.community.dto.object.PatchObjectRequest;
import pe.edu.galaxy.training.java.quarkus.community.dto.object.UpdateObjectRequest;
import pe.edu.galaxy.training.java.quarkus.community.dto.user.UserResponse;
import pe.edu.galaxy.training.java.quarkus.community.entities.object.ObjectCondition;
import pe.edu.galaxy.training.java.quarkus.community.entities.object.ObjectEntity;
import pe.edu.galaxy.training.java.quarkus.community.entities.object.ObjectStatus;
import pe.edu.galaxy.training.java.quarkus.community.entities.user.UserEntity;
import pe.edu.galaxy.training.java.quarkus.community.exceptions.ObjectNotFoundException;
import pe.edu.galaxy.training.java.quarkus.community.exceptions.UserNotFoundException;
import pe.edu.galaxy.training.java.quarkus.community.exceptions.conflict.ObjectReservedException;
import pe.edu.galaxy.training.java.quarkus.community.mapper.ObjectMapper;
import pe.edu.galaxy.training.java.quarkus.community.mapper.UserMapper;
import pe.edu.galaxy.training.java.quarkus.community.repository.ObjectRepository;
import pe.edu.galaxy.training.java.quarkus.community.repository.UserRepository;

import java.time.Instant;

@ApplicationScoped
@RequiredArgsConstructor
public class ObjectService {
    private final ObjectRepository objectRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final UserMapper userMapper;
    private final PaginationProperties paginationProperties;

    public PageResponse<ObjectResponse> findPage(int page, Integer size, String category, String condition,
                                                 String status, Long ownerId, String sort) {
        int resolvedSize = size != null ? size : paginationProperties.defaultSize();
        PagingSupport.validate(page, resolvedSize);
        PanacheQuery<ObjectEntity> query = objectRepository.search(category, condition, status, ownerId, sort);
        return paginate(query, page, resolvedSize);
    }

    public ObjectResponse findById(Long id) {
        return objectMapper.toResponse(getOrThrow(id));
    }

    public PageResponse<ObjectResponse> findByOwner(Long ownerId, int page, Integer size, String category,
                                                     String condition, String status, String sort) {
        if (userRepository.findByIdOptional(ownerId).isEmpty()) {
            throw new UserNotFoundException(ownerId);
        }
        int resolvedSize = size != null ? size : paginationProperties.defaultSize();
        PagingSupport.validate(page, resolvedSize);
        PanacheQuery<ObjectEntity> query = objectRepository.searchByOwner(ownerId, category, condition, status, sort);
        return paginate(query, page, resolvedSize);
    }

    public UserResponse findOwner(Long objectId) {
        ObjectEntity entity = getOrThrow(objectId);
        UserEntity owner = userRepository.findByIdOptional(entity.owner.id)
                .orElseThrow(() -> new UserNotFoundException(entity.owner.id));
        return userMapper.toResponse(owner);
    }

    @Transactional
    public ObjectResponse create(CreateObjectRequest request) {
        UserEntity owner = userRepository.findByIdOptional(request.ownerId())
                .orElseThrow(() -> new UserNotFoundException(request.ownerId()));

        ObjectEntity entity = new ObjectEntity();
        entity.owner = owner;
        entity.name = request.name();
        entity.description = request.description();
        entity.category = request.category();
        entity.condition = ObjectCondition.parseCondition(request.condition());
        entity.status = ObjectStatus.AVAILABLE;
        entity.createdAt = Instant.now();
        objectRepository.persist(entity);
        return objectMapper.toResponse(entity);
    }

    @Transactional
    public ObjectResponse replace(Long id, UpdateObjectRequest request) {
        ObjectEntity entity = getOrThrow(id);
        entity.name = request.name();
        entity.description = request.description();
        entity.category = request.category();
        entity.condition = ObjectCondition.parseCondition(request.condition());
        return objectMapper.toResponse(entity);
    }

    @Transactional
    public ObjectResponse patchStatus(Long id, PatchObjectRequest request) {
        ObjectEntity entity = getOrThrow(id);
        if (request.status() != null) {
            entity.status = ObjectStatus.parseStatus(request.status());
        }
        return objectMapper.toResponse(entity);
    }

    @Transactional
    public void delete(Long id) {
        ObjectEntity entity = getOrThrow(id);
        if (entity.status == ObjectStatus.RESERVED) {
            throw new ObjectReservedException(id);
        }
        objectRepository.delete(entity);
    }

    ObjectEntity getOrThrow(Long id) {
        return objectRepository.findByIdOptional(id).orElseThrow(() -> new ObjectNotFoundException(id));
    }

    private PageResponse<ObjectResponse> paginate(PanacheQuery<ObjectEntity> query, int page, int size) {
        query.page(Page.of(page, size));
        var content = query.list().stream()
                .map(objectMapper::toResponse)
                .toList();
        return new PageResponse<>(content, page, size, query.count(), query.pageCount());
    }
}
