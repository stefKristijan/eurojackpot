package com.kstefancic.lotterymaster.service;

import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface HlLotteriesDrawsService {

    void updateDraws(String uniqueLotteryName, String lotteryResultUrl);
}
