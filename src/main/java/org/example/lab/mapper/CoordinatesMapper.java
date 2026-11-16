package org.example.lab.mapper;
import jakarta.enterprise.context.ApplicationScoped;
import org.example.lab.dto.CoordinatesResponse;
import org.example.lab.model.Coordinates;

@ApplicationScoped
public class CoordinatesMapper {
    public CoordinatesResponse toResponse(Coordinates coordinates) {
        return new CoordinatesResponse(coordinates.version, coordinates.id, coordinates.x.toString(), coordinates.y);
    }
}
