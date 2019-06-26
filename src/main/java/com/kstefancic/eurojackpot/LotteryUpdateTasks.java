package com.kstefancic.eurojackpot;

import com.kstefancic.eurojackpot.service.EurojackpotDrawsService;
import com.kstefancic.eurojackpot.service.HlLotoDrawsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import static com.kstefancic.eurojackpot.domain.Constants.*;

@Component
public class LotteryUpdateTasks {

    private static final Logger logger = LoggerFactory.getLogger(LotteryUpdateTasks.class);

    private final HlLotoDrawsService hlLotoDrawsService;
    private final LotteryService lotteryService;
    private final EurojackpotDrawsService eurojackpotDrawsService;

    public LotteryUpdateTasks(HlLotoDrawsService hlLotoDrawsService, LotteryService lotteryService, EurojackpotDrawsService eurojackpotDrawsService) {
        this.hlLotoDrawsService = hlLotoDrawsService;
        this.lotteryService = lotteryService;
        this.eurojackpotDrawsService = eurojackpotDrawsService;
    }

//    @PostConstruct
    private void initializeLotteries() {
        logger.info("Initializing lotteries that are not yet initialized");
        lotteryService.initializeLotteries();
    }

    @Scheduled(cron = "0 0/30 20-23 * * 0,4")
    public void updateLoto6od45() {
        logger.info("Updating " + LOTO_6_OD_45_UK + " draws");
        hlLotoDrawsService.updateDraws(LOTO_6_OD_45_UK, LOTO_6_OD_45_URL);
    }

    @Scheduled(cron = "0 0/30 20-23 * * 3,6")
    public void updateLoto7od39() {
        logger.info("Updating " + LOTO_7_OD_35_UK + " draws");
        hlLotoDrawsService.updateDraws(LOTO_7_OD_35_UK, LOTO_7_OD_35_URL);
    }

    @Scheduled(cron = "0 0 0 * * 6")
    public void updateEurojackpot(){
        logger.info("Updating Eurojackpot draws");
        eurojackpotDrawsService.updateDraws();
    }
}
