package com.kstefancic.eurojackpot;

import com.kstefancic.eurojackpot.domain.MostCommon;
import com.kstefancic.eurojackpot.domain.NumberStatistics;
import com.kstefancic.eurojackpot.domain.RangeStatistics;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface StatisticsService {

    NumberStatistics lotteryNumberStats(int lotteryId, Integer draws);

    RangeStatistics lotteryTensStats(int lotteryId, Integer draws, Integer range, Integer extraRange);

    List<MostCommon> lotteryMostCommon(int lotteryId, int quantity, Integer draws);
}
