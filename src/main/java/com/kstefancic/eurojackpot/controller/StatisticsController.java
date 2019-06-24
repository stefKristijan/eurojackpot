package com.kstefancic.eurojackpot.controller;

import com.kstefancic.eurojackpot.LotteryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("statistics")
public class StatisticsController {

    final LotteryService lotteryService;

    public StatisticsController(LotteryService lotteryService) {
        this.lotteryService = lotteryService;
    }

    @RequestMapping()
    public ResponseEntity<?> calculateNextDraw(){
        return ResponseEntity.ok(lotteryService.calculateDraw());
    }
}
