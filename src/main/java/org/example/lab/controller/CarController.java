package org.example.lab.controller;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import org.example.lab.dto.CarRequest;
import org.example.lab.dto.CarResponse;
import org.example.lab.service.CarService;
import org.example.lab.realtime.LiveUpdates;
import static org.example.lab.realtime.LiveUpdates.Resource.CARS;
import static org.example.lab.realtime.LiveUpdates.Resource.HUMANS;

@Path("cars")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CarController {
    @Inject
    CarService carService;
    @Inject
    LiveUpdates updates;

    @GET
    public List<CarResponse> cars() {
        return carService.list();
    }

    @GET
    @Path("{id}")
    public CarResponse car(@PathParam("id") long id) {
        return carService.get(id);
    }

    @POST
    public Response createCar(@NotNull(message = "Тело запроса обязательно") @Valid CarRequest input) {
        CarResponse created = carService.create(input);
        updates.publish(CARS);
        return Response.status(201)
                .entity(created)
                .build();
    }

    @PUT
    @Path("{id}")
    public CarResponse updateCar( 
            @PathParam("id") long id, 
            @NotNull(message = "Тело запроса обязательно") @Valid CarRequest input) {

        CarResponse updated = carService.update(id, input);
        updates.publish(CARS, HUMANS);
        return updated;
    }

    @DELETE
    @Path("{id}")
    public void deleteCar(
            @PathParam("id") long id,
            @QueryParam("replacementId") Long replacement) {

        carService.delete(id, replacement);
        updates.publish(CARS, HUMANS);
    }
}
