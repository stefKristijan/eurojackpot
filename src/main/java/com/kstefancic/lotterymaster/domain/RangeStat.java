package com.kstefancic.lotterymaster.domain;

public class RangeStat {

    private final int from;
    private final int to;
    private final int draws;

    public RangeStat(int from, int to, int draws){
        this.from = from;
        this.to = to;
        this.draws = draws;
    }

    public int getFrom() {
        return from;
    }

    public int getTo() {
        return to;
    }

    public int getDraws() {
        return draws;
    }
}
