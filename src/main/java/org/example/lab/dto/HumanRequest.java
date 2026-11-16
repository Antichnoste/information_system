package org.example.lab.dto;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.example.lab.model.Mood;
import org.example.lab.model.WeaponType;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
public class HumanRequest {
    private Long version;

    @NotEmpty(message = "Имя не должно быть пустым")
    private String name;

    @NotNull(message = "Выберите координаты")
    private Long coordinatesId;

    @NotNull(message = "Укажите, является ли персонаж героем")
    private Boolean realHero;

    @NotNull(message = "Укажите наличие зубочистки")
    private Boolean hasToothpick;

    private Long carId;
    private Mood mood;

    @NotNull(message = "Укажите скорость удара")
    private Long impactSpeed;

    @NotNull(message = "Укажите саундтрек")
    private String soundtrackName;

    private Long minutesOfWaiting;
    
    @NotNull(message = "Выберите оружие")
    private WeaponType weaponType;
}
