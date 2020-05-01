package com.turleylabs.algo.trader.kata;

import com.turleylabs.algo.trader.kata.framework.Bar;
import com.turleylabs.algo.trader.kata.framework.CBOE;
import com.turleylabs.algo.trader.kata.framework.Holding;
import com.turleylabs.algo.trader.kata.framework.SimpleMovingAverage;

public class OwnStockState implements AlgorithmState {

    protected AlgoData algoData;

    public OwnStockState(AlgoData algoData) {

        this.algoData = algoData;
    }

    @Override
    public AlgorithmState execute(RefactorMeAlgorithm algo, Bar bar) {
        // check if sell
        // sell
        // return no stock state
        Holding holding = algo.getPortfolio().get(algoData.symbol);
        double change = algo.computePercentageChanged(holding.getAveragePrice(), bar.getPrice());

        if (highVolitality(algoData.lastVix)) {
            algo.log(String.format("Sell %s high volatility. Gain %.4f. Vix %.4f", algoData.symbol, change, algoData.lastVix.getClose()));
            algo.liquidate(algoData.symbol);
            return new NoStockState(this.algoData);
        } else {
            if (tenDayAverageLessThan3PctBelow21Day(algoData.movingAverage10, algoData.movingAverage21)) {
                algo.log(String.format("Sell %s 10 day below 21 day. Gain %.4f. Vix %.4f", algoData.symbol, change, algoData.lastVix.getClose()));
                algo.liquidate(algoData.symbol);
                return new NoStockState(this.algoData);
            } else {
                if (algo.shouldSellAtGain(bar)) {
                    algo.log(String.format("Sell %s taking profits. Gain %.4f. Vix %.4f", algoData.symbol, change, algoData.lastVix.getClose()));
                    algo.liquidate(algoData.symbol);
                    return new TookProfitState(this.algoData);
                }
            }
        }

        return this;
    }

    private boolean tenDayAverageLessThan3PctBelow21Day(SimpleMovingAverage movingAverage10, SimpleMovingAverage movingAverage21) {
        return movingAverage10.getValue() < 0.97 * movingAverage21.getValue();
    }

    private boolean highVolitality(CBOE lastVix) {
        return (double) (lastVix.getClose()) > 22.0;
    }


}
