package com.kstefancic.eurojackpot;

import com.kstefancic.eurojackpot.service.Loto6od45Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class LotteryUpdateTasks {

    Logger logger = LoggerFactory.getLogger(LotteryUpdateTasks.class);

    private final Loto6od45Service loto6od45Service;
    private final LotteryService lotteryService;

    public LotteryUpdateTasks(Loto6od45Service loto6od45Service, LotteryService lotteryService) {
        this.loto6od45Service = loto6od45Service;
        this.lotteryService = lotteryService;
    }

    @PostConstruct
    private void initializeLotteries(){
        logger.info("Initializing lotteries that are not yet initialized");
        lotteryService.initializeLotteries();
    }

    @Scheduled(cron = "* */30 20-23 * * 0,4")
    public void updateLoto6od45(){
        logger.info("Updating Loto 6 od 45 draws");
        loto6od45Service.updateDraws();
    }
}
