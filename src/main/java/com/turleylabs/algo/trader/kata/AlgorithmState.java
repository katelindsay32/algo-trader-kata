package com.turleylabs.algo.trader.kata;

import com.turleylabs.algo.trader.kata.framework.Bar;

public interface AlgorithmState {
    AlgorithmState execute(RefactorMeAlgorithm algo, Bar bar);
}
