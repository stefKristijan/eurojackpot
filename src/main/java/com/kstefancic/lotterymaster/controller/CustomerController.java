package com.kstefancic.lotterymaster.controller;

import javax.validation.Valid;

import com.kstefancic.lotterymaster.domain.User;
import com.kstefancic.lotterymaster.service.CustomerService;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/customer")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService service){
        this.customerService = service;
    }

    @PostMapping
    public HttpEntity<?> createCustomer(
        @RequestBody @Valid User user
    ) {
        return ResponseEntity.ok(customerService.create(user));
    }

    @GetMapping("verify")
    public void verify(
            @RequestParam("email") String email,
            @RequestParam("verificationCode") String code
    ){
        customerService.verifyUser(email, code);
    }

    @GetMapping("resend-verification-code")
    public void resendVerificationCode(
            @RequestParam("email") String email,
            @RequestParam("password") String password
    ){
        customerService.resendVerificationCode(email, password);
    }

}
