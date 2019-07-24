package com.kstefancic.lotterymaster.domain;

import java.util.ArrayList;
import java.util.List;

public class NumberStatistics {

    private List<NumberStat> stats = new ArrayList<>();
    private List<NumberStat> extraStats = new ArrayList<>();

    public NumberStatistics(int draws, Lottery lottery){
        for(int i = 1; i <= lottery.getMaxNumber(); i++){
            stats.add(new NumberStat(i,0, draws));
        }
        if(lottery.hasExtraNumbers()){
            for(int i = 1; i <= lottery.getMaxExtraNumber(); i++){
                extraStats.add(new NumberStat(i,0, draws));
            }
        }
    }

    public void updateStats(Draw draw, int cycle){
        draw.getNumbers().forEach(n -> {
            NumberStat numberStat = stats.stream().filter(ns -> ns.getNumber() == n).findFirst().get();
            numberStat.draw();
            numberStat.cyclesNotDrawn(cycle);
        });
        draw.getExtraNumbers().forEach(en -> {
            NumberStat eNumStat = extraStats.stream().filter(es -> es.getNumber() == en).findFirst().get();
            eNumStat.draw();
            eNumStat.cyclesNotDrawn(cycle);
        });
    }

    public List<NumberStat> getStats() {
        return stats;
    }

    public List<NumberStat> getExtraStats() {
        return extraStats;
    }
}
