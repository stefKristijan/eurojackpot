package com.kstefancic.lotterymaster.domain;

public enum TicketItem {

    TICKET_1("1 LotteryMaster Generator TicketItem", 50,1),
    TICKET_10("10 LotteryMaster Generator Tickets", 400, 10),
    TICKET_20("20 LotteryMaster Generator Tickets", 700, 20),
    TICKET_50("50 LotteryMaster Generator Tickets", 1500, 50),
    TICKET_100("100 LotteryMaster Generator Tickets", 2500, 100);

    private String description;
    private int amount;
    private int tickets;

    private TicketItem(String description, int amount, int tickets){
        this.description = description;
        this.amount = amount;
        this.tickets = tickets;
    }

    public int getTickets() {
        return tickets;
    }

    public String getDescription() {
        return description;
    }

    public int getAmount() {
        return amount;
    }
}
