package main

import (
	"context"
	"fmt"
	"log"
	"os"
	"sync"
	"time"
)

// Task represents a unit of work
type Task struct {
	ID      int
	Payload string
}

func worker(ctx context.Context, id int, tasks <-chan *Task, results chan<- string, wg *sync.WaitGroup, fileMutex *sync.Mutex, outFile *os.File) {
	defer wg.Done()
	log.Printf("Worker %d started\n", id)
	for {
		select {
		case <-ctx.Done():
			log.Printf("Worker %d context done, exiting\n", id)
			return
		case task, ok := <-tasks:
			if !ok {
				// channel closed, no more tasks
				log.Printf("Worker %d detected closed task channel, exiting\n", id)
				return
			}
			// process task
			res, err := processTask(task, id)
			if err != nil {
				log.Printf("Worker %d error processing task %d: %v\n", id, task.ID, err)
				continue
			}
			// push result to results channel non-blocking (buffered)
			results <- res
			// persist to file (synchronized)
			fileMutex.Lock()
			_, err = outFile.WriteString(res + "\n")
			fileMutex.Unlock()
			if err != nil {
				log.Printf("Worker %d failed to write to file: %v\n", id, err)
			} else {
				log.Printf("Worker %d completed task %d\n", id, task.ID)
			}
		}
	}
}

func processTask(t *Task, workerID int) (string, error) {
	// simulate computational delay
	d := 150*time.Millisecond + time.Duration(t.ID%5)*100*time.Millisecond
	time.Sleep(d)
	res := fmt.Sprintf("TaskResult{id=%d, payload=%s, worker=%d, ts=%s}", t.ID, t.Payload, workerID, time.Now().Format(time.RFC3339Nano))
	return res, nil
}

func main() {
	numWorkers := 4
	numTasks := 20

	// buffered channels
	tasks := make(chan *Task, 10)
	results := make(chan string, 100)

	// open output file
	outFile, err := os.OpenFile("go_results.txt", os.O_CREATE|os.O_WRONLY|os.O_APPEND, 0644)
	if err != nil {
		log.Fatalf("Failed to open output file: %v\n", err)
	}
	defer func() {
		if cerr := outFile.Close(); cerr != nil {
			log.Printf("Error closing file: %v\n", cerr)
		}
	}()

	var wg sync.WaitGroup
	var fileMutex sync.Mutex
	ctx, cancel := context.WithCancel(context.Background())

	// start workers
	for i := 1; i <= numWorkers; i++ {
		wg.Add(1)
		go worker(ctx, i, tasks, results, &wg, &fileMutex, outFile)
	}

	// producer: send tasks
	for i := 1; i <= numTasks; i++ {
		t := &Task{ID: i, Payload: fmt.Sprintf("payload-%d", i)}
		tasks <- t
		log.Printf("Main enqueued task %d\n", i)
		time.Sleep(50 * time.Millisecond)
	}

	// all tasks enqueued, close channel so workers can finish
	close(tasks)

	// wait for workers to finish
	wg.Wait()
	// no more results will be appended; close results channel
	close(results)

	// drain results (optional)
	count := 0
	for r := range results {
		fmt.Println(r)
		count++
	}
	log.Printf("All workers done. Results count: %d\n", count)

	// cancel context just in case (not strictly necessary here)
	cancel()
}
