package org.example.lab.realtime;

import jakarta.inject.Inject;
import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;

@WebListener
public class LiveUpdatesSessionListener implements HttpSessionListener {
    @Inject
    LiveUpdates updates;

    @Override
    public void sessionDestroyed(HttpSessionEvent event) {
        updates.closeSession(event.getSession().getId());
    }
}
