package org.example.lab.controller;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import org.example.lab.dto.CoordinatesRequest;
import org.example.lab.dto.CoordinatesResponse;
import org.example.lab.service.CoordinatesService;
import org.example.lab.realtime.LiveUpdates;
import static org.example.lab.realtime.LiveUpdates.Resource.COORDINATES;
import static org.example.lab.realtime.LiveUpdates.Resource.HUMANS;

@Path("coordinates")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CoordinatesController {
    @Inject
    CoordinatesService coordinatesService;
    @Inject
    LiveUpdates updates;

    @GET
    public List<CoordinatesResponse> coordinates() {
        return coordinatesService.list();
    }

    @GET
    @Path("{id}")
    public CoordinatesResponse coordinates(@PathParam("id") long id) {
        return coordinatesService.get(id);
    }

    @POST
    public Response createCoordinates(@NotNull(message = "Тело запроса обязательно") @Valid CoordinatesRequest input) {
        CoordinatesResponse created = coordinatesService.create(input);
        updates.publish(COORDINATES);
        return Response.status(201)
                .entity(created)
                .build();
    }

    @PUT
    @Path("{id}")
    public CoordinatesResponse updateCoordinates(
            @PathParam("id") long id,
            @NotNull(message = "Тело запроса обязательно") @Valid CoordinatesRequest input) {
        CoordinatesResponse updated = coordinatesService.update(id, input);
        updates.publish(COORDINATES, HUMANS);
        return updated;
    }

    @DELETE
    @Path("{id}")
    public void deleteCoordinates(
            @PathParam("id") long id,
            @QueryParam("replacementId") Long replacement) {
        coordinatesService.delete(id, replacement);
        updates.publish(COORDINATES, HUMANS);
    }
}
