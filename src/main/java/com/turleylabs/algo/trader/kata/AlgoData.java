package com.turleylabs.algo.trader.kata;

import com.turleylabs.algo.trader.kata.framework.CBOE;
import com.turleylabs.algo.trader.kata.framework.SimpleMovingAverage;

import java.time.LocalDate;

public class AlgoData {
    public String symbol = "TQQQ";
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

    public AlgoData() {
    }
}