package com.kstefancic.eurojackpot;

import com.kstefancic.eurojackpot.domain.*;
import com.kstefancic.eurojackpot.repository.DrawRepository;
import com.kstefancic.eurojackpot.repository.LotteryRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class StatisticsServiceImpl implements StatisticsService {

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
    public Map<Integer, Double> nextDrawNumberCoefficients(int lotteryId, int draws) {
        Lottery lottery = lotteryRepository.findById(lotteryId)
                .orElseThrow(() -> new EntityNotFoundException(Constants.NOT_FOUND_LOTTERY));

        NumberStatistics statsForDraws = lotteryNumberStats(lotteryId, draws);
        NumberStatistics statsForAll = lotteryNumberStats(lotteryId, lottery.getDraws().size());

        Map<Integer, Double> numberCoefficients = calculatePairCoefficient(lottery, draws);
        Map<Integer, Double> rangeCoefficient = calculateRangeCoefficient(lottery, draws, statsForDraws, statsForAll);
        numberCoefficients.forEach((k,v) -> numberCoefficients.put(k, numberCoefficients.get(k) + rangeCoefficient.get(k)));

        statsForDraws.getStats().forEach(ns ->
                numberCoefficients.put(ns.getNumber(), numberCoefficients.get(ns.getNumber()) +
                        ((1.0 * ns.getDrawn() / draws) * (1.0 * statsForAll.getStats().stream().filter(nsA -> nsA.getNumber() == ns.getNumber()).findFirst().get().getDrawn() / lottery.getDraws().size()) + (ns.getCyclesNotDrawn() / lottery.getDraws().size())))
        );

        return numberCoefficients;
    }

    private Map<Integer, Double> calculateRangeCoefficient(Lottery lottery, int draws, NumberStatistics statsForDraws, NumberStatistics statsForAll) {
        int range = lottery.getMaxNumber() / lottery.getDraw();
        RangeStatistics rangeForDraws = lotteryTensStats(lottery.getId(), draws, range, 2);
        RangeStatistics rangeForAll = lotteryTensStats(lottery.getId(), lottery.getDraws().size(), range, 2);

        Map<Integer, Double> rangeDrawsCoefficient = new HashMap<>();
        for (int i = 1; i <= lottery.getMaxNumber(); i++) {
            rangeDrawsCoefficient.put(i, 0.0);
        }

        rangeForDraws.getStats().forEach(rs -> {
            for (int i = rs.getFrom(); i <= rs.getTo(); i++) {
                int finalI = i;
                rangeDrawsCoefficient.put(i, (1.0 * rs.getDraws() / draws) / (rs.getTo() - rs.getFrom() + 1) *
                        statsForDraws.getStats().stream().filter(s -> s.getNumber() == finalI).findFirst().get().getDrawn() / rs.getDraws());
            }
        });
        rangeForAll.getStats().forEach(rs -> {
            for (int i = rs.getFrom(); i <= rs.getTo(); i++) {
                int finalI = i;
                rangeDrawsCoefficient.put(i, rangeDrawsCoefficient.get(i) + (1.0 * rs.getDraws() / lottery.getDraws().size()) / (rs.getTo() - rs.getFrom() + 1) *
                        statsForAll.getStats().stream().filter(s -> s.getNumber() == finalI).findFirst().get().getDrawn() / rs.getDraws());
            }
        });
        return rangeDrawsCoefficient;
    }

    private Map<Integer, Double> calculatePairCoefficient(Lottery lottery, int draws) {
        Map<Integer, Double> pairCoefficients = new HashMap<>();
        Map<Integer, Double> triplesCoefficients = new HashMap<>();
        MostCommonStatistics pairsDraws = lotteryMostCommon(lottery.getId(), 2, draws, 2);
        MostCommonStatistics pairsAll = lotteryMostCommon(lottery.getId(), 2, lottery.getDraws().size(), 2);
        MostCommonStatistics tripleDraws = lotteryMostCommon(lottery.getId(), 3, draws, 2);
        MostCommonStatistics tripleAll = lotteryMostCommon(lottery.getId(), 3, lottery.getDraws().size(), 2);
        for (int i = 1; i <= lottery.getMaxNumber(); i++) {
            pairCoefficients.put(i, 0.0);
            triplesCoefficients.put(i, 0.0);
        }

        pairsDraws.getMostCommonStats().forEach(mc ->
                mc.getNumbers().forEach(n -> pairCoefficients.put(n, pairCoefficients.get(n) + (1.0 * mc.getDrawn() / draws)))
        );
        pairsAll.getMostCommonStats().forEach(mc ->
                mc.getNumbers().forEach(n -> pairCoefficients.put(n, pairCoefficients.get(n) + (1.0 * mc.getDrawn() / lottery.getDraws().size())))
        );
        pairCoefficients.forEach((k, v) -> pairCoefficients.put(k, pairCoefficients.get(k) * 2 / lottery.getDraw()));

        tripleDraws.getMostCommonStats().forEach(mc ->
                mc.getNumbers().forEach(n -> triplesCoefficients.put(n, triplesCoefficients.get(n) + (1.0 * mc.getDrawn() / draws)))
        );
        tripleAll.getMostCommonStats().forEach(mc ->
                mc.getNumbers().forEach(n -> triplesCoefficients.put(n, triplesCoefficients.get(n) + (1.0 * mc.getDrawn() / lottery.getDraws().size())))
        );
        triplesCoefficients.forEach((k, v) -> triplesCoefficients.put(k, triplesCoefficients.get(k) * 3 / lottery.getDraw()));

        pairCoefficients.forEach((k, v) -> pairCoefficients.put(k, pairCoefficients.get(k) + triplesCoefficients.get(k)));

        return pairCoefficients;
    }
}
