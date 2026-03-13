package com.example.BloggingApi.Aasync;


import java.util.HashMap;
import java.util.List;
import java.util.*;

public class EventBus {

    private Map<Class<? extends Event>, List<EventListener>> listeners = new HashMap<>();

    public void subscribe(Class<? extends Event> eventType, EventListener listener) {

        listeners
                .computeIfAbsent(eventType, k -> new ArrayList<>())
                .add(listener);
    }

    public void publish(Event event) {

        List<EventListener> eventListeners = listeners.get(event.getClass());

        if (eventListeners == null) {
            return;
        }

        for (EventListener listener : eventListeners) {
            listener.handle(event);
        }
    }
}