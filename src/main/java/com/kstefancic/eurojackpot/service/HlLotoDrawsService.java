package com.kstefancic.eurojackpot.service;

import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface HlLotoDrawsService {

    void updateDraws(String uniqueLotteryName, String lotteryResultUrl);
}
