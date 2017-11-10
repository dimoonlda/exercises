package ua.kiev.dimoon.test.concurrent.java.impatient.ex1;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;

public class Exercise1 {
    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        String word = "java";
        Path startPath = Paths.get("/home/dlutai/Downloads/java-thread-programming");
        long count = Files.walk(startPath)
                .parallel()
                .filter(path -> path.toFile().isFile())
                .map(path -> path.getFileName().toString())
                .filter(pathAsString -> (pathAsString.contains(word)))
                .count();
        System.out.println("Count: " + count);

        Files.walk(startPath)
                .parallel()
                .filter(path -> path.toFile().isFile())
                .map(path -> path.getFileName().toString())
                .filter(pathAsString -> (pathAsString.contains(word)))
                .findFirst()
                .ifPresent(first -> System.out.println("First: " + first));
    }
}
