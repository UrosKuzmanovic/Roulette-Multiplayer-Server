package main;

import java.io.Serializable;

public class FieldInfo implements Serializable{

    private int number;
    private double bet;

    public FieldInfo(int number, double bet) {
        this.number = number;
        this.bet = bet;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public double getBet() {
        return bet;
    }

    public void setBet(double bet) {
        this.bet = bet;
    }

}