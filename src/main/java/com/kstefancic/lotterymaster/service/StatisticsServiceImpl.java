package com.kstefancic.lotterymaster.service;

import com.kstefancic.lotterymaster.domain.*;
import com.kstefancic.lotterymaster.repository.DrawRepository;
import com.kstefancic.lotterymaster.repository.LotteryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class StatisticsServiceImpl implements StatisticsService {

    private static final Logger logger = LoggerFactory.getLogger(StatisticsServiceImpl.class);
    final private LotteryRepository lotteryRepository;
    final private DrawRepository drawRepository;

    @PersistenceContext
    private EntityManager em;

    public StatisticsServiceImpl(LotteryRepository lotteryRepository, DrawRepository drawRepository) {
        this.lotteryRepository = lotteryRepository;
        this.drawRepository = drawRepository;
    }

    @Override
    public NumberStatistics lotteryNumberStats(int lotteryId, Integer draws) {
        Lottery lottery = lotteryRepository.findById(lotteryId)
            .orElseThrow(() -> new EntityNotFoundException(Constants.NOT_FOUND_LOTTERY));
        Pageable pageable = PageRequest.of(0, draws != null && draws > 0 ? draws : Integer.MAX_VALUE, Sort.by("time").descending());
        List<Draw> drawList = drawRepository.findAllByLotteryId(lotteryId, pageable);
        NumberStatistics numberStatistics = new NumberStatistics(drawList.size(), lottery);
        for (int i = 0; i < drawList.size(); i++) {
            numberStatistics.updateStats(drawList.get(i), i);
        }
        return numberStatistics;
    }

    @Override
    public RangeStatistics lotteryTensStats(int lotteryId, Integer draws, Integer range, Integer extraRange) {
        Lottery lottery = lotteryRepository.findById(lotteryId)
            .orElseThrow(() -> new EntityNotFoundException(Constants.NOT_FOUND_LOTTERY));
        draws = draws != null && draws > 0 ? draws : Integer.MAX_VALUE;
        range = range != null && range > 1 ? range : lottery.getDraw();
        extraRange = extraRange != null && extraRange > 1 ? extraRange : lottery.getExtraDraw();
        List<RangeStat> rangeStats = new ArrayList<>();
        List<RangeStat> extraStats = new ArrayList<>();
        for (int i = 1; i <= lottery.getMaxNumber(); i += range) {
            int to = i + range - 1 < lottery.getMaxNumber() ? i + range - 1 : lottery.getMaxNumber();
            rangeStats.add(new RangeStat(i, to, drawRepository.countInRange(lotteryId, draws, i, to)));
        }
        for (int i = 1; i <= (lottery.getMaxExtraNumber() != null ? lottery.getMaxExtraNumber() : 0); i += extraRange) {
            int to = i + extraRange - 1 < lottery.getMaxExtraNumber() ? i + extraRange - 1 : lottery.getMaxExtraNumber();
            extraStats.add(new RangeStat(i, to, drawRepository.countExtraInRange(lotteryId, draws, i, to)));
        }
        return new RangeStatistics(rangeStats, extraStats);
    }

    @Override
    public MostCommonStatistics lotteryMostCommon(int lotteryId, int quantity, Integer draws, int extraQuantity) {
        Lottery lottery = lotteryRepository.findById(lotteryId)
            .orElseThrow(() -> new EntityNotFoundException(Constants.NOT_FOUND_LOTTERY));
        if (quantity < 2 || quantity > lottery.getDraw()) {
            quantity = 2;
        }
        List<MostCommon> mostCommons = drawRepository.mostCommonStats(em, lotteryId, quantity, draws != null && draws > 0 ? draws : Integer.MAX_VALUE, "numbers");
        List<MostCommon> extraMostCommons = null;
        if (lottery.hasExtraNumbers()) {
            if (extraQuantity < 2 || quantity > lottery.getExtraDraw()) {
                extraQuantity = lottery.getExtraDraw();
            }
            extraMostCommons = drawRepository.mostCommonStats(em, lotteryId, extraQuantity, draws != null && draws > 0 ? draws : Integer.MAX_VALUE, "extra_numbers");
        }

        return new MostCommonStatistics(mostCommons, extraMostCommons);
    }

    @Override
    public List<NumberCoefficient> nextDrawNumberCoefficients(int lotteryId, Integer draws, Integer maxDraws, Double rangeMultiplier, Double mcMultiplier, Double drawnMultiplier, Integer range, Double lastDrawDivider) {
        Lottery lottery = lotteryRepository.findById(lotteryId)
            .orElseThrow(() -> new EntityNotFoundException(Constants.NOT_FOUND_LOTTERY));
        List<NumberCoefficient> coefficients = new ArrayList<>();

        draws = draws == null || draws < 10 || draws >= lottery.getDraws().size() - 10 ? 10 : draws;
        maxDraws = maxDraws == null || maxDraws <= draws ? lottery.getDraws().size() : maxDraws;
        range = range == null || range < 2 || range > lottery.getMaxNumber() / lottery.getDraw() ? lottery.getMaxNumber() / lottery.getDraw() : range;
        rangeMultiplier = rangeMultiplier == null || rangeMultiplier < 0 ? 1.0 : rangeMultiplier;
        mcMultiplier = mcMultiplier == null || mcMultiplier < 0 ? 1.0 : mcMultiplier;
        drawnMultiplier = drawnMultiplier == null || drawnMultiplier < 0 ? 1.0 : drawnMultiplier;

        NumberStatistics statsForDraws = lotteryNumberStats(lotteryId, draws);
        NumberStatistics statsForAll = lotteryNumberStats(lotteryId, maxDraws);

        calculatePairCoefficient(lottery, draws, maxDraws, mcMultiplier).forEach((k, v) -> coefficients.add(new NumberCoefficient(k, v)));

        Map<Integer, Double> rangeCoefficient = calculateRangeCoefficient(lottery, draws, maxDraws, range, rangeMultiplier, statsForDraws, statsForAll);
        coefficients.forEach(nc -> nc.addRangeCoefficient(rangeCoefficient.get(nc.getNumber())));

        Map<Integer, Double> drawnCoefficients = calculateDrawnCoefficients(statsForDraws, statsForAll, draws, drawnMultiplier, maxDraws, lottery);
        coefficients.forEach(nc -> nc.addDrawnCoefficient(drawnCoefficients.get(nc.getNumber())));

        coefficients.forEach(nc -> {
            if (statsForDraws.getStats().stream().anyMatch(s -> s.getNumber() == nc.getNumber() && s.getCyclesNotDrawn() == 0)) {
                nc.setCoefficientSum(nc.getCoefficientSum() /
                    (lastDrawDivider == null || lastDrawDivider <= 0 ? 2 : lastDrawDivider));
            }
        });

        coefficients.sort(Comparator.comparing(NumberCoefficient::getCoefficientSum).reversed());
        return coefficients;
    }

    private Map<Integer, Double> calculateDrawnCoefficients(NumberStatistics statsForDraws, NumberStatistics statsForAll, Integer draws, Double drawMultiplier, Integer maxDraws, Lottery lottery) {
        Map<Integer, Double> numberCoefficients = new HashMap<>();
        for (int i = 1; i <= lottery.getMaxNumber(); i++) {
            numberCoefficients.put(i, 0.0);
        }
        statsForDraws.getStats().forEach(ns ->
            {
                NumberStat nsAll = statsForAll.getStats().stream().filter(nsA -> nsA.getNumber() == ns.getNumber()).findFirst().get();
                numberCoefficients.put(ns.getNumber(), numberCoefficients.get(ns.getNumber()) +
                    ((1.0 * ns.getDrawn() / draws) * (1.0 * nsAll.getDrawn() / maxDraws) + (1.0 * nsAll.getCyclesNotDrawn() / maxDraws)));
            }
        );
        numberCoefficients.forEach((k, v) -> numberCoefficients.put(k, numberCoefficients.get(k) * drawMultiplier));
        return numberCoefficients;
    }

    private Map<Integer, Double> calculateRangeCoefficient(Lottery lottery, int draws, Integer maxDraws, Integer range,
                                                           Double rangeMultiplier, NumberStatistics statsForDraws, NumberStatistics statsForAll) {
        RangeStatistics rangeForDraws = lotteryTensStats(lottery.getId(), draws, range, 2);
        RangeStatistics rangeForAll = lotteryTensStats(lottery.getId(), maxDraws, range, 2);

        Map<Integer, Double> rangeDrawsCoefficient = new HashMap<>();
        for (int i = 1; i <= lottery.getMaxNumber(); i++) {
            rangeDrawsCoefficient.put(i, 0.0);
        }

        rangeForDraws.getStats().forEach(rs -> {
            int interval = rs.getTo() - rs.getFrom() + 1;
            RangeStat allRs = rangeForAll.getStats().stream().filter(rsA -> rsA.getFrom() == rs.getFrom() && rsA.getTo() == rs.getTo())
                .findFirst().orElseThrow(() -> new RuntimeException("Different range interval length for all draws"));
            for (int i = rs.getFrom(); i <= rs.getTo(); i++) {
                int finalI = i;
                double drawnInDraws = 1.0 * statsForDraws.getStats().stream().filter(s -> s.getNumber() == finalI).findFirst().get().getDrawn() / rs.getDraws();
                double drawnInAll = 1.0 * statsForAll.getStats().stream().filter(s -> s.getNumber() == finalI).findFirst().get().getDrawn() / allRs.getDraws();
                double drawsRangePercent = (rs.getDraws() / (1.0 * draws * interval)) / (allRs.getDraws() / (50.0 * interval));
                double drawnPercent = 1.0 * drawnInDraws / drawnInAll;
                rangeDrawsCoefficient.put(i,
                    (
                        (drawsRangePercent >= 1 ? -1.0 * (drawsRangePercent - (int) drawsRangePercent) : drawsRangePercent * rangeMultiplier) +
                            (drawnPercent >= 1 ? -1.0 * (drawnPercent - (int) drawnPercent) : drawnPercent * rangeMultiplier)
                    ) / interval
                );
            }
        });
        return rangeDrawsCoefficient;
    }

    private Map<Integer, Double> calculatePairCoefficient(Lottery lottery, int draws, Integer maxDraws, Double mcMultiplier) {
        Map<Integer, Double> pairCoefficients = new HashMap<>();
        Map<Integer, Double> triplesCoefficients = new HashMap<>();
        MostCommonStatistics pairsDraws = lotteryMostCommon(lottery.getId(), 2, draws, 2);
        MostCommonStatistics pairsAll = lotteryMostCommon(lottery.getId(), 2, maxDraws, 2);
        MostCommonStatistics tripleDraws = lotteryMostCommon(lottery.getId(), 3, draws, 2);
        MostCommonStatistics tripleAll = lotteryMostCommon(lottery.getId(), 3, maxDraws, 2);
        for (int i = 1; i <= lottery.getMaxNumber(); i++) {
            pairCoefficients.put(i, 0.0);
            triplesCoefficients.put(i, 0.0);
        }

        pairsDraws.getMostCommonStats().forEach(mc ->
            mc.getNumbers().forEach(n -> pairCoefficients.put(n, pairCoefficients.get(n) + (1.0 * mc.getDrawn() / draws)))
        );
        pairsAll.getMostCommonStats().forEach(mc ->
            mc.getNumbers().forEach(n -> pairCoefficients.put(n, pairCoefficients.get(n) + (1.0 * mc.getDrawn() / maxDraws)))
        );
        pairCoefficients.forEach((k, v) -> pairCoefficients.put(k, pairCoefficients.get(k) * 2 / lottery.getDraw()));

        tripleDraws.getMostCommonStats().forEach(mc ->
            mc.getNumbers().forEach(n -> triplesCoefficients.put(n, triplesCoefficients.get(n) + (1.0 * mc.getDrawn() / draws)))
        );
        tripleAll.getMostCommonStats().forEach(mc ->
            mc.getNumbers().forEach(n -> triplesCoefficients.put(n, triplesCoefficients.get(n) + (1.0 * mc.getDrawn() / maxDraws)))
        );
        triplesCoefficients.forEach((k, v) -> triplesCoefficients.put(k, triplesCoefficients.get(k) * 3 / lottery.getDraw()));

        pairCoefficients.forEach((k, v) -> pairCoefficients.put(k, mcMultiplier * (pairCoefficients.get(k) + triplesCoefficients.get(k))));

        return pairCoefficients;
    }
}
