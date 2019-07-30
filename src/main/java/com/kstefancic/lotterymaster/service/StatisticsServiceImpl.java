package com.kstefancic.lotterymaster.service;

import com.kstefancic.lotterymaster.domain.*;
import com.kstefancic.lotterymaster.repository.DrawRepository;
import com.kstefancic.lotterymaster.repository.LotteryRepository;
import com.kstefancic.lotterymaster.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceContext;
import javax.validation.ValidationException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class StatisticsServiceImpl implements StatisticsService {

    private static final Logger logger = LoggerFactory.getLogger(StatisticsServiceImpl.class);
    private final LotteryRepository lotteryRepository;
    private final DrawRepository drawRepository;
    private final UserRepository userRepository;

    @PersistenceContext
    private EntityManager em;

    public StatisticsServiceImpl(LotteryRepository lotteryRepository, DrawRepository drawRepository, UserRepository userRepository) {
        this.lotteryRepository = lotteryRepository;
        this.drawRepository = drawRepository;
        this.userRepository = userRepository;
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
    public CoefficientStatistics nextDrawNumberCoefficients(int lotteryId, Generator generator) {
        User user = userRepository.findById(SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(() -> new EntityNotFoundException("No such user"));
        Lottery lottery = lotteryRepository.findById(lotteryId)
                .orElseThrow(() -> new EntityNotFoundException(Constants.NOT_FOUND_LOTTERY));
        List<NumberCoefficient> coefficients = new ArrayList<>();
        List<NumberCoefficient> extraCoefficients = new ArrayList<>();

        validateGeneratesLeft(generator.getType(), user.getGeneratesLeft());

        int draws = generator.getDraws() == null || generator.getDraws() < 10 || generator.getDraws() >= lottery.getDraws().size() - 10 ? 10 : generator.getDraws();
        int maxDraws = generator.getMaxDraws() == null || generator.getMaxDraws() <= draws ? lottery.getDraws().size() : generator.getMaxDraws();
        int range = generator.getRange() == null || generator.getRange() < 2 || generator.getRange() > lottery.getMaxNumber() / lottery.getDraw() ? lottery.getMaxNumber() / lottery.getDraw() : generator.getRange();

        NumberStatistics statsForDraws = lotteryNumberStats(lotteryId, draws);
        NumberStatistics statsForAll = lotteryNumberStats(lotteryId, maxDraws);

        calculatePairCoefficient(lottery, draws, maxDraws, generator.getMcMultiplier()).forEach((k, v) -> coefficients.add(new NumberCoefficient(k, v)));

        Map<Integer, Double> rangeCoefficient = calculateRangeCoefficient(lottery, draws, maxDraws, range, generator.getRangeMultiplier(), statsForDraws, statsForAll);
        coefficients.forEach(nc -> nc.addRangeCoefficient(rangeCoefficient.get(nc.getNumber())));

        Map<Integer, Double> drawnCoefficients = calculateDrawnCoefficients(statsForDraws, statsForAll, draws, generator.getDrawnMultiplier(), maxDraws, lottery);
        coefficients.forEach(nc -> nc.addDrawnCoefficient(drawnCoefficients.get(nc.getNumber())));

        coefficients.forEach(nc -> {
            if (statsForDraws.getStats().stream().anyMatch(s -> s.getNumber() == nc.getNumber() && s.getCyclesNotDrawn() == 0)) {
                nc.setCoefficientSum(nc.getCoefficientSum() /
                        (generator.getLastDrawDivider() == null || generator.getLastDrawDivider() <= 0 ? 2 : generator.getLastDrawDivider()));
            }
        });

        if (lottery.hasExtraNumbers()) {
            if (lottery.getExtraDraw() > 1) {
                calculateExtraPairCoefficient(lottery, draws, maxDraws).forEach((k, v) -> extraCoefficients.add(new NumberCoefficient(k, v)));
                Map<Integer, Double> extraDrawnC = calculateExtraDrawnCoefficients(statsForDraws, statsForAll, draws, maxDraws, lottery);
                extraCoefficients.forEach(nc -> nc.addDrawnCoefficient(extraDrawnC.get(nc.getNumber())));
            }else{
                calculateExtraDrawnCoefficients(statsForDraws, statsForAll, draws, maxDraws, lottery)
                        .forEach((k, v) -> {
                            NumberCoefficient nc = new NumberCoefficient(k);
                            nc.addDrawnCoefficient(v);
                            extraCoefficients.add(nc);
                        });
            }

            extraCoefficients.forEach(nc -> {
                if (statsForDraws.getExtraStats().stream().anyMatch(s -> s.getNumber() == nc.getNumber() && s.getCyclesNotDrawn() == 0)) {
                    nc.setCoefficientSum(nc.getCoefficientSum() / 2);
                }
            });
        }
        switch (generator.getSort()) {
            case MC:
                coefficients.sort(Comparator.comparing(NumberCoefficient::getMcCoefficient).reversed());
                extraCoefficients.sort(Comparator.comparing(NumberCoefficient::getCoefficientSum).reversed());
                break;
            case DRAWS:
                coefficients.sort(Comparator.comparing(NumberCoefficient::getDrawnCoefficient).reversed());
                extraCoefficients.sort(Comparator.comparing(NumberCoefficient::getDrawnCoefficient).reversed());
                break;
            case RANGE:
                coefficients.sort(Comparator.comparing(NumberCoefficient::getRangeCoefficient).reversed());
                extraCoefficients.sort(Comparator.comparing(NumberCoefficient::getRangeCoefficient).reversed());
                break;
            default:
                coefficients.sort(Comparator.comparing(NumberCoefficient::getCoefficientSum).reversed());
                extraCoefficients.sort(Comparator.comparing(NumberCoefficient::getCoefficientSum).reversed());
        }
        CoefficientStatistics result;
        switch (generator.getType()) {
            case DRAW:
                result = new CoefficientStatistics(coefficients, extraCoefficients, lottery.getDraw(), lottery.getExtraDraw(), true);
                user.setGeneratesLeft(user.getGeneratesLeft() - 1);
                break;
            case DRAW_STATS:
                result = new CoefficientStatistics(coefficients, extraCoefficients, lottery.getDraw(), lottery.getExtraDraw(), false);
                user.setGeneratesLeft(user.getGeneratesLeft() - 2);
                break;
            default:
                result = new CoefficientStatistics(coefficients, extraCoefficients);
                user.setGeneratesLeft(user.getGeneratesLeft() - 4);
                break;
        }
        userRepository.save(user);
        return result;
    }

    private void validateGeneratesLeft(GeneratorType type, int generatesLeft) {
        if (generatesLeft < 1) {
            throw new ValidationException("Not enough tickets");
        } else if (type.equals(GeneratorType.DRAW_STATS) && generatesLeft < 2) {
            throw new ValidationException("You need 2 tickets to generate statistics for next draw");
        } else if (type.equals(GeneratorType.FULL) && generatesLeft < 4) {
            throw new ValidationException("You need 4 tickets to generate FULL number statistics");
        }
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

    private Map<Integer, Double> calculateExtraDrawnCoefficients(NumberStatistics statsForDraws, NumberStatistics statsForAll, Integer draws, Integer maxDraws, Lottery lottery) {
        Map<Integer, Double> numberCoefficients = new HashMap<>();
        for (int i = 1; i <= lottery.getMaxExtraNumber(); i++) {
            numberCoefficients.put(i, 0.0);
        }
        statsForDraws.getExtraStats().forEach(ns ->
                {
                    NumberStat nsAll = statsForAll.getExtraStats().stream().filter(nsA -> nsA.getNumber() == ns.getNumber()).findFirst().get();
                    numberCoefficients.put(ns.getNumber(), numberCoefficients.get(ns.getNumber()) +
                            ((1.0 * ns.getDrawn() / draws) * (1.0 * nsAll.getDrawn() / maxDraws) + (1.0 * nsAll.getCyclesNotDrawn() / maxDraws)));
                }
        );
        numberCoefficients.forEach((k, v) -> numberCoefficients.put(k, numberCoefficients.get(k)));
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
                double drawnInDraws = rs.getDraws() == 0 ? 0 : 1.0 * statsForDraws.getStats().stream().filter(s -> s.getNumber() == finalI).findFirst().get().getDrawn() / rs.getDraws();
                double drawnInAll = allRs.getDraws() == 0 ? 0 :1.0 * statsForAll.getStats().stream().filter(s -> s.getNumber() == finalI).findFirst().get().getDrawn() / allRs.getDraws();
                double drawsRangePercent = (rs.getDraws() / (1.0 * draws * interval)) / (allRs.getDraws() / (1.0 * maxDraws * interval));
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

    private Map<Integer, Double> calculateExtraRangeCoefficient(Lottery lottery, int draws, Integer maxDraws,
                                                           Double rangeMultiplier, NumberStatistics statsForDraws, NumberStatistics statsForAll) {
        RangeStatistics rangeForDraws = lotteryTensStats(lottery.getId(), draws, 10, 2);
        RangeStatistics rangeForAll = lotteryTensStats(lottery.getId(), maxDraws, 10, 2);

        Map<Integer, Double> rangeDrawsCoefficient = new HashMap<>();
        for (int i = 1; i <= lottery.getMaxExtraNumber(); i++) {
            rangeDrawsCoefficient.put(i, 0.0);
        }

        rangeForDraws.getExtraStats().forEach(rs -> {
            int interval = rs.getTo() - rs.getFrom() + 1;
            RangeStat allRs = rangeForAll.getExtraStats().stream().filter(rsA -> rsA.getFrom() == rs.getFrom() && rsA.getTo() == rs.getTo())
                    .findFirst().orElseThrow(() -> new RuntimeException("Different range interval length for all draws"));
            for (int i = rs.getFrom(); i <= rs.getTo(); i++) {
                int finalI = i;
                double drawnInDraws = rs.getDraws() == 0 ? 0 : 1.0 * statsForDraws.getExtraStats().stream().filter(s -> s.getNumber() == finalI).findFirst().get().getDrawn() / rs.getDraws();
                double drawnInAll = allRs.getDraws() == 0 ? 0 : 1.0 * statsForAll.getExtraStats().stream().filter(s -> s.getNumber() == finalI).findFirst().get().getDrawn() / allRs.getDraws();
                double drawsRangePercent = (rs.getDraws() / (1.0 * draws * interval)) / (allRs.getDraws() / (1.0 * maxDraws * interval));
                double drawnPercent = drawnInAll == 0 ? 0 : 1.0 * drawnInDraws / drawnInAll;
                rangeDrawsCoefficient.put(i, drawnPercent == 0 ? 0 :
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

    private Map<Integer, Double> calculateExtraPairCoefficient(Lottery lottery, int draws, Integer maxDraws) {
        Map<Integer, Double> pairCoefficients = new HashMap<>();
        MostCommonStatistics pairsDraws = lotteryMostCommon(lottery.getId(), 2, draws, 2);
        MostCommonStatistics pairsAll = lotteryMostCommon(lottery.getId(), 2, maxDraws, 2);
        for (int i = 1; i <= lottery.getMaxExtraNumber(); i++) {
            pairCoefficients.put(i, 0.0);
        }

        pairsDraws.getExtraMostCommons().forEach(mc ->
                mc.getNumbers().forEach(n -> pairCoefficients.put(n, pairCoefficients.get(n) + (1.0 * mc.getDrawn() / draws)))
        );
        pairsAll.getExtraMostCommons().forEach(mc ->
                mc.getNumbers().forEach(n -> pairCoefficients.put(n, pairCoefficients.get(n) + (1.0 * mc.getDrawn() / maxDraws)))
        );
        pairCoefficients.forEach((k, v) -> pairCoefficients.put(k, pairCoefficients.get(k) * 2 / lottery.getDraw()));

        return pairCoefficients;
    }
}
