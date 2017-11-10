package ua.kiev.dimoon.test.concurrent.java.impatient.ex9;

import java.util.concurrent.atomic.LongAccumulator;

public class Exercise9 {

    private static void accumulate(LongAccumulator accumulator) {
        accumulator.accumulate(-12);
        accumulator.accumulate(12);
        accumulator.accumulate(22);
        accumulator.accumulate(32);
        accumulator.accumulate(1200);
        accumulator.accumulate(166666666662L);
        accumulator.accumulate(1288);
        accumulator.accumulate(2277);
        accumulator.accumulate(32555555555L);
    }

    public static void main(String[] args) {
        LongAccumulator maxAccum = new LongAccumulator((x, y) -> x >= y ? x : y, 0);
        accumulate(maxAccum);
        System.out.println("Max: " + maxAccum.get());

        LongAccumulator minAccum = new LongAccumulator((x, y) -> x >= y ? y : x, 0);
        accumulate(minAccum);
        System.out.println("Min: " + minAccum.get());
    }
}
