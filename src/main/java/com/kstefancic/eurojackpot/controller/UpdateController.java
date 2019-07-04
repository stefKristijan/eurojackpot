package com.kstefancic.eurojackpot.controller;

import com.kstefancic.eurojackpot.LotteryService;
import com.kstefancic.eurojackpot.domain.Constants;
import com.kstefancic.eurojackpot.service.HlLotteriesDrawsService;
import com.kstefancic.eurojackpot.service.PskLotteriesService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("update")
public class UpdateController {

    private final LotteryService lotteryService;
    private final PskLotteriesService service;
    private final HlLotteriesDrawsService hlLotteriesDrawsService;

    public UpdateController(LotteryService lotteryService, PskLotteriesService service, HlLotteriesDrawsService hlLotteriesDrawsService) {
        this.lotteryService = lotteryService;
        this.service = service;
        this.hlLotteriesDrawsService = hlLotteriesDrawsService;
    }

//    @GetMapping("test")
//    public void testUpdate(){
//        service.updateDraws();
//    }
}
