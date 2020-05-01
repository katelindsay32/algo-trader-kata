package com.turleylabs.algo.trader.kata;

import com.turleylabs.algo.trader.kata.framework.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Map;

public class RefactorMeAlgorithm extends BaseAlgorithm {

    String symbol = "TQQQ";
    SimpleMovingAverage movingAverage200;
    SimpleMovingAverage movingAverage50;
    SimpleMovingAverage movingAverage21;
    SimpleMovingAverage movingAverage10;
    double previousMovingAverage50;
    double previousMovingAverage21;
    double previousMovingAverage10;
    double previousPrice;
    LocalDate previous;
    CBOE lastVix;
    boolean boughtBelow50;
    boolean tookProfits;

    public void initialize() {
        this.setStartDate(2010, 3, 23);  //Set Start Date
        this.setEndDate(2020, 03, 06);

        this.setCash(100000);             //Set Strategy Cash

        movingAverage200 = this.SMA(symbol, 200);
        movingAverage50 = this.SMA(symbol, 50);
        movingAverage21 = this.SMA(symbol, 21);
        movingAverage10 = this.SMA(symbol, 10);

    }

    protected void onData(Slice data) {
        if (data.getCBOE("VIX") != null) {
            lastVix = data.getCBOE("VIX");
        }
        if (previous == getDate()) return;

        if (!movingAverage200.isReady()) return;

        Bar bar = data.get(symbol);
        if (bar == null) {
            this.log(String.format("No data for symbol %s", symbol));
            return;
        }
        if (tookProfits) {
            resetTookProfits(bar);
        } else if (doNotOwn(symbol)) {

            if (shouldBuy(bar)) {
                buy(bar);
            }
        } else {
            Holding holding = portfolio.get(symbol);
            double change = computePercentageChanged(holding.getAveragePrice(), bar.getPrice());

            if (droppedMoreThan7PctBelow50DayAndBoughtAbove50(bar)) {
                this.log(String.format("Sell %s loss of 50 day. Gain %.4f. Vix %.4f", symbol, change, lastVix.getClose()));
                this.liquidate(symbol);
            } else {
                if (highVolitality()) {
                    this.log(String.format("Sell %s high volatility. Gain %.4f. Vix %.4f", symbol, change, lastVix.getClose()));
                    this.liquidate(symbol);
                } else {
                    if (tenDayAverageLessThan3PctBelow21Day()) {
                        this.log(String.format("Sell %s 10 day below 21 day. Gain %.4f. Vix %.4f", symbol, change, lastVix.getClose()));
                        this.liquidate(symbol);
                    } else {
                        if (shouldSellAtGain(bar)) {
                            this.log(String.format("Sell %s taking profits. Gain %.4f. Vix %.4f", symbol, change, lastVix.getClose()));
                            this.liquidate(symbol);
                            tookProfits = true;
                        }
                    }
                }
            }
        }


        previous = getDate();
        previousMovingAverage50 = movingAverage50.getValue();
        previousMovingAverage21 = movingAverage21.getValue();
        previousMovingAverage10 = movingAverage10.getValue();
        previousPrice = bar.getPrice();
    }

    private boolean shouldSellAtGain(Bar bar) {
        return bar.getPrice() >= (movingAverage50.getValue() * 1.15) && bar.getPrice() >= (movingAverage200.getValue() * 1.40);
    }

    private boolean tenDayAverageLessThan3PctBelow21Day() {
        return movingAverage10.getValue() < 0.97 * movingAverage21.getValue();
    }

    private boolean highVolitality() {
        return (double) (lastVix.getClose()) > 22.0;
    }

    private boolean droppedMoreThan7PctBelow50DayAndBoughtAbove50(Bar bar) {
        return bar.getPrice() < (movingAverage50.getValue() * .93) && !boughtBelow50;
    }

    private double computePercentageChanged(double startingPrice, double newPrice) {
        return (newPrice - startingPrice) / startingPrice;
    }

