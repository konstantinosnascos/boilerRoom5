package com.example;
public class Order {

    private int orderId;
    private int customerId;
    private double amount;

    public Order(int orderId, int customerId, double amount) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.amount = amount;
    }

    public int getOrderId() {
        return orderId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public double getAmount() {
        return amount;
    }

    @Override
    public String toString() {
        return String.format("\nOrder{id=%d, customerId=%d, amount=%.2f}",
                orderId, customerId, amount);
    }
}


