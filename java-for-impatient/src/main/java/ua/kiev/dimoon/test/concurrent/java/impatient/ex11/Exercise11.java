package ua.kiev.dimoon.test.concurrent.java.impatient.ex11;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.LongAdder;

public class Exercise11 {

    private static BlockingQueue<File> queue = new ArrayBlockingQueue<>(10);
    private static BlockingQueue<Map<String, LongAdder>> queue2 = new ArrayBlockingQueue<>(10);
    private static final String DUMMY_FILE_NAME = "DUMMY_FILE_NAME";
    private static final File DUMMY_FILE = new File(DUMMY_FILE_NAME);
    private static final Map<String, LongAdder> DUMMY_MAP = new HashMap<>();

    static {
        DUMMY_MAP.put(DUMMY_FILE_NAME, null);
    }

    private final static ForkJoinPool consumerPool = new ForkJoinPool(3);

    static class Producer implements Runnable {
        private final String rootPath;

        public Producer(String rootPath) {
            this.rootPath = rootPath;
        }

        @Override
        public void run() {
            try {
                Files.walk(Paths.get(rootPath))
                        .map(Path::toFile)
                        .filter(File::isFile)
                        .forEach(file -> {
                            try {
                                queue.put(file);
                                System.out.println("Producer put file: " + file.getName());
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                        });
                queue.put(DUMMY_FILE);
                System.out.println("Producer put file: " + DUMMY_FILE.getName());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    static class Consumer implements Callable<String> {

        private WordsFinder wordsFinder = new WordsFinder();
        private Map<String, LongAdder> localMap = new ConcurrentHashMap<>();

        class WordsFinder {
            void findWords(File source) {

                try (BufferedReader br = new BufferedReader(new FileReader(source))) {

                    String currentLine;

                    while ((currentLine = br.readLine()) != null) {
                        Arrays.stream(currentLine.split(" "))
                                .map(String::trim)
                                .forEach(word -> {
                                    localMap.putIfAbsent(word, new LongAdder());
                                    localMap.computeIfPresent(word, (key, value) -> {
                                        value.increment();
                                        return value;
                                    });
                                });
                    }
                    try {
                        queue2.put(Collections.unmodifiableMap(localMap));
                        System.out.println("Consumer put localMap: ");
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public String call() {
            while (true) {
                File file = null;
                try {
                    file = queue.take();
                    System.out.println("Consumer take file: " + file.getName());
                    if (file.equals(DUMMY_FILE)) {
                        if (consumerPool.getRunningThreadCount() <= 1) {
                            queue2.put(DUMMY_MAP);
                        }
                        queue.put(DUMMY_FILE);
                        Thread.currentThread().interrupt();
                        System.out.println("Consumer put DUMMY_MAP: ");
                        break;
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                wordsFinder.findWords(file);
            }
            return null;
        }
    }

    static class FinalConsumer implements Runnable {

        private Map<String, Long> wordsToCounterResult = new ConcurrentHashMap<>();

        @Override
        public void run() {
            while (true) {
                try {
                    Map<String, LongAdder> wordsToCounter = queue2.take();
                    //System.out.println("FinalConsumer took localMap: " + wordsToCounter.keySet());
                    if (wordsToCounter.keySet().contains(DUMMY_FILE_NAME)) {
                        break;
                    }
                    wordsToCounter.forEach((key, value) -> {
//                                wordsToCounterResult.putIfAbsent(key, 0L);
//                                wordsToCounterResult.computeIfPresent(key, (k, v) -> value.sum() + v);
                                wordsToCounterResult.merge(key, value.sum(), (value1, value2) -> value1 + value2);
                    }
                    );

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            wordsToCounterResult.entrySet().stream()
                    .sorted(Comparator.comparingLong(entry -> -1 * entry.getValue()))
                    .limit(10)
                    .forEach(System.out::println);
        }
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        Thread producerThread = new Thread(new Producer("/home/dlutai/Downloads/java-thread-programming/source/"));
        producerThread.start();
        Thread finalConsumer = new Thread(new FinalConsumer());
        finalConsumer.start();
        long start = System.currentTimeMillis();
        consumerPool.invokeAll(
                Arrays.asList(new Consumer(), new Consumer(), new Consumer())
        );

        finalConsumer.join();
        System.out.println("End. Took: " + (System.currentTimeMillis() - start));
    }
}
