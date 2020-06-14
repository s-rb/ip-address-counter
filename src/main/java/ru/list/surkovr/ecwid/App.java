package ru.list.surkovr.ecwid;

public class App {

    public static final String SOURCE_FILE = "K:/data/source2.txt";
    public static final String IP_SPLIT_REGEX = "\\.";

    public static void main(String[] args) {
        long startCount = System.currentTimeMillis();
        Counter counter = new SingleThreadCounter(SOURCE_FILE);
        long count = counter.countUniqueIPs();

        System.out.println("Unique results " + count);
        System.out.println("Counted for " +
                (System.currentTimeMillis() - startCount) + " ms");
    }
}