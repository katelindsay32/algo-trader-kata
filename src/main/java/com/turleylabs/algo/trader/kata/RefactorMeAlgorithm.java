package com.turleylabs.algo.trader.kata;

import com.turleylabs.algo.trader.kata.framework.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Map;

public class RefactorMeAlgorithm extends BaseAlgorithm {

    String symbol = "TQQQ";
    public SimpleMovingAverage movingAverage200;
    public SimpleMovingAverage movingAverage50;
    public SimpleMovingAverage movingAverage21;
    public SimpleMovingAverage movingAverage10;
    public double previousMovingAverage50;
    public double previousMovingAverage21;
    public double previousMovingAverage10;
    public double previousPrice;
    public LocalDate previous;
    public CBOE lastVix;
    public boolean boughtBelow50;
    public boolean tookProfits;
    public AlgorithmState algorithmState = new NoStockState();

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
        } else{
            this.algorithmState = this.algorithmState.execute(this, bar);
        }


        previous = getDate();
        previousMovingAverage50 = movingAverage50.getValue();
        previousMovingAverage21 = movingAverage21.getValue();
        previousMovingAverage10 = movingAverage10.getValue();
        previousPrice = bar.getPrice();
    }

    public boolean shouldSellAtGain(Bar bar) {
        return bar.getPrice() >= (movingAverage50.getValue() * 1.15) && bar.getPrice() >= (movingAverage200.getValue() * 1.40);
    }


    public double computePercentageChanged(double startingPrice, double newPrice) {
        return (newPrice - startingPrice) / startingPrice;
    }

    private void resetTookProfits(Bar bar) {
        if (bar.getPrice() < movingAverage10.getValue()) {
            tookProfits = false;
        }
    }

    private boolean doNotOwn(String symbol) {
        return portfolio.getOrDefault(symbol, Holding.Default).getQuantity() == 0;
    }

    public void log(String msg){
        super.log(msg);
    }

    public void setHoldings(String symbol, double amt){
        super.setHoldings(symbol, amt);
    }

    public Map<String, Holding> getPortfolio(){
        return super.portfolio;
    }

    public void liquidate(String symbol){
        super.liquidate(symbol);
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
