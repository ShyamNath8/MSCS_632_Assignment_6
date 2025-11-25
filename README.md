# Multi-Threaded Data Processing System (Java + Go)

This repository contains the implementation of a **parallel data processing system** developed in **Java** and **Go**. The system simulates multiple worker threads processing tasks retrieved from a shared queue, writing results to a shared output resource, and handling errors safely.  

---

## Project Overview
The Data Processing System demonstrates how different programming languages handle concurrency and error management.  

The system includes:
- A shared task queue  
- Multiple worker threads or goroutines  
- Synchronized access to shared resources  
- Logging of events and exceptions  
- Graceful shutdown with no deadlocks  
- Simulated processing delays  

Both implementations follow the assignment requirements, including task synchronization, safe resource sharing, concurrency control, and exception/error handling.

---

## Project Structure
/Java

/src

/com/example/dataproc

Main.java

Task.java

Worker.java

SharedTaskQueue.java

/Go

main.go

---

## Java Implementation

### Requirements
- JDK 17 or later  
- A terminal or VS Code with Java extension

### How to Compile and Run

1. Navigate to the `src` directory:

cd Java/src

Compile all Java files:

javac com/example/dataproc/*.java


Run the program:

java com.example.dataproc.Main


This launches the worker threads, processes all tasks, and writes results to java_results.txt.

Go Implementation

Requirements

Go 1.20 or later

A terminal or VS Code with Go extension

How to Run

Navigate to the folder:

cd Go


Run the program:

go run main.go


The Go version uses channels and goroutines to process tasks concurrently and writes results to go_results.txt.

Concurrency Techniques Used

Java

ExecutorService for thread pooling

ReentrantLock + Condition for queue synchronization

CopyOnWriteArrayList for thread-safe result storage

synchronized blocks for file writing

Logging via java.util.logging.Logger

Go

Goroutines for lightweight concurrency

Channels for safe task distribution

sync.WaitGroup to wait for workers

sync.Mutex to protect shared file writes

Error handling through returned error values

Resource cleanup with defer

Exception & Error Handling
Java

try–catch blocks for:

InterruptedException

IOException

queue access errors

Threads log errors and continue processing

Go

Every function returns an error

Errors logged using log.Println

File handles protected with defer file.Close()
