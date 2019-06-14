package com.kstefancic.eurojackpot;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@Controller
@RequestMapping("update")
public class TestController {

    private final EurojackpotService eurojackpotService;

    public TestController(EurojackpotService eurojackpotService) {
        this.eurojackpotService = eurojackpotService;
    }

    @GetMapping
    public ResponseEntity<?> updateDraws() throws IOException {
        return ResponseEntity.ok(eurojackpotService.updateDraws());
    }

    @GetMapping("/draws")
    public String getDraws(Model model) {
        model.addAttribute("draws", eurojackpotService.findAll());
        return "index.html";
    }
}
