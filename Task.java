package com.example.dataproc;

public class Task {
    private final int id;
    private final String payload;

    public Task(int id, String payload) {
        this.id = id;
        this.payload = payload;
    }

    public int getId() {
        return id;
    }

    public String getPayload() {
        return payload;
    }
}
