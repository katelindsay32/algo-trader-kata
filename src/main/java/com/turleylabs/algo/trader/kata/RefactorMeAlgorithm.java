package com.turleylabs.algo.trader.kata;

import com.turleylabs.algo.trader.kata.framework.*;

import java.util.ArrayList;
import java.util.Map;

public class RefactorMeAlgorithm extends BaseAlgorithm {

    public AlgoData algoData;

    public boolean boughtBelow50;
    public boolean tookProfits;
    public AlgorithmState algorithmState;

    public void initialize() {
        this.setStartDate(2010, 3, 23);  //Set Start Date
        this.setEndDate(2020, 03, 06);

        algoData = new AlgoData();

        algorithmState = new NoStockState(algoData);
        this.setCash(100000);             //Set Strategy Cash
        algoData.movingAverage200 = this.SMA(algoData.symbol, 200);
        algoData.movingAverage50 = this.SMA(algoData.symbol, 50);
        algoData.movingAverage21 = this.SMA(algoData.symbol, 21);
        algoData.movingAverage10 = this.SMA(algoData.symbol, 10);

    }

    protected void onData(Slice data) {
        if (data.getCBOE("VIX") != null) {
            algoData.lastVix = data.getCBOE("VIX");
        }
        if (algoData.previous == getDate()) return;

        if (!algoData.movingAverage200.isReady()) return;

        Bar bar = data.get(algoData.symbol);
        if (bar == null) {
            this.log(String.format("No data for symbol %s", algoData.symbol));
            return;
        }
        if (tookProfits) {
            resetTookProfits(bar);
        } else{
            this.algorithmState = this.algorithmState.execute(this, bar);
        }


        algoData.previous = getDate();
        algoData.previousMovingAverage50 = algoData.movingAverage50.getValue();
        algoData.previousMovingAverage21 = algoData.movingAverage21.getValue();
        algoData.previousMovingAverage10 = algoData.movingAverage10.getValue();
        algoData.previousPrice = bar.getPrice();
    }

    public boolean shouldSellAtGain(Bar bar) {
        return bar.getPrice() >= (algoData.movingAverage50.getValue() * 1.15) && bar.getPrice() >= (algoData.movingAverage200.getValue() * 1.40);
    }


    public double computePercentageChanged(double startingPrice, double newPrice) {
        return (newPrice - startingPrice) / startingPrice;
    }

    private void resetTookProfits(Bar bar) {
        if (bar.getPrice() < algoData.movingAverage10.getValue()) {
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
                " symbol='" + movingAverage.getSymbol() +
                " value='" + movingAverage.getValue() +
                " isReady='" + movingAverage.isReady() +
                "}\n";
    }

    private String TradeToString(Trade trade) {
        return "Trade{" +
                " symbol='" + trade.getSymbol() +
                " averagePrice='" + trade.getAveragePrice() +
                " numberOfShares='" + trade.getNumberOfShares() +
                " date='" + trade.getDate() +
                "}\n";
    }

    private String TradesArrayListToString(ArrayList<Trade> trades) {
        StringBuilder arrayAsString = new StringBuilder("{");
        for (Trade trade : trades) {
            arrayAsString.append(TradeToString(trade));
        }
        arrayAsString.append("}\n");
        return arrayAsString.toString();
    }

    private String CBOEToString(CBOE cboe) {
        return "CBOE{" +
                "close='" + cboe.getClose() +
                "}\n";
    }

    private String HoldingToString(Holding holding) {
        return "Holding{" +
                "averagePrice='" + holding.getAveragePrice() +
                "quantity='" + holding.getQuantity() +
                "}\n";
    }

    private String PortfolioToString(Map<String, Holding> portfolio) {

        StringBuilder mapAsString = new StringBuilder("{");
        for (String key : portfolio.keySet()) {
            mapAsString.append(key + "=" + HoldingToString(portfolio.get(key)) + ", ");
        }
        mapAsString.append("}\n");
        return mapAsString.toString();
    }

    @Override
    public String toString() {
        return "RefactorMeAlgorithm{" +
                "symbol='" + algoData.symbol + '\'' +
                ", movingAverage200=" + MovingAverageToString(algoData.movingAverage200) +
                ", movingAverage50=" + MovingAverageToString(algoData.movingAverage50) +
                ", movingAverage21=" + MovingAverageToString(algoData.movingAverage21) +
                ", movingAverage10=" + MovingAverageToString(algoData.movingAverage10) +
                ", previousMovingAverage50=" + algoData.previousMovingAverage50 +
                ", previousMovingAverage21=" + algoData.previousMovingAverage21 +
                ", previousMovingAverage10=" + algoData.previousMovingAverage10 +
                ", previousPrice=" + algoData.previousPrice +
                ", previous=" + algoData.previous +
                ", lastVix=" + CBOEToString(algoData.lastVix) +
                ", boughtBelow50=" + boughtBelow50 +
                ", tookProfits=" + tookProfits +
                ", portfolio=" + PortfolioToString(portfolio) +
                ", trades=" + TradesArrayListToString(trades) +
                "}\n";
    }
    //endregion
}