    private void buy(Bar bar) {
        this.log(String.format("Buy %s Vix %.4f. above 10 MA %.4f", symbol, lastVix.getClose(), computePercentageChanged(movingAverage10.getValue(), bar.getPrice())));
        double amount = 1.0;
        this.setHoldings(symbol, amount);

        boughtBelow50 = bar.getPrice() < movingAverage50.getValue();
    }

    private void resetTookProfits(Bar bar) {
        if (bar.getPrice() < movingAverage10.getValue()) {
            tookProfits = false;
        }
    }

    private boolean shouldBuy(Bar bar) {
        return bar.getPrice() > movingAverage10.getValue()
                && movingAverage10.getValue() > movingAverage21.getValue()
                && movingAverage10.getValue() > previousMovingAverage10
                && movingAverage21.getValue() > previousMovingAverage21
                && (double) (lastVix.getClose()) < 19.0
                && !(shouldSellAtGain(bar))
                && computePercentageChanged(movingAverage10.getValue(), bar.getPrice()) < 0.07;
    }

    private boolean doNotOwn(String symbol) {
        return portfolio.getOrDefault(symbol, Holding.Default).getQuantity() == 0;
    }

    //region ToStrings

    private String MovingAverageToString(SimpleMovingAverage movingAverage) {
        return "SimpleMovingAverage{" +
                "symbol='" + movingAverage.getSymbol() +
                "value='" + movingAverage.getValue() +
                "isReady='" + movingAverage.isReady() +
                '}';
    }

    private String TradeToString(Trade trade) {
        return "Trade{" +
                "symbol='" + trade.getSymbol() +
                "averagePrice='" + trade.getAveragePrice() +
                "numberOfShares='" + trade.getNumberOfShares() +
                "date='" + trade.getDate() +
                '}';
    }

    private String TradesArrayListToString(ArrayList<Trade> trades) {
        StringBuilder arrayAsString = new StringBuilder("{");
        for (Trade trade : trades) {
            arrayAsString.append(TradeToString(trade));
        }
        arrayAsString.append("}");
        String result = arrayAsString.toString();
        return result;
    }

    private String CBOEToString(CBOE cboe) {
        return "CBOE{" +
                "close='" + cboe.getClose() +
                '}';
    }

    private String HoldingToString(Holding holding) {
        return "Holding{" +
                "averagePrice='" + holding.getAveragePrice() +
                "quantity='" + holding.getQuantity() +
                '}';
    }

    private String PortfolioToString(Map<String, Holding> portfolio) {

        StringBuilder mapAsString = new StringBuilder("{");
        for (String key : portfolio.keySet()) {
            mapAsString.append(key + "=" + HoldingToString(portfolio.get(key)) + ", ");
        }
        mapAsString.append("}");
        return mapAsString.toString();
    }

    @Override
    public String toString() {
        return "RefactorMeAlgorithm{" +
                "symbol='" + symbol + '\'' +
                ", movingAverage200=" + MovingAverageToString(movingAverage200) +
                ", movingAverage50=" + MovingAverageToString(movingAverage50) +
                ", movingAverage21=" + MovingAverageToString(movingAverage21) +
                ", movingAverage10=" + MovingAverageToString(movingAverage10) +
                ", previousMovingAverage50=" + previousMovingAverage50 +
                ", previousMovingAverage21=" + previousMovingAverage21 +
                ", previousMovingAverage10=" + previousMovingAverage10 +
                ", previousPrice=" + previousPrice +
                ", previous=" + previous +
                ", lastVix=" + CBOEToString(lastVix) +
                ", boughtBelow50=" + boughtBelow50 +
                ", tookProfits=" + tookProfits +
                ", portfolio=" + PortfolioToString(portfolio) +
                ", trades=" + TradesArrayListToString(trades) +
                '}';
    }
    //endregion
}
