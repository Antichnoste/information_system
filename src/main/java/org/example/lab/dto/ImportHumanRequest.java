package org.example.lab.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.example.lab.model.Mood;
import org.example.lab.model.WeaponType;

public class ImportHumanRequest {
    @NotEmpty(message = "имя не должно быть пустым")
    @Size(max = 255)
    public String name;

    @Valid
    @NotNull(message = "укажите координаты")
    public Point coordinates;

    @NotNull(message = "укажите, является ли персонаж героем")
    public Boolean realHero;

    @NotNull(message = "укажите наличие зубочистки")
    public Boolean hasToothpick;

    @Valid
    public CarData car;
    public Mood mood;

    @NotNull(message = "укажите скорость удара")
    public Long impactSpeed;

    @NotNull(message = "укажите саундтрек")
    @Size(max = 255)
    public String soundtrackName;
    public Long minutesOfWaiting;

    @NotNull(message = "выберите оружие")
    public WeaponType weaponType;

    public static class Point {
        @NotNull(message = "укажите координату x")
        public Long x;

        @NotNull(message = "укажите координату y")
        @DecimalMin(value = "-629", inclusive = false, message = "координата y должна быть больше -629")
        public Float y;
    }

    public static class CarData {
        @Size(max = 255)
        public String name;

        @NotNull(message = "укажите, крутой ли автомобиль")
        public Boolean cool;

        @Size(max = 255)
        public String color;
    }
}
