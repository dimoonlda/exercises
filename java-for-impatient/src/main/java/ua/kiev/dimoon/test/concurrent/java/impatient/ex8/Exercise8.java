package ua.kiev.dimoon.test.concurrent.java.impatient.ex8;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Exercise8 {
    private static AtomicLong counter = new AtomicLong();
    private static LongAdder counter2 = new LongAdder();

    public static void main(String[] args) {
        IntFunction<Callable<Integer>> taskSupplier = (i) -> (Callable<Integer>) () -> {
            IntStream.range(0, 100_000).forEach((j) -> counter.incrementAndGet());
            return i;
        };
        List<Callable<Integer>> tasks = IntStream.range(0, 1000).mapToObj(taskSupplier).collect(Collectors.toList());
        long start = System.currentTimeMillis();
        ForkJoinPool.commonPool().invokeAll(tasks);
        System.out.println("Took for AtomicLong: " + (System.currentTimeMillis() - start) + " ms.");


        taskSupplier = (i) -> (Callable<Integer>) () -> {
            IntStream.range(0, 100_000).forEach((j) -> counter2.increment());
            return i;
        };
        tasks = IntStream.range(0, 1000).mapToObj(taskSupplier).collect(Collectors.toList());
        start = System.currentTimeMillis();
        ForkJoinPool.commonPool().invokeAll(tasks);
        System.out.println("Took for LongAdder: " + (System.currentTimeMillis() - start) + " ms.");

    }
}
