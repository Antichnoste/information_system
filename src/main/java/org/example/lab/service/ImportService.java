package org.example.lab.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import java.util.ArrayList;
import java.util.List;
import org.example.lab.dto.ImportHistoryPageResponse;
import org.example.lab.dto.ImportHistoryResponse;
import org.example.lab.dto.ImportHumanRequest;
import org.example.lab.exception.GlobalExceptionHandler;
import org.example.lab.model.AppUser;
import org.example.lab.model.Car;
import org.example.lab.model.HumanBeing;
import org.example.lab.model.ImportHistory;
import org.example.lab.model.ImportStatus;
import org.example.lab.model.UserRole;
import org.example.lab.realtime.UpdateResource;
import org.example.lab.repository.AppUserRepository;
import org.example.lab.repository.CarRepository;
import org.example.lab.repository.HumanRepository;
import org.example.lab.repository.ImportHistoryRepository;

@ApplicationScoped
@Transactional
public class ImportService {
    @Inject
    AppUserRepository users;
    @Inject
    CoordinatesService coordinates;
    @Inject
    CarRepository cars;
    @Inject
    HumanRepository humans;
    @Inject
    ImportHistoryRepository historyRepository;
    @Inject
    Event<UpdateResource> changes;

    public ImportHistoryPageResponse history(String username, int page, int size) {
        if (page < 0 || size < 1 || size > 100 || (long) page * size > Integer.MAX_VALUE) {
            throw new WebApplicationException(GlobalExceptionHandler.response(400,
                    "Номер страницы должен быть от 0, размер — от 1 до 100, смещение — не больше 2147483647"));
        }

        AppUser user = requireUser(username);
        Long userId = (user.getRole() == UserRole.ADMIN) ? null : user.getId();
        long total = historyRepository.count(userId);
        List<ImportHistoryResponse> items = new ArrayList<>();
        if ((long) page * size < total) {
            for (ImportHistory history : historyRepository.findPage(userId, page, size)) {
                items.add(new ImportHistoryResponse(
                        history.id,
                        history.status,
                        history.user.getUsername(),
                        history.status == ImportStatus.SUCCESS ? history.addedCount : null,
                        history.status == ImportStatus.FAILED ? history.errorMessage : null));
            }
        }
        return new ImportHistoryPageResponse(items, total, page, size);
    }

    public ImportHistory importHumans(String username, List<ImportHumanRequest> records) {
        AppUser user = requireUser(username);
        for (ImportHumanRequest input : records) {
            HumanBeing human = new HumanBeing();
            human.name = input.name;
            human.coordinates = coordinates.findOrCreate(input.coordinates.x, input.coordinates.y);
            human.realHero = input.realHero;
            human.hasToothpick = input.hasToothpick;
            if (input.car != null) {
                Car car = new Car();
                car.name = input.car.name;
                car.cool = input.car.cool;
                car.color = input.car.color;
                cars.save(car);
                human.car = car;
            }
            human.mood = input.mood;
            human.impactSpeed = input.impactSpeed;
            human.soundtrackName = input.soundtrackName;
            human.minutesOfWaiting = input.minutesOfWaiting;
            human.weaponType = input.weaponType;
            humans.save(human);
        }

        ImportHistory history = new ImportHistory();
        history.user = user;
        history.status = ImportStatus.SUCCESS;
        history.addedCount = records.size();
        historyRepository.save(history);
        changes.fire(UpdateResource.ALL);
        return history;
    }

    public void recordFailure(String username, String message) {
        ImportHistory history = new ImportHistory();
        history.user = requireUser(username);
        history.status = ImportStatus.FAILED;
        history.errorMessage = message.substring(0, Math.min(message.length(), 255));
        historyRepository.save(history);
    }

    private AppUser requireUser(String username) {
        AppUser user = users.findByUsername(username);
        if (user == null) {
            throw new WebApplicationException(GlobalExceptionHandler.response(401, "Войдите в систему"));
        }
        return user;
    }
}
