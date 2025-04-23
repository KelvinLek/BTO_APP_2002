package entity;

import pub_enums.FlatType;

public class Flat {
    private FlatType flatType;
    private int total;
    private int remaining;
    private double price;

    public Flat(FlatType flatType, int total, int remaining, double price) {
        this.flatType = flatType;
        this.total = total;
        this.remaining = remaining;
        this.price = price;
    }


    public FlatType getFlatType() {
        return flatType;
    }

    public void setFlatType(FlatType flatType) {
        this.flatType = flatType;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getRemaining() {
        return remaining;
    }

    public void setRemaining(int remaining) {
        this.remaining = remaining;
    }
}
