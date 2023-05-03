package com.akscardgames.cardgamesproject.gamesRelated;

public class ResultData {
    private final String name;
    private final int profit;

    public ResultData(String name, int profit) {
        this.name = name;
        this.profit = profit;
    }

    public String getName() {
        return name;
    }

    public int getProfit() {
        return profit;
    }
}
