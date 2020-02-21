package com.kstefancic.lotterymaster.controller;

import com.kstefancic.lotterymaster.domain.Draw;
import com.kstefancic.lotterymaster.service.LotteryService;
import com.kstefancic.lotterymaster.service.EurojackpotDrawsService;
import com.kstefancic.lotterymaster.service.HlLotteriesDrawsService;
import com.kstefancic.lotterymaster.service.PskLotteriesService;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping
    public void testUpdate(
        @RequestParam("password") String password
    ) {
        if (password.equals("UPdatePassword1!")) {
            service.updateDraws(50, true);
            hlLotteriesDrawsService.updateDraws(LOTO_7_OD_35_UK, LOTO_7_OD_35_URL);
            hlLotteriesDrawsService.updateDraws(LOTO_6_OD_45_UK, LOTO_6_OD_45_URL);
            eurojackpotDrawsService.updateDraws();
        }
    }

    @GetMapping("win-for-life")
    public void updateWinForLife(
        @RequestParam("password") String password
    ){
        if(password.equals("WinForLifePass!")){
            lotteryService.updateWinForLifeDraws();
        }
    }

    @PostMapping("{lotteryId}/add-draw/{password}")
    public void addDrawToLottery(
            @PathVariable("lotteryId") int lotteryId,
            @PathVariable("password") String password,
            @RequestBody Draw draw
            ){
        if (password.equals("@dd1Dr@w")) {
            lotteryService.addDrawToLottery(lotteryId, draw);
        }
    }

    @PostMapping("test-play")
    public void testPlay(@RequestParam("password") String password){
        if(password.equals("!Helloh@Tes%"))
        lotteryService.playWinForLife();
    }
}
