package org.example.lab.service;
import org.example.lab.exception.GlobalExceptionHandler;
import jakarta.ws.rs.WebApplicationException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import org.example.lab.model.Coordinates;
import org.example.lab.model.HumanBeing;
import org.example.lab.dto.CoordinatesRequest;
import org.example.lab.dto.CoordinatesResponse;
import org.example.lab.mapper.CoordinatesMapper;
import org.example.lab.repository.HumanRepository;
import org.example.lab.repository.CoordinatesRepository;
import org.example.lab.realtime.UpdateResource;

@ApplicationScoped
@Transactional
public class CoordinatesService {
    @Inject
    HumanRepository humanRepository;
    @Inject
    CoordinatesRepository coordinatesRepository;
    @Inject
    CoordinatesMapper mapper;
    @Inject
    Event<UpdateResource> changes;

    public List<CoordinatesResponse> list() {
        List<CoordinatesResponse> result = new ArrayList<>();
        for (Coordinates coordinates : coordinatesRepository.findAll()) {
            result.add(mapper.toResponse(coordinates));
        }
        return result;
    }

    public CoordinatesResponse get(long id) {
        return mapper.toResponse(coordinatesRepository.findById(id));
    }

    public CoordinatesResponse create(CoordinatesRequest input) {
        coordinatesRepository.useSerializableIsolation();
        requireFreePoint(input.getX(), input.getY(), null);
        Coordinates coordinates = createPoint(input.getX(), input.getY());
        changes.fire(UpdateResource.COORDINATES);
        return mapper.toResponse(coordinates);
    }

    public CoordinatesResponse update(long id, CoordinatesRequest input) {
        coordinatesRepository.useSerializableIsolation();
        Coordinates coordinates = coordinatesRepository.findById(id);
        if (input.getVersion() == null) {
            throw new WebApplicationException(GlobalExceptionHandler.response(400, "Укажите версию объекта"));
        }
        if (!input.getVersion().equals(coordinates.version)) {
            throw new WebApplicationException(GlobalExceptionHandler.response(409, "Объект уже изменён другим пользователем. Закройте форму и откройте её заново."));
        }
        requireFreePoint(input.getX(), input.getY(), coordinates.id);
        coordinates.x = input.getX();
        coordinates.y = input.getY();
        coordinatesRepository.flush();
        changes.fire(UpdateResource.COORDINATES);
        changes.fire(UpdateResource.HUMANS);
        return mapper.toResponse(coordinates);
    }

    public Coordinates findOrCreate(Long x, Float y) {
        Coordinates coordinates = coordinatesRepository.findByValues(x, y);
        return (coordinates == null) ? createPoint(x, y) : coordinates;
    }

    public void delete(long id, Long replacementId, Long expectedVersion) {
        coordinatesRepository.useSerializableIsolation();
        Coordinates coordinates = coordinatesRepository.findById(id);
        if (expectedVersion == null) {
            throw new WebApplicationException(GlobalExceptionHandler.response(400, "Укажите версию объекта"));
        }
        if (!expectedVersion.equals(coordinates.version)) {
            throw new WebApplicationException(GlobalExceptionHandler.response(409,
                    "Объект уже изменён другим пользователем. Закройте форму и откройте её заново."));
        }
        if (replacementId != null && replacementId == id) {
            throw new WebApplicationException(GlobalExceptionHandler.response(400, "Выберите другие координаты"));
        }
        List<HumanBeing> humans = humanRepository.findByCoordinatesId(id);
        if (!humans.isEmpty() && replacementId == null) {
            throw new WebApplicationException(GlobalExceptionHandler.response(409, "Координаты используются. Выберите замену."));
        }
        Coordinates replacement = replacementId == null ? null : coordinatesRepository.findById(replacementId);
        for (HumanBeing human : humans) {
            human.coordinates = replacement;
        }
        coordinatesRepository.delete(coordinates);
        coordinatesRepository.flush();
        changes.fire(UpdateResource.COORDINATES);
        changes.fire(UpdateResource.HUMANS);
    }

    private void requireFreePoint(Long x, Float y, Long currentId) {
        Coordinates existing = coordinatesRepository.findByValues(x, y);
        if (existing != null && (currentId == null || !existing.id.equals(currentId))) {
            throw new WebApplicationException(GlobalExceptionHandler.response(409, "Точка с такими координатами уже существует"));
        }
    }

    private Coordinates createPoint(Long x, Float y) {
        Coordinates coordinates = new Coordinates();
        coordinates.x = x;
        coordinates.y = y;
        coordinatesRepository.save(coordinates);
        return coordinates;
    }
}
