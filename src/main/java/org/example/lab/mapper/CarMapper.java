package org.example.lab.mapper;
import jakarta.enterprise.context.ApplicationScoped;
import org.example.lab.dto.CarResponse;
import org.example.lab.model.Car;

@ApplicationScoped
public class CarMapper {
    public CarResponse toResponse(Car car) {
        if (car == null) {
            return null;
        }
        return new CarResponse(car.version, car.id, car.name, car.cool, car.color);
    }
}
