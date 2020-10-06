package com.kstefancic.lotterymaster;

import com.kstefancic.lotterymaster.service.EurojackpotDrawsService;
import com.kstefancic.lotterymaster.service.GermaniaLotteriesService;
import com.kstefancic.lotterymaster.service.HlLotteriesDrawsService;
import com.kstefancic.lotterymaster.service.LotteryService;
import com.kstefancic.lotterymaster.service.PskLotteriesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import static com.kstefancic.lotterymaster.domain.Constants.*;

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
        logger.debug("Initializing lotteries that are not yet initialized");
        lotteryService.initializeLotteries();
    }

//    @Scheduled(cron = "0 0/30 20-23 * * 0,4")
    public void updateLoto6od45() {
        logger.debug("Updating " + LOTO_6_OD_45_UK + " draws");
        hlLotteriesDrawsService.updateDraws(LOTO_6_OD_45_UK, LOTO_6_OD_45_URL);
    }

//    @Scheduled(cron = "0 0/30 20-23 * * 3,6")
    public void updateLoto7od39() {
        logger.debug("Updating " + LOTO_7_OD_35_UK + " draws");
        hlLotteriesDrawsService.updateDraws(LOTO_7_OD_35_UK, LOTO_7_OD_35_URL);
    }

    @Scheduled(cron = "0 0 0 * * 6")
    public void updateEurojackpot(){
        logger.debug("Updating Eurojackpot draws");
        eurojackpotDrawsService.updateDraws(true);
    }

    @Scheduled(cron = "0 0/10 * * * *")
    public void updatePskLotteries(){
        logger.debug("Updating PSK lotteries");
        pskLotteriesService.updateDraws(5, false);
    }

    @Scheduled(cron = "0/30 * * * * *")
    public void updateGreekKenoAndItalianKino(){
        germaniaLotteriesService.updateDraws();
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void deleteOldGreekKenoAndItalianKinoData(){
        logger.info("Deleting germania lotteries from 2 days ago");
        germaniaLotteriesService.delete2DaysAgoLotteries();
    }

    @Scheduled(cron = "0 20/5 7-23 * * *")
    public void updateWinForLifeDraws(){
        lotteryService.updateWinForLifeDraws();
    }

//    Testing Win For Life success rate
//    @Scheduled(cron = "0 51 7-23 * * *")

    public void checkResultOfWinForLife(){
        lotteryService.checkWinForLifeNumbers();
    }
//    @Scheduled(cron = "0 4/15 8-22 * * *")

    public void playGreeceKinoLotto(){
        lotteryService.playGreeceKinoLotto();
    }
//    @Scheduled(cron = "0 4/15 * * * *")

    public void checkItaly2090Result(){
        lotteryService.checkItaly2090Result();
    }
//    @Scheduled(cron = "0 52 7-23 * * *")
//    public void playWinForLife(){
//        lotteryService.playWinForLife();
//    }
//
}
