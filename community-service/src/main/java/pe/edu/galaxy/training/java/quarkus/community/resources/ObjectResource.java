package pe.edu.galaxy.training.java.quarkus.community.resources;

import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import lombok.RequiredArgsConstructor;
import pe.edu.galaxy.training.java.quarkus.community.dto.PageResponse;
import pe.edu.galaxy.training.java.quarkus.community.dto.object.CreateObjectRequest;
import pe.edu.galaxy.training.java.quarkus.community.dto.object.ObjectResponse;
import pe.edu.galaxy.training.java.quarkus.community.dto.object.PatchObjectRequest;
import pe.edu.galaxy.training.java.quarkus.community.dto.object.UpdateObjectRequest;
import pe.edu.galaxy.training.java.quarkus.community.dto.user.UserResponse;
import pe.edu.galaxy.training.java.quarkus.community.service.ObjectService;

@Path("/api/objects")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
public class ObjectResource {
    private final ObjectService objectService;

    @GET
    public PageResponse<ObjectResponse> list(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") Integer size,
            @QueryParam("category") String category,
            @QueryParam("condition") String condition,
            @QueryParam("status") String status,
            @QueryParam("ownerId") Long ownerId,
            @QueryParam("sort") String sort) {
        return objectService.findPage(page, size, category, condition, status, ownerId, sort);
    }

    @GET
    @Path("/{id}")
    public ObjectResponse findById(@PathParam("id") Long id) {
        return objectService.findById(id);
    }

    @POST
    public Response create(@Valid CreateObjectRequest request, @Context UriInfo uriInfo) {
        ObjectResponse created = objectService.create(request);
        return Response
                .created(uriInfo
                        .getAbsolutePathBuilder()
                        .path(String.valueOf(created.id()))
                        .build())
                .entity(created)
                .build();
    }

    @PUT
    @Path("/{id}")
    public ObjectResponse replace(@PathParam("id") Long id, @Valid UpdateObjectRequest request) {
        return objectService.replace(id, request);
    }

    @PATCH
    @Path("/{id}")
    public ObjectResponse patchStatus(@PathParam("id") Long id, PatchObjectRequest request) {
        return objectService.patchStatus(id, request);
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id) {
        objectService.delete(id);
        return Response.noContent().build();
    }

    @GET
    @Path("/{id}/owner")
    public UserResponse owner(@PathParam("id") Long id) {
        return objectService.findOwner(id);
    }
}
