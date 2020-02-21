package com.kstefancic.lotterymaster.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CoefficientStatistics {

    private final List<NumberCoefficient> coefficients;
    private final List<NumberCoefficient> extraCoefficients;

    public CoefficientStatistics(List<NumberCoefficient> coefficients, List<NumberCoefficient> extraCoefficients) {
        this.coefficients = coefficients;
        this.extraCoefficients = extraCoefficients;
        convertStatsToPercentage();
    }

    public CoefficientStatistics(List<NumberCoefficient> coefficients, List<NumberCoefficient> extraCoefficients, Integer draw, Integer extraDraw, boolean withoutStats) {
        calculateRangesAndPercentage(coefficients);
        this.coefficients = coefficients.subList(0, draw);
        if (extraCoefficients != null && extraCoefficients.size() > 0) {
            calculateRangesAndPercentage(extraCoefficients);
            this.extraCoefficients = extraCoefficients.subList(0, extraDraw);
        }else{
            this.extraCoefficients = new ArrayList<>();
        }
        if (withoutStats) {
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
        } else {
            convertStatsToPercentage();
        }
    }

    public List<NumberCoefficient> getCoefficients() {
        return coefficients;
    }

    public List<NumberCoefficient> getExtraCoefficients() {
        return extraCoefficients;
    }

    private void convertStatsToPercentage() {
        calculateRangesAndPercentage(this.coefficients);
        if (!extraCoefficients.isEmpty())
            calculateRangesAndPercentage(this.extraCoefficients);
    }

    private void calculateRangesAndPercentage(List<NumberCoefficient> coefficients) {
        double maxNc = coefficients.get(0).getCoefficientSum();
        double minNc = coefficients.get(0).getCoefficientSum();
        double maxMcNc = coefficients.get(0).getMcCoefficient();
        double minMcNc = coefficients.get(0).getMcCoefficient();
        double maxRcNc = coefficients.get(0).getRangeCoefficient();
        double minRcNc = coefficients.get(0).getRangeCoefficient();
        double maxDNc = coefficients.get(0).getDrawnCoefficient();
        double minDNc = coefficients.get(0).getDrawnCoefficient();
        for (NumberCoefficient c : coefficients) {
            if (c.getCoefficientSum() > maxNc)
                maxNc = c.getCoefficientSum();
            if (c.getCoefficientSum() < minNc)
                minNc = c.getCoefficientSum();

            if (c.getRangeCoefficient() > maxRcNc)
                maxRcNc = c.getRangeCoefficient();
            if (c.getRangeCoefficient() < minRcNc)
                minRcNc = c.getRangeCoefficient();

            if (c.getMcCoefficient() > maxMcNc)
                maxMcNc = c.getMcCoefficient();
            if (c.getMcCoefficient() < minMcNc)
                minMcNc = c.getMcCoefficient();

            if (c.getDrawnCoefficient() > maxDNc)
                maxDNc = c.getDrawnCoefficient();
            if (c.getDrawnCoefficient() < minDNc)
                minDNc = c.getDrawnCoefficient();
        }
        ;
        double finalMinNc = minNc;
        double finalMaxNc = maxNc;
        double finalMinRcNc = minRcNc;
        double finalMaxRcNc = maxRcNc;
        double finalMinMcNc = minMcNc;
        double finalMaxMcNc = maxMcNc;
        double finalMinDNc = minDNc;
        double finalMaxDNc = maxDNc;
        coefficients.forEach(nc -> {
            nc.setCoefficientSum((nc.getCoefficientSum() - finalMinNc) / (finalMaxNc - finalMinNc));
            nc.setRangeCoefficient((nc.getRangeCoefficient() - finalMinRcNc) / (finalMaxRcNc - finalMinRcNc));
            nc.setMcCoefficient((nc.getMcCoefficient() - finalMinMcNc) / (finalMaxMcNc - finalMinMcNc));
            nc.setDrawnCoefficient((nc.getDrawnCoefficient() - finalMinDNc) / (finalMaxDNc - finalMinDNc));
        });
    }

}
