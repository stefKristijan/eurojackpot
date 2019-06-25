package com.kstefancic.eurojackpot;

import com.kstefancic.eurojackpot.service.HlLotoDrawsUpdateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import static com.kstefancic.eurojackpot.domain.Constants.*;

@Component
public class LotteryUpdateTasks {

    private static final Logger logger = LoggerFactory.getLogger(LotteryUpdateTasks.class);

    private final HlLotoDrawsUpdateService hlLotoDrawsUpdateService;
    private final LotteryService lotteryService;

    public LotteryUpdateTasks(HlLotoDrawsUpdateService hlLotoDrawsUpdateService, LotteryService lotteryService) {
        this.hlLotoDrawsUpdateService = hlLotoDrawsUpdateService;
        this.lotteryService = lotteryService;
    }

    @PostConstruct
    private void initializeLotteries() {
        logger.info("Initializing lotteries that are not yet initialized");
        lotteryService.initializeLotteries();
    }

    @Scheduled(cron = "* */30 20-23 * * 0,4")
    public void updateLoto6od45() {
        logger.info("Updating " + LOTO_6_OD_45_UK + " draws");
        hlLotoDrawsUpdateService.updateDraws(LOTO_6_OD_45_UK, LOTO_6_OD_45_URL);
    }

    @Scheduled(cron = "* */30 20-23 * * 3,6")
    public void updateLoto7od39() {
        logger.info("Updating " + LOTO_7_OD_39_UK + " draws");
        hlLotoDrawsUpdateService.updateDraws(LOTO_7_OD_39_UK, LOTO_7_OD_39_URL);
    }
}
