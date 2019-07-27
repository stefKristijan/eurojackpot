package com.kstefancic.lotterymaster.service;

import com.kstefancic.lotterymaster.domain.TicketOrder;

import java.util.Map;

public interface StripeService {

    Map<String, Object> orderTickets(TicketOrder ticketOrder);

    Map<String, Object> confirmPayment(String paymentIntentId, int tickets);
}
