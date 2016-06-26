package com.boxedmeatrevolution.brains;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * Created by aidan on 2016-06-26.
 */
public class FibonacciTask extends Task {

    public FibonacciTask(int numberIndex) {
        super();
        _numberIndex = numberIndex;
    }

    @Override
    public Serializable call() throws IOException {
        System.out.println("Prelim F" + _numberIndex);
        if (_numberIndex <= 1) {
            System.out.println("Calculating F" + _numberIndex + " = " + 1);
            return Integer.valueOf(1);
        }

        List<Serializable> previousNumbers = requestTasks(Arrays.<Task>asList(new FibonacciTask(_numberIndex - 1), new FibonacciTask(_numberIndex - 2)));
        Integer previous = (Integer) previousNumbers.get(0);
        Integer secondPrevious = (Integer) previousNumbers.get(1);
        Integer result = previous + secondPrevious;
        System.out.println("Calculating F" + _numberIndex + " = " + result);
        return result;
    }

    private final int _numberIndex;
}
