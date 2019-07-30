package com.kstefancic.lotterymaster.domain;

import java.util.List;

public class CoefficientStatistics {

    private final List<NumberCoefficient> coefficients;
    private final List<NumberCoefficient> extraCoefficients;

    public CoefficientStatistics(List<NumberCoefficient> coefficients, List<NumberCoefficient> extraCoefficients){
        this.coefficients = coefficients;
        this.extraCoefficients = extraCoefficients;
    }

    public CoefficientStatistics(List<NumberCoefficient> coefficients, List<NumberCoefficient> extraCoefficients, Integer draw, Integer extraDraw, boolean withoutStats) {
        this.coefficients = coefficients.subList(0, draw);
        this.extraCoefficients = extraCoefficients.subList(0, extraDraw);
        if(withoutStats){
            this.coefficients.forEach(c -> {
                c.setRangeCoefficient(0);
                c.setMcCoefficient(0);
                c.setDrawnCoefficient(0);
                c.setCoefficientSum(0);
            });
            this.extraCoefficients.forEach(ec -> {
                ec.setRangeCoefficient(0);
                ec.setMcCoefficient(0);
                ec.setDrawnCoefficient(0);
                ec.setCoefficientSum(0);
            });
        }
    }

    public List<NumberCoefficient> getCoefficients() {
        return coefficients;
    }

    public List<NumberCoefficient> getExtraCoefficients() {
        return extraCoefficients;
    }

}
