package ua.kiev.dimoon.test.concurrent.java.impatient.ex3;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class Exercise3 {
    static String word = "String";

    static Callable<String> createTask(String file) {
        return () -> {
            String threadName = Thread.currentThread().getName();
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {

                String currentLine;

                while ((currentLine = br.readLine()) != null) {
                    System.out.println(threadName + ": Is about to sleep.");
                    //Thread.sleep(500);
                    System.out.println("waked.");
                    if (Thread.currentThread().isInterrupted()) {
                        System.out.println("interrupted");
                        return null;
                    }
                    if (currentLine.contains(word)) {
                        return file;
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        };
    }

    static Runnable createRunnable(String file) {
        return () -> {
            String threadName = Thread.currentThread().getName();
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {

                String currentLine;

                while ((currentLine = br.readLine()) != null) {
                    System.out.println(threadName + ": Is about to sleep.");
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    System.out.println("waked.");
                    if (Thread.currentThread().isInterrupted()) {
                        System.out.println("interrupted");
                        break;
                    }
                    if (currentLine.contains(word)) {
                        System.out.println("Found.");
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        };
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException, IOException {
        Path root = Paths.get("/home/dlutai/projects/couchbasetest/src/main/java/demo/");
        ForkJoinPool pool = ForkJoinPool.commonPool();
        //Callable<String> callable = createTask("/home/dlutai/projects/couchbasetest/src/main/java/demo/Employee.java");
        List<Callable<String>> files = Files.walk(root).parallel()
                .filter(path -> path.toFile().isFile())
                .map(path -> {
                    System.out.println(path.toAbsolutePath().toString());
                    return createTask(path.toAbsolutePath().toString());
                })
                .collect(Collectors.toList());
        String resultFile = pool.invokeAny(files);
        Thread.sleep(1000);
        System.out.println("Winner: " + resultFile);
    }

//    public static void main(String[] args) throws ExecutionException, InterruptedException {
//        ExecutorService pool = Executors.newCachedThreadPool();
//        Thread thread = new Thread(createRunnable("/home/dlutai/projects/couchbasetest/src/main/java/demo/Employee.java"));
//        thread.start();
//        Thread.sleep(1000);
//        thread.interrupt();
//    }

}
