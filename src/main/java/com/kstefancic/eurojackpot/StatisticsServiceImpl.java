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
import java.util.List;

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
        Pageable pageable = PageRequest.of(0, draws != null && draws > 0 ? draws : Integer.MAX_VALUE, Sort.by("date").descending());
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
    public List<MostCommon> lotteryMostCommon(int lotteryId, int quantity, Integer draws) {
        Lottery lottery = lotteryRepository.findById(lotteryId)
                .orElseThrow(() -> new EntityNotFoundException(Constants.NOT_FOUND_LOTTERY));
        List<MostCommon> mostCommonStats = new ArrayList<>();
        if(quantity < 2 || quantity > lottery.getDraw()){
            quantity = 2;
        }
        return drawRepository.mostCommonStats(em, lotteryId, quantity, draws != null && draws > 0 ? draws : Integer.MAX_VALUE);
    }
}
