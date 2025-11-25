package com.example.dataproc;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class SharedTaskQueue {
    private final Queue<Task> queue = new LinkedList<>();
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition notEmpty = lock.newCondition();
    private boolean closed = false; // support graceful shutdown

    public void addTask(Task task) {
        lock.lock();
        try {
            if (closed) throw new IllegalStateException("Queue closed");
            queue.add(task);
            notEmpty.signal();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Waits until a task is available or queue is closed and empty.
     * Returns null if queue is closed and empty.
     */
    public Task getTask() throws InterruptedException {
        lock.lock();
        try {
            while (queue.isEmpty()) {
                if (closed) return null;
                notEmpty.await();
            }
            return queue.poll();
        } finally {
            lock.unlock();
        }
    }

    public void close() {
        lock.lock();
        try {
            closed = true;
            notEmpty.signalAll();
        } finally {
            lock.unlock();
        }
    }
}
