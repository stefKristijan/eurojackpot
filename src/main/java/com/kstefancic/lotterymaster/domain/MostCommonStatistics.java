package com.kstefancic.lotterymaster.domain;

import java.util.List;

public class MostCommonStatistics {

    private final List<MostCommon> mostCommonStats;
    private final List<MostCommon> extraMostCommons;

    public MostCommonStatistics(List<MostCommon> mostCommons, List<MostCommon> extraMostCommons){
        this.mostCommonStats = mostCommons;
        this.extraMostCommons = extraMostCommons;
    }

    public List<MostCommon> getMostCommonStats() {
        return mostCommonStats;
    }

    public List<MostCommon> getExtraMostCommons() {
        return extraMostCommons;
    }
}
