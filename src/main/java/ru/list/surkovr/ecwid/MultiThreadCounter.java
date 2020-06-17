package ru.list.surkovr.ecwid;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static ru.list.surkovr.ecwid.App.IP_SPLIT_REGEX;

// ! Не протестирован на большом объеме !
// TODO: Использовать для синхронизации работы CountDownLatch - 
//  https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/CountDownLatch.html
public class MultiThreadCounter implements Counter {

    public static final int MAX_QUEUE_SIZE = 150;
    public static final int POOL_SIZE = Runtime.getRuntime().availableProcessors();
    public static final int MAX_COMPUTE_THREADS_COUNT = POOL_SIZE;
    public static final int TIMEOUT_POOL_TERMINATION_SEC = 2;
    public static final int TIMEOUT_POLL_QUEUE_SEC = 1;

    private final String sourceFile;

    private final ExecutorService pool;
    private final BlockingQueue<String> stringQueue;
    private final AtomicBoolean isReadFinished;
    private final boolean[][][][] uniqueIPs;
    private final AtomicLong counter;

    public MultiThreadCounter(String sourceFile) {
        this.sourceFile = sourceFile;
        pool = Executors.newWorkStealingPool(POOL_SIZE);
        stringQueue = new LinkedBlockingQueue<>(MAX_QUEUE_SIZE);
        isReadFinished = new AtomicBoolean(false);
        uniqueIPs = new boolean[256][256][256][256];
        counter = new AtomicLong(0);
    }

    @Override
    public Long countUniqueIPs() {
        try {
            pool.execute(getReadFileTask());
            for (int i = 0; i < MAX_COMPUTE_THREADS_COUNT; i++) {
                pool.execute(getComputeTask());
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        }

        while (!isReadFinished.get() || !stringQueue.isEmpty()) {
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        try {
            pool.shutdown();
            pool.awaitTermination(TIMEOUT_POOL_TERMINATION_SEC, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            System.err.println("tasks interrupted");
        } finally {
            if (!pool.isTerminated()) {
                System.err.println("cancel non-finished tasks");
            }
            pool.shutdownNow();
        }
        return counter.get();
    }

    private Runnable getReadFileTask() {
        return () -> {
            File file = new File(sourceFile);
            try (FileReader fr = new FileReader(file);
                 BufferedReader br = new BufferedReader(fr)) {
                String s;
                while ((s = br.readLine()) != null) {
                    try {
                        stringQueue.put(s);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                isReadFinished.set(true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
    }

    private Runnable getComputeTask() {
        return () -> {
            while (!isReadFinished.get() || !stringQueue.isEmpty()) {
                try {
                    String str = stringQueue.poll(TIMEOUT_POLL_QUEUE_SEC, TimeUnit.SECONDS);
                    if (str == null) continue;
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