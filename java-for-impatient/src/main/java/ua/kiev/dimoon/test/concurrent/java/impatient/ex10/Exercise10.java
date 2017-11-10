package ua.kiev.dimoon.test.concurrent.java.impatient.ex10;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.*;

public class Exercise10 {

    private static BlockingQueue<File> queue = new ArrayBlockingQueue<>(10);
    private static final String DUMMY_FILE_NAME = "DUMMY_FILE_NAME";
    private static final File DUMMY_FILE = new File(DUMMY_FILE_NAME);
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

    static class Consumer implements Callable<String> {

        private String keyWord;
        private KeyWordFinder keyWordFinder = new KeyWordFinder();

        public Consumer(String keyWord) {
            this.keyWord = keyWord;
        }

        class KeyWordFinder {
            void findWord(String keyWord, File source) {
                try (BufferedReader br = new BufferedReader(new FileReader(source))) {

                    String currentLine;

                    while ((currentLine = br.readLine()) != null) {
                        if (currentLine.contains(keyWord)) {
                            System.out.println("Line: " + currentLine);
                        }
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
                    if (file.equals(DUMMY_FILE)) {
                        consumerPool.shutdownNow();
                        break;
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                keyWordFinder.findWord(keyWord, file);
            }
            return null;
        }
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        String keyWord = "String";
        Thread producerThread = new Thread(new Producer("/home/dlutai/Downloads/java-thread-programming"));
        producerThread.start();
        long start = System.currentTimeMillis();
        consumerPool.invokeAll(
                Arrays.asList(new Consumer(keyWord), new Consumer(keyWord), new Consumer(keyWord))
        );
        System.out.println("End. Took: " + (System.currentTimeMillis() - start));
    }
}
