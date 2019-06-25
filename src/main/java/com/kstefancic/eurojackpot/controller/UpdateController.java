package com.kstefancic.eurojackpot.controller;

import com.kstefancic.eurojackpot.LotteryService;
import com.kstefancic.eurojackpot.service.Loto6od45Service;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;

@Controller
@RequestMapping("update")
public class UpdateController {

    private final LotteryService lotteryService;
    private final Loto6od45Service service;

    public UpdateController(LotteryService lotteryService, Loto6od45Service service) {
        this.lotteryService = lotteryService;
        this.service = service;
    }

    @GetMapping("test")
    public void testUpdate(){
        service.updateDraws();
    }
}
