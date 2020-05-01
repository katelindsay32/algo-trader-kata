package com.turleylabs.algo.trader.kata;

import com.turleylabs.algo.trader.kata.framework.Bar;

public class NoStockState implements AlgorithmState {
    private AlgoData algoData;

    public NoStockState(AlgoData algoData) {
        this.algoData = algoData;
    }

    @Override
    public AlgorithmState execute(RefactorMeAlgorithm algo, Bar bar) {

        if (shouldBuy(algo, bar)) {
            buy(algo, bar);
            return new OwnStockState(algoData);
        }

        return this;
    }

    private boolean shouldBuy(RefactorMeAlgorithm algo, Bar bar) {
        return bar.getPrice() > algoData.movingAverage10.getValue()
                && algoData.movingAverage10.getValue() > algoData.movingAverage21.getValue()
                && algoData.movingAverage10.getValue() > algoData.previousMovingAverage10
                && algoData.movingAverage21.getValue() > algoData.previousMovingAverage21
                && (double) (algoData.lastVix.getClose()) < 19.0
                && !(algo.shouldSellAtGain(bar))
                && algo.computePercentageChanged(algoData.movingAverage10.getValue(), bar.getPrice()) < 0.07;
    }

    private void buy(RefactorMeAlgorithm algo, Bar bar) {
        algo.log(String.format("Buy %s Vix %.4f. above 10 MA %.4f", algoData.symbol, algoData.lastVix.getClose(), algo.computePercentageChanged(algoData.movingAverage10.getValue(), bar.getPrice())));
        double amount = 1.0;
        algo.setHoldings(algoData.symbol, amount);
        algo.boughtBelow50 = bar.getPrice() < algoData.movingAverage50.getValue();
    }
}
