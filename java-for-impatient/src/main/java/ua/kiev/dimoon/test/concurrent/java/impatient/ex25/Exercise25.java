package ua.kiev.dimoon.test.concurrent.java.impatient.ex25;

import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class Exercise25 {
    private static LongAdder counter = new LongAdder();

    static {
        counter.increment();
    }

    private static Random rnd = new Random();

    private static <T extends Long> CompletableFuture<T> repeat(Supplier<T> action, Predicate<T> until) {
        return CompletableFuture.supplyAsync(action).thenApply(t -> {
            System.out.println(t);
            counter.add(t);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (!until.test(t)) {
                repeat(action, until);
            } else {
                System.out.println(true);
            }
            return t;
        });
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        CompletableFuture<Long> repeat = repeat(() -> (long) rnd.nextInt(10), (t) -> {
            System.out.println("predicate: " + t);
            return t == 5L;
        })
                .whenComplete((result, throwable) -> System.out.println("Times: " + counter.sum()));
    }
}
