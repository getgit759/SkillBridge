// src/main/java/com/skillbridge/event/SessionCompletedEvent.java
package com.skillbridge.event;

import com.skillbridge.model.Session;
import org.springframework.context.ApplicationEvent;

public class SessionCompletedEvent extends ApplicationEvent {

    private final Session session;

    public SessionCompletedEvent(Object source, Session session) {
        super(source);
        this.session = session;
    }

    public Session getSession() {
        return session;
    }
}