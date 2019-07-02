package com.kstefancic.eurojackpot.controller;

import com.kstefancic.eurojackpot.LotteryService;
import com.kstefancic.eurojackpot.domain.Constants;
import com.kstefancic.eurojackpot.service.EurojackpotDrawsService;
import com.kstefancic.eurojackpot.service.HlLotoDrawsHlLotoService;
import com.kstefancic.eurojackpot.service.PskLotteriesService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("update")
public class UpdateController {

    private final LotteryService lotteryService;
    private final PskLotteriesService service;

    public UpdateController(LotteryService lotteryService, PskLotteriesService service) {
        this.lotteryService = lotteryService;
        this.service = service;
    }

    @GetMapping("test")
    public void testUpdate(){
        service.updateDraws();
    }
}
