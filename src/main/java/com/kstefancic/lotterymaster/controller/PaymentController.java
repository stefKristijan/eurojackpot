package com.kstefancic.lotterymaster.controller;


import com.kstefancic.lotterymaster.domain.TicketOrder;
import com.kstefancic.lotterymaster.service.StripeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("api/payment")
public class PaymentController {

    private final StripeService stripeService;

    public PaymentController(StripeService stripeService) {
        this.stripeService = stripeService;
    }

    @PostMapping("order-tickets")
    public ResponseEntity<?> orderTickets(
            @RequestBody @Valid TicketOrder ticketOrder
    ) {
        return ResponseEntity.ok(stripeService.orderTickets(ticketOrder));
    }

    @PostMapping("confirm")
    public ResponseEntity<?> confirmPayment(
            @RequestParam("paymentIntentId") String paymentIntentId,
            @RequestParam("tickets") int tickets
    ) {
        return ResponseEntity.ok(stripeService.confirmPayment(paymentIntentId, tickets));
    }
}
