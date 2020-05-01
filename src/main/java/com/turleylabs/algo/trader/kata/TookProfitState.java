package com.turleylabs.algo.trader.kata;

import com.turleylabs.algo.trader.kata.framework.Bar;

public class TookProfitState implements AlgorithmState {
    private AlgoData algoData;

    public TookProfitState(AlgoData algoData) {
        this.algoData = algoData;
    }

    @Override
    public AlgorithmState execute(RefactorMeAlgorithm algo, Bar bar) {
        if (bar.getPrice() < algoData.movingAverage10.getValue()) {
            return new NoStockState(algoData);
        }
        return this;
    }

}
