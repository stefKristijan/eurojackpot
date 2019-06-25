package com.kstefancic.eurojackpot.controller;

import com.kstefancic.eurojackpot.LotteryService;
import com.kstefancic.eurojackpot.StatisticsService;
import com.kstefancic.eurojackpot.domain.Lottery;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lottery")
public class DrawController {

    final private LotteryService lotteryService;
    final private StatisticsService statisticsService;

    public DrawController(LotteryService lotteryService, StatisticsService statisticsService) {
        this.lotteryService = lotteryService;
        this.statisticsService = statisticsService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> lottery(
            @PathVariable("id") Integer lotteryId
    ) {
        Lottery lottery = lotteryService.findById(lotteryId);
        lottery.setDraws(null);
        lottery.setResultUrls(null);
        return ResponseEntity.ok(lottery);
    }

    @GetMapping("")
    public ResponseEntity<?> lotteryList(){
        List<Lottery> all = lotteryService.findAll();
        all.forEach(l -> {
            l.setDraws(null);
            l.setResultUrls(null);
        });
        return ResponseEntity.ok(all);
    }

    @GetMapping("/{id}/draws")
    public ResponseEntity<?> lotteryDraws(
            @PathVariable("id") Integer lotteryId,
            Pageable pageable
    ) {
        return ResponseEntity.ok(lotteryService.findByLottery(lotteryId, pageable));
    }

    @GetMapping("/{id}/number-stats")
    public ResponseEntity<?> lotteryNumberStats(
            @PathVariable("id") int lotteryId,
            @RequestParam(name = "draws", required = false) Integer draws
    ){
        return ResponseEntity.ok(statisticsService.lotteryNumberStats(lotteryId, draws));
    }

    @GetMapping("/{id}/range-stats")
    public ResponseEntity<?> lotteryTensStats(
            @PathVariable("id") int lotteryId,
            @RequestParam(name = "draws", required = false) Integer draws,
            @RequestParam(name = "range", required = false) Integer range,
            @RequestParam(name = "extraRange", required = false) Integer extraRange
    ){
        return ResponseEntity.ok(statisticsService.lotteryTensStats(lotteryId, draws, range, extraRange));
    }

    @GetMapping("/{id}/most-common")
    public ResponseEntity<?> lotteryMostCommon(
            @PathVariable("id") int lotteryId,
            @RequestParam(name = "draws", required = false) Integer draws,
            @RequestParam("quantity") int quantity,
            @RequestParam("extraQuantity") int extraQuantity
    ){
        return ResponseEntity.ok(statisticsService.lotteryMostCommon(lotteryId, quantity, draws, extraQuantity));
    }
}
