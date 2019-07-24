package com.kstefancic.lotterymaster.domain;

public class  NumberStat {

    private int number;
    private int drawn = 0;
    private int cyclesNotDrawn = 0;

    boolean cyclesNotDrawnUpdated = false;

    public NumberStat() {
    }

    public NumberStat(int number, int drawn, int cyclesNotDrawn) {
        this.number = number;
        this.drawn = drawn;
        this.cyclesNotDrawn = cyclesNotDrawn;
    }

    public void draw() {
        drawn++;
    }

    public void cyclesNotDrawn(int cycles) {
        if (!cyclesNotDrawnUpdated) {
            cyclesNotDrawn = cycles;
            cyclesNotDrawnUpdated = true;
        }
    }

    public int getDrawn() {
        return drawn;
    }

    public int getCyclesNotDrawn() {
        return cyclesNotDrawn;
    }

    public int getNumber() {
        return number;
    }
}