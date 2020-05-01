package com.turleylabs.algo.trader.kata;

import com.turleylabs.algo.trader.kata.framework.Bar;

public class NoStockState implements AlgorithmState {
    @Override
    public AlgorithmState execute(RefactorMeAlgorithm algo, Bar bar) {

        if (shouldBuy(algo, bar)) {
            buy(algo, bar);
            return new OwnStockState();
        }

        return this;
    }

    private boolean shouldBuy(RefactorMeAlgorithm algo, Bar bar) {
        return bar.getPrice() > algo.movingAverage10.getValue()
                && algo.movingAverage10.getValue() > algo.movingAverage21.getValue()
                && algo.movingAverage10.getValue() > algo.previousMovingAverage10
                && algo.movingAverage21.getValue() > algo.previousMovingAverage21
                && (double) (algo.lastVix.getClose()) < 19.0
                && !(algo.shouldSellAtGain(bar))
                && algo.computePercentageChanged(algo.movingAverage10.getValue(), bar.getPrice()) < 0.07;
    }

    private void buy(RefactorMeAlgorithm algo, Bar bar) {
        algo.log(String.format("Buy %s Vix %.4f. above 10 MA %.4f", algo.symbol, algo.lastVix.getClose(), algo.computePercentageChanged(algo.movingAverage10.getValue(), bar.getPrice())));
        double amount = 1.0;
        algo.setHoldings(algo.symbol, amount);
        algo.boughtBelow50 = bar.getPrice() < algo.movingAverage50.getValue();
    }
}
