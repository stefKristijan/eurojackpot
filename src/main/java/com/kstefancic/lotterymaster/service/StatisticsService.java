package com.kstefancic.lotterymaster.service;

import com.kstefancic.lotterymaster.domain.*;
import org.springframework.stereotype.Service;

@Service
public interface StatisticsService {

    NumberStatistics lotteryNumberStats(int lotteryId, Integer draws);

    RangeStatistics lotteryTensStats(int lotteryId, Integer draws, Integer range, Integer extraRange);

    MostCommonStatistics lotteryMostCommon(int lotteryId, int quantity, Integer draws, int extraQuantity);

    CoefficientStatistics nextDrawNumberCoefficients(int lotteryId, Generator generator);

}
