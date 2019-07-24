package com.kstefancic.lotterymaster.controller;

import com.kstefancic.lotterymaster.service.CustomerService;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/generator")
public class GeneratorController {

    private final CustomerService customerService;

    public GeneratorController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping("buy-tickets")
    public HttpEntity<?> checkout(
            @RequestParam("token") String token,
            @RequestParam("sku") String sku
    ){
        return ResponseEntity.ok(customerService.charge(token, sku));
    }

    @GetMapping("tickets")
    public HttpEntity<?> tickets(){
        return ResponseEntity.ok(customerService.getTickets());
    }
}
