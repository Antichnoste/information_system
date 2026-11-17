package org.example.lab.service;
import org.example.lab.exception.GlobalExceptionHandler;
import jakarta.ws.rs.WebApplicationException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import org.example.lab.model.Car;
import org.example.lab.model.HumanBeing;
import org.example.lab.dto.CarRequest;
import org.example.lab.dto.CarResponse;
import org.example.lab.mapper.CarMapper;
import org.example.lab.repository.HumanRepository;
import org.example.lab.repository.CarRepository;
import org.example.lab.realtime.UpdateResource;

@ApplicationScoped
@Transactional
public class CarService {
    @Inject
    HumanRepository humanRepository;
    @Inject
    CarRepository carRepository;
    @Inject
    CarMapper mapper;
    @Inject
    Event<UpdateResource> changes;

    public List<CarResponse> list() {
        List<CarResponse> result = new ArrayList<>();
        for (Car car : carRepository.findAll()) {
            result.add(mapper.toResponse(car));
        }
        return result;
    }

    public CarResponse get(long id) {
        return mapper.toResponse(carRepository.findById(id));
    }

    public CarResponse create(CarRequest input) {
        Car car = new Car();
        car.name = input.getName();
        car.cool = input.getCool();
        car.color = input.getColor();
        carRepository.save(car);
        changes.fire(UpdateResource.CARS);
        return mapper.toResponse(car);
    }

    public CarResponse update(long id, CarRequest input) {
        Car car = carRepository.findById(id);
        if (input.getVersion() == null) {
            throw new WebApplicationException(GlobalExceptionHandler.response(400, "Укажите версию объекта"));
        }
        if (!input.getVersion().equals(car.version)) {
            throw new WebApplicationException(GlobalExceptionHandler.response(409,
                    "Объект уже изменён другим пользователем. Закройте форму и откройте её заново."));
        }
        car.name = input.getName();
        car.cool = input.getCool();
        car.color = input.getColor();
        carRepository.flush();
        changes.fire(UpdateResource.CARS);
        changes.fire(UpdateResource.HUMANS);
        return mapper.toResponse(car);
    }

    public void delete(long id, Long replacementId, Long expectedVersion) {
        Car car = carRepository.findById(id);
        if (expectedVersion == null) {
            throw new WebApplicationException(GlobalExceptionHandler.response(400, "Укажите версию объекта"));
        }
        if (!expectedVersion.equals(car.version)) {
            throw new WebApplicationException(GlobalExceptionHandler.response(409,
                    "Объект уже изменён другим пользователем. Закройте форму и откройте её заново."));
        }
        if (replacementId != null && replacementId == id) {
            throw new WebApplicationException(GlobalExceptionHandler.response(400, "Выберите другой автомобиль"));
        }
        List<HumanBeing> humans = humanRepository.findByCarId(id);
        if (!humans.isEmpty() && replacementId == null) {
            throw new WebApplicationException(GlobalExceptionHandler.response(409, "Автомобиль используется. Выберите замену."));
        }
        Car replacement = replacementId == null ? null : carRepository.findById(replacementId);
        for (HumanBeing human : humans) {
            human.car = replacement;
        }
        carRepository.delete(car);
        carRepository.flush();
        changes.fire(UpdateResource.CARS);
        changes.fire(UpdateResource.HUMANS);
    }
}
