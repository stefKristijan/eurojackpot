package com.kstefancic.lotterymaster.domain;

public class TicketOrder {

    private String ticketItem;
    private String paymentMethodId;

    public String getTicketItem() {
        return ticketItem;
    }

    public void setTicketItem(String ticketItem) {
        this.ticketItem = ticketItem;
    }

    public String getPaymentMethodId() {
        return paymentMethodId;
    }

    public void setPaymentMethodId(String paymentMethodId) {
        this.paymentMethodId = paymentMethodId;
    }
}
