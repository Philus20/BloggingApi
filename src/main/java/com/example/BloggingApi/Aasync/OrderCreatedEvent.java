package com.example.BloggingApi.Aasync;

public class OrderCreatedEvent implements Event {

    private int orderId;

    public OrderCreatedEvent(int orderId) {
        this.orderId = orderId;
    }

    public int getOrderId() {
        return orderId;
    }
}