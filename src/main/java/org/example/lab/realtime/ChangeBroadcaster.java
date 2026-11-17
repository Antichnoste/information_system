package org.example.lab.realtime;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.TransactionPhase;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;

@ApplicationScoped
public class ChangeBroadcaster {
    public void onChange(@Observes(during = TransactionPhase.AFTER_SUCCESS) UpdateResource resource) {
        JsonArrayBuilder resources = Json.createArrayBuilder();
        for (String path : resource.getPaths()) {
            resources.add(path);
        }
        String message = Json.createObjectBuilder()
                .add("type", "data-changed")
                .add("resources", resources)
                .build().toString();
        LiveUpdatesEndpoint.broadcast(message);
    }
}
