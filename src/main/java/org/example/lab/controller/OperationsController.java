package org.example.lab.controller;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import org.example.lab.model.WeaponType;
import org.example.lab.dto.HumanResponse;
import org.example.lab.dto.OperationResponse;
import org.example.lab.service.OperationsService;
import org.example.lab.realtime.LiveUpdates;
import static org.example.lab.realtime.LiveUpdates.Resource.CARS;
import static org.example.lab.realtime.LiveUpdates.Resource.HUMANS;

@Path("operations")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OperationsController {
    @Inject
    OperationsService operationsService;
    @Inject
    LiveUpdates updates;

    @DELETE
    @Path("by-weapon")
    public OperationResponse weapon(@QueryParam("weaponType") WeaponType weapon) {
        OperationResponse result = operationsService.deleteByWeapon(weapon);
        if (result.getCount() > 0) updates.publish(HUMANS);
        return result;
    }

    @GET
    @Path("minimum-waiting")
    public Response minimum() {
        HumanResponse result = operationsService.minimumWaiting();
        return result == null
                ? Response.noContent().build()
                : Response.ok(result).build();
    }

    @GET
    @Path("soundtrack")
    public List<HumanResponse> soundtrack(@QueryParam("substring") String substring) {
        return operationsService.soundtrack(substring);
    }

    @POST
    @Path("sadden")
    public OperationResponse sadden() {
        OperationResponse result = operationsService.sadden();
        if (result.getCount() > 0) updates.publish(HUMANS);
        return result;
    }

    @POST
    @Path("give-cars")
    public OperationResponse giveCars() {
        OperationResponse result = operationsService.giveCars();
        if (result.getCount() > 0) updates.publish(CARS, HUMANS);
        return result;
    }
}
