package com.example.BloggingApi.Aasync;

public class EmailService implements EventListener {

    @Override
    public void handle(Event event) {

        OrderCreatedEvent orderEvent = (OrderCreatedEvent) event;

        System.out.println("Email sent for order: " + orderEvent.getOrderId());
    }
}