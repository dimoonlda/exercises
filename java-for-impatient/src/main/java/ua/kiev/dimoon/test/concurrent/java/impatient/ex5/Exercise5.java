package ua.kiev.dimoon.test.concurrent.java.impatient.ex5;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class Exercise5 {
    static ConcurrentHashMap<String, Set<String>> map = new ConcurrentHashMap<>();

    static Callable<Void> createTask(String file) {
        return () -> {
            Set<String> fileAsSet = new HashSet<>();
            fileAsSet.add(file);
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {

                String currentLine;

                while ((currentLine = br.readLine()) != null) {
                    if (Thread.currentThread().isInterrupted()) {
                        System.out.println("interrupted");
                        return null;
                    }
                    Arrays.stream(currentLine.split(" "))
                            .parallel()
                            .map(String::trim)
                            .forEach(word -> {
                                map.merge(word, fileAsSet, (oldSet, newSet) -> {
                                    Set<String> files = new HashSet<>();
                                    files.addAll(newSet);
                                    files.addAll(oldSet);
                                    return Collections.unmodifiableSet(files);
                                });
                            });
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        };
    }

    public static void main(String[] args) throws InterruptedException, IOException {
        Path root = Paths.get("/home/dlutai/projects/couchbasetest/src/main/java/demo/");
        ForkJoinPool pool = ForkJoinPool.commonPool();
        List<Callable<Void>> tasks = Files.walk(root).parallel()
                .filter(path -> path.toFile().isFile())
                .map(path -> {
                    System.out.println(path.toAbsolutePath().toString());
                    return createTask(path.toAbsolutePath().toString());
                })
                .collect(Collectors.toList());
        pool.invokeAll(tasks);
        map.entrySet().stream()
                .sorted(Comparator.comparingInt(o -> -1*o.getValue().size()))
                .limit(5)
                .forEach(entry -> System.out.println(entry.getKey() + ": " + entry.getValue()));
    }
}
