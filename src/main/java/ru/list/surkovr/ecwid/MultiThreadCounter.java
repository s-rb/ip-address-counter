package ru.list.surkovr.ecwid;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static ru.list.surkovr.ecwid.App.IP_SPLIT_REGEX;

// ! Не протестирован на большом объеме !
public class MultiThreadCounter implements Counter {

    public static final int MAX_QUEUE_SIZE = 100;
    public static final int POOL_SIZE = Runtime.getRuntime().availableProcessors();
    public static final int MAX_COMPUTE_THREADS_COUNT = POOL_SIZE - 1;
    private final String sourceFile;

    public MultiThreadCounter(String sourceFile) {
        this.sourceFile = sourceFile;
    }

    @Override
    public Long countUniqueIPs() {
        final boolean[][][][] uniqueIPs = new boolean[256][256][256][256];
        final Queue<String> stringQueue = new ConcurrentLinkedQueue<>();
        final AtomicLong counter = new AtomicLong(0);
        final ExecutorService pool = Executors.newFixedThreadPool(POOL_SIZE);
        final AtomicBoolean isReadFinished = new AtomicBoolean(false);

        pool.execute(getReadFileTask(stringQueue, isReadFinished));
        for (int i = 0; i < MAX_COMPUTE_THREADS_COUNT; i++) {
            pool.execute(getComputeTask(counter, uniqueIPs, stringQueue, isReadFinished));
        }

        while (!isReadFinished.get() || !stringQueue.isEmpty()) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        pool.shutdown();
        return counter.get();
    }

    private Runnable getReadFileTask(Queue<String> stringQueue, AtomicBoolean isReadFinished) {
        return () -> {
            File file = new File(sourceFile);
            try (FileReader fr = new FileReader(file);
                 BufferedReader br = new BufferedReader(fr)) {
                String s;
                while ((s = br.readLine()) != null) {
                    try {
                        while (stringQueue.size() > MAX_QUEUE_SIZE) {
                            Thread.sleep(1);
                        }
                        stringQueue.add(s);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                isReadFinished.set(true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
    }

    private Runnable getComputeTask(AtomicLong counter, boolean[][][][] uniqueIPs,
                                    Queue<String> stringQueue, AtomicBoolean isReadFinished) {
        return () -> {
            while (!isReadFinished.get() || !stringQueue.isEmpty()) {
                try {
                    String str;
                    while ((str = stringQueue.poll()) == null) {
                        Thread.sleep(1);
                    }
                    String[] splitted = str.split(IP_SPLIT_REGEX);
                    if (splitted.length != 4) return;
                    int a = Integer.parseInt(splitted[0]);
                    int b = Integer.parseInt(splitted[1]);
                    int c = Integer.parseInt(splitted[2]);
                    int d = Integer.parseInt(splitted[3]);
                    synchronized (uniqueIPs) {
                        if (!uniqueIPs[a][b][c][d]) {
                            uniqueIPs[a][b][c][d] = true;
                            counter.incrementAndGet();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }
}