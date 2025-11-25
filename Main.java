package com.example.dataproc;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class Main {
    private static final Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) throws InterruptedException {
        final int NUM_WORKERS = 4;
        final int NUM_TASKS = 20;
        Path outputFile = Paths.get("java_results.txt");

        SharedTaskQueue queue = new SharedTaskQueue();
        List<String> results = new CopyOnWriteArrayList<>();

        ExecutorService exec = Executors.newFixedThreadPool(NUM_WORKERS);

        // start workers
        for (int i = 0; i < NUM_WORKERS; i++) {
            exec.submit(new Worker(i + 1, queue, results, outputFile));
        }

        // produce tasks
        for (int i = 1; i <= NUM_TASKS; i++) {
            Task t = new Task(i, "payload-" + i);
            queue.addTask(t);
            logger.info(() -> "Main added task " + t.getId());
            // optionally sleep to simulate real-time arrival
            Thread.sleep(50);
        }

        // all tasks submitted -> close queue and shutdown
        queue.close();
        exec.shutdown(); // workers will exit when queue empty and closed

        // wait for termination
        while (!exec.isTerminated()) {
            Thread.sleep(200);
        }

        logger.info(() -> "All workers terminated. Results collected: " + results.size());
        // Optionally print results summary
        results.forEach(System.out::println);
    }
}
