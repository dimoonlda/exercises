package ua.kiev.dimoon.test.concurrent.java.impatient.ex7;

import java.util.concurrent.ConcurrentHashMap;

public class Exercise7 {
    static ConcurrentHashMap<String, Long> map = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        map.put("1", 1L);
        map.put("2", 2L);
        map.put("3", 3L);
        map.put("13", 3L);
        map.put("4", 14L);
        map.put("5", 5L);
        map.put("6", 6L);
        map.put("7", 7L);
        System.out.println(map.reduceEntries(Runtime.getRuntime().availableProcessors(),
                (entitySet1, entitySet2) -> (entitySet1.getValue() >= entitySet2.getValue()) ? entitySet1 : entitySet2));
    }
}
