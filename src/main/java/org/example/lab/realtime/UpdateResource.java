package org.example.lab.realtime;

public enum UpdateResource {
    HUMANS("humans"),
    CARS("cars"),
    COORDINATES("coordinates"),
    ALL("humans", "cars", "coordinates");

    private final String[] paths;

    UpdateResource(String... paths) {
        this.paths = paths;
    }

    public String[] getPaths() {
        return paths;
    }
}
