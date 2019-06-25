package com.kstefancic.eurojackpot;

import com.kstefancic.eurojackpot.domain.MostCommonStatistics;
import com.kstefancic.eurojackpot.domain.NumberStatistics;
import com.kstefancic.eurojackpot.domain.RangeStatistics;
import org.springframework.stereotype.Service;

@Service
public interface StatisticsService {

    NumberStatistics lotteryNumberStats(int lotteryId, Integer draws);

    RangeStatistics lotteryTensStats(int lotteryId, Integer draws, Integer range, Integer extraRange);

    MostCommonStatistics lotteryMostCommon(int lotteryId, int quantity, Integer draws, int extraQuantity);
}
