package pe.edu.galaxy.training.java.quarkus.community.resources;

import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import lombok.RequiredArgsConstructor;
import pe.edu.galaxy.training.java.quarkus.community.dto.PageResponse;
import pe.edu.galaxy.training.java.quarkus.community.dto.object.ObjectResponse;
import pe.edu.galaxy.training.java.quarkus.community.dto.user.CreateUserRequest;
import pe.edu.galaxy.training.java.quarkus.community.dto.user.UpdateUserRequest;
import pe.edu.galaxy.training.java.quarkus.community.dto.user.UserResponse;
import pe.edu.galaxy.training.java.quarkus.community.service.ObjectService;
import pe.edu.galaxy.training.java.quarkus.community.service.UserService;

@Path("/api/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
public class UserResource {
    private final UserService userService;
    private final ObjectService objectService;

    @GET
    public PageResponse<UserResponse> list(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") Integer size,
            @QueryParam("neighborhood") String neighborhood,
            @QueryParam("status") String status,
            @QueryParam("sort") String sort) {
        return userService.findPage(page, size, neighborhood, status, sort);
    }

    @GET
    @Path("/{id}")
    public UserResponse findById(@PathParam("id") Long id) {
        return userService.findById(id);
    }

    @POST
    public Response create(@Valid CreateUserRequest request, @Context UriInfo uriInfo) {
        UserResponse created = userService.create(request);
        return Response
                .created(uriInfo
                        .getAbsolutePathBuilder()
                        .path(String.valueOf(created.id()))
                        .build())
                .entity(created)
                .build();
    }

    @PATCH
    @Path("/{id}")
    public UserResponse patch(@PathParam("id") Long id, @Valid UpdateUserRequest request) {
        return userService.patch(id, request);
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id) {
        userService.delete(id);
        return Response.noContent().build();
    }

    @GET
    @Path("/{id}/objects")
    public PageResponse<ObjectResponse> objectsByOwner(
            @PathParam("id") Long id,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") Integer size,
            @QueryParam("category") String category,
            @QueryParam("condition") String condition,
            @QueryParam("status") String status,
            @QueryParam("sort") String sort) {
        return objectService.findByOwner(id, page, size, category, condition, status, sort);
    }
}
