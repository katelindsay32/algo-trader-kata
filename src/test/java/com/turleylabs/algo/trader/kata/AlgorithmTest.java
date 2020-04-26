package com.turleylabs.algo.trader.kata;

import org.approvaltests.Approvals;
import org.junit.Test;

public class AlgorithmTest {

    @Test
    public void algorithmExecutesTrades() {
        RefactorMeAlgorithm refactorAlgorithm = new RefactorMeAlgorithm();

        refactorAlgorithm.run();

        Approvals.verify(refactorAlgorithm);
    }

}
