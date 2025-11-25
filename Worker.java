package com.example.dataproc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Worker implements Runnable {
    private static final Logger logger = Logger.getLogger(Worker.class.getName());
    private final int id;
    private final SharedTaskQueue queue;
    private final List<String> sharedResults; // thread-safe list
    private final Path outputFile;

    public Worker(int id, SharedTaskQueue queue, List<String> sharedResults, Path outputFile) {
        this.id = id;
        this.queue = queue;
        this.sharedResults = sharedResults;
        this.outputFile = outputFile;
    }

    @Override
    public void run() {
        logger.info(() -> "Worker " + id + " started.");
        try {
            while (true) {
                Task task = queue.getTask();
                if (task == null) {
                    // queue closed and empty -> terminate
                    logger.info(() -> "Worker " + id + " detected queue closed and will exit.");
                    break;
                }
                try {
                    String result = processTask(task);
                    // add to shared results (CopyOnWriteArrayList is thread-safe)
                    sharedResults.add(result);
                    // append to file (IO can throw)
                    safeAppendToFile(result);
                    logger.info(() -> "Worker " + id + " completed task " + task.getId());
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Worker " + id + " processing error on task " + task.getId(), e);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.log(Level.WARNING, "Worker " + id + " interrupted.", e);
        } finally {
            logger.info(() -> "Worker " + id + " terminated.");
        }
    }

    private String processTask(Task task) throws InterruptedException {
        // Simulate computational work
        Thread.sleep(200 + (task.getId() % 5) * 100L); // variable delay
        String result = String.format("TaskResult{id=%d, payload=%s, worker=%d, ts=%s}",
                task.getId(), task.getPayload(), id, Instant.now().toString());
        return result;
    }

    private void safeAppendToFile(String content) {
        try {
            // create file if not exists; append line
            Files.writeString(outputFile, content + System.lineSeparator(),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException io) {
            logger.log(Level.SEVERE, "Worker " + id + " failed to write to file: " + outputFile, io);
        }
    }
}
