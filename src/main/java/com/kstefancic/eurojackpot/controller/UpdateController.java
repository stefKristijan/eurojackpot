package com.kstefancic.eurojackpot.controller;

import com.kstefancic.eurojackpot.LotteryService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;

@Controller
@RequestMapping("update")
public class UpdateController {

    private final LotteryService lotteryService;

    public UpdateController(LotteryService lotteryService) {
        this.lotteryService = lotteryService;
    }

    @GetMapping
    public ResponseEntity<?> updateDraws() throws IOException {
        return ResponseEntity.ok(lotteryService.updateDraws());
    }

    @GetMapping("/draws")
    public String getDraws(Model model) {
        model.addAttribute("draws", lotteryService.findAll());
        return "index.html";
    }
}
