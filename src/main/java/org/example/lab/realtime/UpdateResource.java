package org.example.lab.realtime;

public enum UpdateResource {
    HUMANS("humans"),
    CARS("cars"),
    COORDINATES("coordinates");

    private final String path;

    UpdateResource(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
