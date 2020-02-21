package com.kstefancic.lotterymaster.service;

import com.kstefancic.lotterymaster.domain.Draw;
import com.kstefancic.lotterymaster.domain.Lottery;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface LotteryService {

    void initializeLotteries();

    List<Lottery> findAll();

    List<Draw> findByLottery(Integer lotteryId, Pageable pageRequest);

    Lottery findById(Integer lotteryId);

    void addDrawToLottery(int lotteryId, Draw draw);

    void checkWinForLifeNumbers();

    void playWinForLife();

    void playGreeceKinoLotto();

    void checkItaly2090Result();

    void updateWinForLifeDraws();
}
