package com.example.r3eleaderboardviewer;

import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class R3ECarOrClass {
    public final R3ECar car;
    public final R3EClass carClass;
    public final boolean isClass;

    public R3ECarOrClass(R3ECar car) {
        this.car = car;
        this.carClass = null;
        this.isClass = false;
    }

    public R3ECarOrClass(R3EClass carClass) {
        this.car = null;
        this.carClass = carClass;
        this.isClass = true;
    }

    public String getName() {
        if (isClass) {
            return "";
        } else {
            return car.name;
        }
    }

    public String getId() {
        if (isClass) {
            return carClass.id;
        } else {
            return car.id;
        }
    }

    public URL getIcon() {
        if (isClass) {
            return carClass.icon;
        } else {
            return car.getIcon();
        }
    }

    public String getClassName() {
        if (isClass) {
            return carClass.name;
        } else {
            return car.carClass.name;
        }
    }

    public R3EClass getCarClass() {
        if (isClass) {
            return carClass;
        } else {
            return car.carClass;
        }
    }

    public Collection<R3ECar> getCars() {
        if (isClass) {
            return carClass.cars.values();
        } else {
            return Arrays.asList(car);
        }
    }
}
