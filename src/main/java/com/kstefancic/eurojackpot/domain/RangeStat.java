package com.kstefancic.eurojackpot.domain;

import org.w3c.dom.ranges.Range;

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
