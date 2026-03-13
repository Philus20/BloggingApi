package com.example.BloggingApi.Aasync;

public class AnalyticsService implements EventListener {

    @Override
    public void handle(Event event) {

        OrderCreatedEvent orderEvent = (OrderCreatedEvent) event;

        System.out.println("Analytics recorded order: " + orderEvent.getOrderId());
    }
}