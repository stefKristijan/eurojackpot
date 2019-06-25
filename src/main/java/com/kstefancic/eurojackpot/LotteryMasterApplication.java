package com.kstefancic.eurojackpot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LotteryMasterApplication {

    public static void main(String[] args) {
        SpringApplication.run(LotteryMasterApplication.class, args);
    }

}
