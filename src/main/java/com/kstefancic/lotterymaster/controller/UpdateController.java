package com.kstefancic.lotterymaster.controller;

import com.kstefancic.lotterymaster.service.LotteryService;
import com.kstefancic.lotterymaster.service.EurojackpotDrawsService;
import com.kstefancic.lotterymaster.service.HlLotteriesDrawsService;
import com.kstefancic.lotterymaster.service.PskLotteriesService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.kstefancic.lotterymaster.domain.Constants.*;

@RestController
@RequestMapping("update")
public class UpdateController {

    private final LotteryService lotteryService;
    private final PskLotteriesService service;
    private final HlLotteriesDrawsService hlLotteriesDrawsService;
    private final EurojackpotDrawsService eurojackpotDrawsService;

    public UpdateController(LotteryService lotteryService, PskLotteriesService service, HlLotteriesDrawsService hlLotteriesDrawsService, EurojackpotDrawsService eurojackpotDrawsService) {
        this.lotteryService = lotteryService;
        this.service = service;
        this.hlLotteriesDrawsService = hlLotteriesDrawsService;
        this.eurojackpotDrawsService = eurojackpotDrawsService;
    }

    @GetMapping("update")
    public void testUpdate(
        @RequestParam("password") String password
    ) {
        if (password.equals("UPdatePassword1!")) {
            service.updateDraws();
            hlLotteriesDrawsService.updateDraws(LOTO_7_OD_35_UK, LOTO_7_OD_35_URL);
            hlLotteriesDrawsService.updateDraws(LOTO_6_OD_45_UK, LOTO_6_OD_45_URL);
            eurojackpotDrawsService.updateDraws();
        }
    }
}
