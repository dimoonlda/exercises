package ua.kiev.dimoon.test.concurrent.java.impatient.ex12;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.LongAccumulator;

public class Exercise12 {

    private static BlockingQueue<File> queue = new ArrayBlockingQueue<>(10);
    private static final String DUMMY_FILE_NAME = "DUMMY_FILE_NAME";
    private static final File DUMMY_FILE = new File(DUMMY_FILE_NAME);
    private static final Map<String, LongAccumulator> DUMMY_MAP = new HashMap<>();

    static {
        DUMMY_MAP.put(DUMMY_FILE_NAME, null);
    }

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
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                        });
                queue.put(DUMMY_FILE);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private static class Consumer implements Callable<Map<String, Integer>> {

        private WordsFinder wordsFinder = new WordsFinder();
        private Map<String, Integer> localMap;
        private File file;

        public Consumer(File file) {
            this.file = file;
        }

        class WordsFinder {
            void findWords(File source) {
                localMap = new HashMap<>();
                try (BufferedReader br = new BufferedReader(new FileReader(source))) {

                    String currentLine;

                    while ((currentLine = br.readLine()) != null) {
                        Arrays.stream(currentLine.split(" "))
                                .map(String::trim)
                                .forEach(word -> {
                                    if (null != localMap.putIfAbsent(word, 1)) {
                                        localMap.compute(word, (key, value) -> value + 1);
                                    }
                                });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public Map<String, Integer> call() {
                wordsFinder.findWords(file);
                return localMap;
        }
    }


    public static void main(String[] args) throws ExecutionException, InterruptedException {
        Thread producerThread = new Thread(new Producer("/home/dlutai/Downloads/java-thread-programming/source/"));
        producerThread.start();
        ExecutorService pool = Executors.newCachedThreadPool();
        List<Future<Map<String, Integer>>> futures = new ArrayList<>();
        long start = System.currentTimeMillis();
        while (true) {
            File file = queue.take();
            if (file.equals(DUMMY_FILE)) {
                break;
            }
            futures.add(pool.submit(new Consumer(file)));
        }

        futures.stream()
                .map(future -> {
                    Map<String, Integer> result = null;
                    try {
                        result = future.get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                    return result;
                })
                .reduce(new HashMap<>(), (map1, map2) -> {
                    map2.forEach((key2, value2) -> map1.merge(key2, value2, (oldVal, newVal) -> oldVal + newVal));
                    return map1;
                })
                .entrySet().stream()
                .sorted(Comparator.comparingInt(entry -> -1 * entry.getValue()))
                .limit(10L)
                .forEach(entry -> System.out.println(entry.getKey() + "=" + entry.getValue()));

        System.out.println("End. Took: " + (System.currentTimeMillis() - start));

        pool.shutdownNow();
    }


}
