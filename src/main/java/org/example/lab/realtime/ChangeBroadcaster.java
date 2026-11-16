package org.example.lab.realtime;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.TransactionPhase;
import jakarta.json.Json;

@ApplicationScoped
public class ChangeBroadcaster {
    public void onChange(@Observes(during = TransactionPhase.AFTER_SUCCESS) UpdateResource resource) {
        String message = Json.createObjectBuilder()
                .add("type", "data-changed")
                .add("resources", Json.createArrayBuilder().add(resource.getPath()))
                .build().toString();
        LiveUpdatesEndpoint.broadcast(message);
    }
}
