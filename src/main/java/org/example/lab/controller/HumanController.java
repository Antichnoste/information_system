package org.example.lab.controller;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import org.example.lab.dto.HumanRequest;
import org.example.lab.dto.HumanResponse;
import org.example.lab.dto.HumanPageResponse;
import org.example.lab.service.HumanService;
import org.example.lab.realtime.LiveUpdates;
import static org.example.lab.realtime.LiveUpdates.Resource.HUMANS;

@Path("humans")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class HumanController {
    @Inject
    HumanService humanService;
    @Inject
    LiveUpdates updates;

    @GET
    public HumanPageResponse humans(
            @DefaultValue("0") @QueryParam("page") int page,
            @DefaultValue("20") @QueryParam("size") int size,
            @DefaultValue("id") @QueryParam("sort") String sort,
            @DefaultValue("asc") @QueryParam("direction") String direction,
            @Context UriInfo uri) {
        Map<String, String> filters = new HashMap<>();
        for (String column : List.of("name", "soundtrackName", "carName", "carColor", "mood", "weaponType")) {
            filters.put(column, uri.getQueryParameters().getFirst(column));
        }
        return humanService.list(page, size, sort, direction, filters);
    }

    @GET
    @Path("{id}")
    public HumanResponse human(@PathParam("id") int id) {
        return humanService.get(id);
    }

    @POST
    public Response createHuman(@NotNull(message = "Тело запроса обязательно") @Valid HumanRequest input) {
        HumanResponse created = humanService.create(input);
        updates.publish(HUMANS);
        return Response.status(201)
                .entity(created)
                .build();
    }

    @PUT
    @Path("{id}")
    public HumanResponse updateHuman(@PathParam("id") int id, @NotNull(message = "Тело запроса обязательно") @Valid HumanRequest input) {
        HumanResponse updated = humanService.update(id, input);
        updates.publish(HUMANS);
        return updated;
    }

    @DELETE
    @Path("{id}")
    public void deleteHuman(@PathParam("id") int id) {
        humanService.delete(id);
        updates.publish(HUMANS);
    }
}
