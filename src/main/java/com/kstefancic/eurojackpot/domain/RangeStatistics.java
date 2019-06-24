package com.kstefancic.eurojackpot.domain;

import java.util.List;

public class RangeStatistics {
    List<RangeStat> stats;
    List<RangeStat> extraStats;

    public RangeStatistics(List<RangeStat> stats, List<RangeStat> extraStats){
        this.stats = stats;
        this.extraStats = extraStats;
    }

    public List<RangeStat> getStats() {
        return stats;
    }

    public List<RangeStat> getExtraStats() {
        return extraStats;
    }
}
