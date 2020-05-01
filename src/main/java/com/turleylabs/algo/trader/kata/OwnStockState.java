package com.turleylabs.algo.trader.kata;

import com.turleylabs.algo.trader.kata.framework.Bar;
import com.turleylabs.algo.trader.kata.framework.CBOE;
import com.turleylabs.algo.trader.kata.framework.Holding;
import com.turleylabs.algo.trader.kata.framework.SimpleMovingAverage;

public class OwnStockState implements AlgorithmState {

    @Override
    public AlgorithmState execute(RefactorMeAlgorithm algo, Bar bar) {
        // check if sell
        // sell
        // return no stock state
        Holding holding = algo.getPortfolio().get(algo.symbol);
        double change = algo.computePercentageChanged(holding.getAveragePrice(), bar.getPrice());

        if (droppedMoreThan7PctBelow50DayAndBoughtAbove50(bar, algo.movingAverage50, algo.boughtBelow50)) {
            algo.log(String.format("Sell %s loss of 50 day. Gain %.4f. Vix %.4f", algo.symbol, change, algo.lastVix.getClose()));
            algo.liquidate(algo.symbol);
            return new NoStockState();
        } else {
            if (highVolitality(algo.lastVix)) {
                algo.log(String.format("Sell %s high volatility. Gain %.4f. Vix %.4f", algo.symbol, change, algo.lastVix.getClose()));
                algo.liquidate(algo.symbol);
                return new NoStockState();
            } else {
                if (tenDayAverageLessThan3PctBelow21Day(algo.movingAverage10, algo.movingAverage21)) {
                    algo.log(String.format("Sell %s 10 day below 21 day. Gain %.4f. Vix %.4f", algo.symbol, change, algo.lastVix.getClose()));
                    algo.liquidate(algo.symbol);
                    return new NoStockState();
                } else {
                    if (algo.shouldSellAtGain(bar)) {
                        algo.log(String.format("Sell %s taking profits. Gain %.4f. Vix %.4f", algo.symbol, change, algo.lastVix.getClose()));
                        algo.liquidate(algo.symbol);
                        algo.tookProfits = true;
                        return new NoStockState();
                    }
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

    private boolean droppedMoreThan7PctBelow50DayAndBoughtAbove50(Bar bar, SimpleMovingAverage movingAverage50, boolean boughtBelow50) {
        return bar.getPrice() < (movingAverage50.getValue() * .93) && !boughtBelow50;
    }

}
