package com.turleylabs.algo.trader.kata;

import com.turleylabs.algo.trader.kata.framework.Bar;
import com.turleylabs.algo.trader.kata.framework.Holding;
import com.turleylabs.algo.trader.kata.framework.SimpleMovingAverage;

public class BoughtAbove50State extends OwnStockState {

    public BoughtAbove50State(AlgoData algoData) {
        super(algoData);
    }

    @Override
    public AlgorithmState execute(RefactorMeAlgorithm algo, Bar bar) {
        Holding holding = algo.getPortfolio().get(algoData.symbol);
        double change = algo.computePercentageChanged(holding.getAveragePrice(), bar.getPrice());

        if (droppedMoreThan7PctBelow50DayAndBoughtAbove50(bar, algoData.movingAverage50)) {
            algo.log(String.format("Sell %s loss of 50 day. Gain %.4f. Vix %.4f", algoData.symbol, change, algoData.lastVix.getClose()));
            algo.liquidate(algoData.symbol);
            return new NoStockState(this.algoData);
        }
        return super.execute(algo, bar);
    }

    private boolean droppedMoreThan7PctBelow50DayAndBoughtAbove50(Bar bar, SimpleMovingAverage movingAverage50) {
        return bar.getPrice() < (movingAverage50.getValue() * .93);
    }

}
