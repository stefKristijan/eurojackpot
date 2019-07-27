package com.kstefancic.lotterymaster.domain;

public class TicketOrder {

    private TicketItem ticketItem;
    private String paymentMethodId;

    public TicketItem getTicketItem() {
        return ticketItem;
    }

    public void setTicketItem(TicketItem ticketItem) {
        this.ticketItem = ticketItem;
    }

    public String getPaymentMethodId() {
        return paymentMethodId;
    }

    public void setPaymentMethodId(String paymentMethodId) {
        this.paymentMethodId = paymentMethodId;
    }
}
