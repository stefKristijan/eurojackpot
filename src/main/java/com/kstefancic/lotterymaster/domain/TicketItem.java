package com.kstefancic.lotterymaster.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.HashMap;
import java.util.Map;

public enum TicketItem {

    TICKET_1("1 LotteryMaster Generator TicketItem", 50,1),
    TICKET_10("10 LotteryMaster Generator Tickets", 400, 10),
    TICKET_20("20 LotteryMaster Generator Tickets", 700, 20),
    TICKET_50("50 LotteryMaster Generator Tickets", 1500, 50),
    TICKET_100("100 LotteryMaster Generator Tickets", 2500, 100);

    private static Map<String, TicketItem> namesMap = new HashMap<String, TicketItem>(5);

    static {
        namesMap.put("TICKET_1", TICKET_1);
        namesMap.put("TICKET_10", TICKET_10);
        namesMap.put("TICKET_20", TICKET_20);
        namesMap.put("TICKET_50", TICKET_50);
        namesMap.put("TICKET_100", TICKET_100);
    }

    @JsonCreator
    public static TicketItem forValue(String value) {
        return namesMap.get(value.toUpperCase());
    }

    @JsonValue
    public String toValue() {
        for (Map.Entry<String, TicketItem> entry : namesMap.entrySet()) {
            if (entry.getValue() == this)
                return entry.getKey();
        }
        return null; // or fail
    }


    private final String description;
    private final int amount;
    private final int tickets;

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
