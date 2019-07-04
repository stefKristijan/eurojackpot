package com.kstefancic.eurojackpot;

import com.kstefancic.eurojackpot.service.EurojackpotDrawsService;
import com.kstefancic.eurojackpot.service.GermaniaLotteriesService;
import com.kstefancic.eurojackpot.service.HlLotteriesDrawsService;
import com.kstefancic.eurojackpot.service.PskLotteriesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import static com.kstefancic.eurojackpot.domain.Constants.*;

@Component
public class LotteryUpdateTasks {

    private static final Logger logger = LoggerFactory.getLogger(LotteryUpdateTasks.class);

    private final HlLotteriesDrawsService hlLotteriesDrawsService;
    private final LotteryService lotteryService;
    private final EurojackpotDrawsService eurojackpotDrawsService;
    private final PskLotteriesService pskLotteriesService;
    private final GermaniaLotteriesService germaniaLotteriesService;

    public LotteryUpdateTasks(HlLotteriesDrawsService hlLotteriesDrawsService, LotteryService lotteryService, EurojackpotDrawsService eurojackpotDrawsService, PskLotteriesService pskLotteriesService, GermaniaLotteriesService germaniaLotteriesService) {
        this.hlLotteriesDrawsService = hlLotteriesDrawsService;
        this.lotteryService = lotteryService;
        this.eurojackpotDrawsService = eurojackpotDrawsService;
        this.pskLotteriesService = pskLotteriesService;
        this.germaniaLotteriesService = germaniaLotteriesService;
    }

//    @PostConstruct
    private void initializeLotteries() {
        logger.info("Initializing lotteries that are not yet initialized");
        lotteryService.initializeLotteries();
    }

    @Scheduled(cron = "0 0/30 20-23 * * 0,4")
    public void updateLoto6od45() {
        logger.info("Updating " + LOTO_6_OD_45_UK + " draws");
        hlLotteriesDrawsService.updateDraws(LOTO_6_OD_45_UK, LOTO_6_OD_45_URL);
    }

    @Scheduled(cron = "0 0/30 20-23 * * 3,6")
    public void updateLoto7od39() {
        logger.info("Updating " + LOTO_7_OD_35_UK + " draws");
        hlLotteriesDrawsService.updateDraws(LOTO_7_OD_35_UK, LOTO_7_OD_35_URL);
    }

    @Scheduled(cron = "0 0 0 * * 6")
    public void updateEurojackpot(){
        logger.info("Updating Eurojackpot draws");
        eurojackpotDrawsService.updateDraws();
    }

    @Scheduled(cron = "0 0/15 * * * *")
    public void updatePskLotteries(){
        logger.info("Updating PSK lotteries");
        pskLotteriesService.updateDraws();
    }

    @Scheduled(cron = "0/30 * * * * *")
    public void updateGreekKenoAndItalianKino(){
        logger.info("Updating Germania lotteries");
        germaniaLotteriesService.updateDraws();
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void deleteOldGreekKenoAndItalianKinoData(){
        logger.info("Deleting germania lotteries from 2 days ago");
        germaniaLotteriesService.delete2DaysAgoLotteries();
    }
}
