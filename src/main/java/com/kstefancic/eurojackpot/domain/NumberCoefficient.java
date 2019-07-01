package com.kstefancic.eurojackpot.domain;

public class NumberCoefficient {

    private int number;
    private double rangeCoefficient = 0;
    private double mcCoefficient;
    private double drawnCoefficient = 0;
    private double coefficientSum;

    public NumberCoefficient(int number, double mcCoefficient){
        this.number = number;
        this.mcCoefficient = mcCoefficient;
        this.coefficientSum = mcCoefficient;
    }

    public void addDrawnCoefficient(double drawnCoefficient){
        this.drawnCoefficient = drawnCoefficient;
        this.coefficientSum += drawnCoefficient;
    }

    public void addRangeCoefficient(double rangeCoefficient) {
        this.rangeCoefficient = rangeCoefficient;
        this.coefficientSum += rangeCoefficient;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public double getRangeCoefficient() {
        return rangeCoefficient;
    }

    public void setRangeCoefficient(double rangeCoefficient) {
        this.rangeCoefficient = rangeCoefficient;
    }

    public double getMcCoefficient() {
        return mcCoefficient;
    }

    public void setMcCoefficient(double mcCoefficient) {
        this.mcCoefficient = mcCoefficient;
    }

    public double getDrawnCoefficient() {
        return drawnCoefficient;
    }

    public void setDrawnCoefficient(double drawnCoefficient) {
        this.drawnCoefficient = drawnCoefficient;
    }

    public double getCoefficientSum() {
        return coefficientSum;
    }

    public void setCoefficientSum(double coefficientSum) {
        this.coefficientSum = coefficientSum;
    }
}
