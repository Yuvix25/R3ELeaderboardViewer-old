package com.example.r3eleaderboardviewer;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class R3ECarOrClass {
    public final R3ECar car;
    public final R3EClass carClass;
    public final R3EClassGroup carClassGroup;
    public final int type;

    public R3ECarOrClass(R3ECar car) {
        this.car = car;
        this.carClass = null;
        this.carClassGroup = null;
        this.type = 0;
    }

    public R3ECarOrClass(R3EClass carClass) {
        this.car = null;
        this.carClass = carClass;
        this.carClassGroup = null;
        this.type = 1;
    }

    public R3ECarOrClass(R3EClassGroup carClassGroup) {
        this.car = null;
        this.carClass = null;
        this.carClassGroup = carClassGroup;
        this.type = 2;
    }

    public String getCarName() {
        if (type >= 1) {
            return "";
        } else {
            return car.name;
        }
    }

    public String[] getId() {
        if (type == 0) {
            return new String[]{car.id};
        } else if (type == 1) {
            return new String[]{carClass.id};
        } else {
            return carClassGroup.classes.values().stream().map(c -> c.id).toArray(String[]::new);
        }
    }

    public String getIcon() {
        if (type == 0) {
            return car.getIcon().toString();
        } else if (type == 1) {
            return carClass.icon.toString();
        } else {
            return Integer.toString(carClassGroup.iconDrawableId);
        }
    }

    public String getClassName() {
        if (type == 0) {
            return car.carClass.name;
        } else if (type == 1) {
            return carClass.name;
        } else {
            return carClassGroup.name;
        }
    }

    public R3EClass getCarClass() {
        if (type == 0) {
            return car.carClass;
        } else if (type == 1) {
            return carClass;
        } else {
            return null;
        }
    }

    public Collection<R3ECar> getCars() {
        if (type == 0) {
            return Arrays.asList(car);
        } else if (type == 1) {
            return carClass.cars.values();
        } else {
            List<R3ECar> cars = new ArrayList<>();
            for (R3EClass carClass : carClassGroup.classes.values()) {
                cars.addAll(carClass.cars.values());
            }
            return cars;
        }
    }

    public Collection<R3EClass> getClasses() {
        if (type == 0) {
            return Arrays.asList(car.carClass);
        } else if (type == 1) {
            return Arrays.asList(carClass);
        } else {
            return carClassGroup.classes.values();
        }
    }
}
