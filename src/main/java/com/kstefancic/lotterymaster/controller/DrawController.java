package com.kstefancic.lotterymaster.controller;

import javax.validation.Valid;
import java.util.Comparator;
import java.util.List;

import com.kstefancic.lotterymaster.domain.Generator;
import com.kstefancic.lotterymaster.domain.Lottery;
import com.kstefancic.lotterymaster.service.LotteryService;
import com.kstefancic.lotterymaster.service.StatisticsService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
        if (lottery.getEnglishName() != null) {
            lottery.setName(lottery.getEnglishName());
        }
        return ResponseEntity.ok(lottery);
    }

    @GetMapping("")
    public ResponseEntity<?> lotteryList() {
        List<Lottery> all = lotteryService.findAll();
        all.forEach(l -> {
            l.setDraws(null);
            l.setResultUrls(null);
            if (l.getEnglishName() != null) {
                l.setName(l.getEnglishName());
            }
        });
        all.sort(Comparator.comparing(Lottery::getName));
        return ResponseEntity.ok(all);
    }

    @GetMapping("/{id}/draws")
    public ResponseEntity<?> lotteryDraws(
        @PathVariable("id") Integer lotteryId,
        Pageable pageable
    ) {
        return ResponseEntity.ok(lotteryService.findByLottery(lotteryId, pageable));
    }

    @GetMapping("test-gen")
    public void test(@RequestParam("password") String password) {
        if (password.equals("TestPass!"))
            lotteryService.checkWinForLifeNumbers();
    }

    @GetMapping("/{id}/number-stats")
    public ResponseEntity<?> lotteryNumberStats(
        @PathVariable("id") int lotteryId,
        @RequestParam(name = "draws", required = false) Integer draws
    ) {
        return ResponseEntity.ok(statisticsService.lotteryNumberStats(lotteryId, draws));
    }

    @GetMapping("/{id}/range-stats")
    public ResponseEntity<?> lotteryTensStats(
        @PathVariable("id") int lotteryId,
        @RequestParam(name = "draws", required = false) Integer draws,
        @RequestParam(name = "range", required = false) Integer range,
        @RequestParam(name = "extraRange", required = false) Integer extraRange
    ) {
        return ResponseEntity.ok(statisticsService.lotteryTensStats(lotteryId, draws, range, extraRange));
    }

    @GetMapping("/{id}/most-common")
    public ResponseEntity<?> lotteryMostCommon(
        @PathVariable("id") int lotteryId,
        @RequestParam(name = "draws", required = false) Integer draws,
        @RequestParam("quantity") int quantity,
        @RequestParam("extraQuantity") int extraQuantity
    ) {
        return ResponseEntity.ok(statisticsService.lotteryMostCommon(lotteryId, quantity, draws, extraQuantity));
    }

    @PostMapping("/{id}/calculate")
    public ResponseEntity<?> getNextDrawNumberCoefficients(
        @PathVariable("id") int lotteryId,
        @RequestBody @Valid Generator generator
    ) {
        return ResponseEntity.ok(statisticsService.nextDrawNumberCoefficients(lotteryId, generator, null));
    }

}
