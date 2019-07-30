package com.kstefancic.lotterymaster.domain;

import javax.validation.constraints.NotNull;

public class Generator {

    private Integer draws;
    private Integer maxDraws;
    @NotNull
    private Double rangeMultiplier;
    @NotNull
    private Double mcMultiplier;
    @NotNull
    private Double drawnMultiplier;
    private Double lastDrawDivider = 1.0;
    private Integer range;
    @NotNull
    private GeneratorType type;
    @NotNull
    private GeneratorSort sort;

    public GeneratorType getType() {
        return type;
    }

    public void setType(GeneratorType type) {
        this.type = type;
    }

    public GeneratorSort getSort() {
        return sort;
    }

    public void setSort(GeneratorSort sort) {
        this.sort = sort;
    }

    public Integer getDraws() {
        return draws;
    }

    public void setDraws(Integer draws) {
        this.draws = draws;
    }

    public Integer getMaxDraws() {
        return maxDraws;
    }

    public void setMaxDraws(Integer maxDraws) {
        this.maxDraws = maxDraws;
    }

    public Double getRangeMultiplier() {
        return rangeMultiplier;
    }

    public void setRangeMultiplier(Double rangeMultiplier) {
        this.rangeMultiplier = rangeMultiplier;
    }

    public Double getMcMultiplier() {
        return mcMultiplier;
    }

    public void setMcMultiplier(Double mcMultiplier) {
        this.mcMultiplier = mcMultiplier;
    }

    public Double getDrawnMultiplier() {
        return drawnMultiplier;
    }

    public void setDrawnMultiplier(Double drawnMultiplier) {
        this.drawnMultiplier = drawnMultiplier;
    }

    public Double getLastDrawDivider() {
        return lastDrawDivider;
    }

    public void setLastDrawDivider(Double lastDrawDivider) {
        this.lastDrawDivider = lastDrawDivider;
    }

    public Integer getRange() {
        return range;
    }

    public void setRange(Integer range) {
        this.range = range;
    }
}
