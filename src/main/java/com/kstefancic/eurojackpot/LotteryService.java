package com.kstefancic.eurojackpot;

import com.kstefancic.eurojackpot.domain.Draw;
import com.kstefancic.eurojackpot.domain.Lottery;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface LotteryService {

    void initializeLotteries();

    List<Lottery> findAll();

    Draw calculateDraw();

    List<Draw> findByLottery(Integer lotteryId, Pageable pageRequest);

    Lottery findById(Integer lotteryId);
}
