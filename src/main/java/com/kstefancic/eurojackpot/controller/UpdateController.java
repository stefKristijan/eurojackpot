package com.kstefancic.eurojackpot.controller;

import com.kstefancic.eurojackpot.LotteryService;
import com.kstefancic.eurojackpot.domain.Constants;
import com.kstefancic.eurojackpot.service.HlLotoDrawsUpdateService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("update")
public class UpdateController {

    private final LotteryService lotteryService;
    private final HlLotoDrawsUpdateService service;

    public UpdateController(LotteryService lotteryService, HlLotoDrawsUpdateService service) {
        this.lotteryService = lotteryService;
        this.service = service;
    }

    @GetMapping("test")
    public void testUpdate(){
        service.updateDraws(Constants.LOTO_7_OD_39_UK, Constants.LOTO_7_OD_39_URL);
    }
}
