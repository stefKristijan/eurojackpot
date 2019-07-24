package com.kstefancic.lotterymaster.service;

import com.kstefancic.lotterymaster.domain.MostCommonStatistics;
import com.kstefancic.lotterymaster.domain.NumberCoefficient;
import com.kstefancic.lotterymaster.domain.NumberStatistics;
import com.kstefancic.lotterymaster.domain.RangeStatistics;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface StatisticsService {

    NumberStatistics lotteryNumberStats(int lotteryId, Integer draws);

    RangeStatistics lotteryTensStats(int lotteryId, Integer draws, Integer range, Integer extraRange);

    MostCommonStatistics lotteryMostCommon(int lotteryId, int quantity, Integer draws, int extraQuantity);

    List<NumberCoefficient> nextDrawNumberCoefficients(int lotteryId, Integer draws, Integer maxDraws, Double rangeMultiplier, Double mcMultiplier, Double drawnMultiplier, Integer range, Double lastDrawDivider);

}
