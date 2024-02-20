package main

import (
	"sync"
	"testing"
)

// go test -bench=. -benchtime=1000000000x -count 5
func BenchmarkParallel(b *testing.B) {
	const capacity = 16
	const parallelism = 10000

	elementsPerChannel := b.N / parallelism

	var wg sync.WaitGroup

	for i := 0; i < parallelism; i++ {
		c := make(chan int, capacity)

		wg.Add(1)
		go func() {
			defer wg.Done()
			for j := 0; j < elementsPerChannel; j++ {
				c <- 91
			}
		}()

		wg.Add(1)
		go func() {
			defer wg.Done()
			for j := 0; j < elementsPerChannel; j++ {
				<-c
			}
		}()
	}

	wg.Wait()
}
