package org.orchapod;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

class ReadThread implements Runnable{

	private final String label;
	private final ReadWriteLock rwl;
	private final AtomicInteger counter;
	private final boolean verbose;
	private final long task_duration;
	private final int id;

	public ReadThread(
		final String label, 
		final ReadWriteLock rwl, 
		final AtomicInteger counter, 
		final boolean verbose, 
		final long task_duration, 
		final int id
	){
		this.label = label.trim() + " ";
		this.rwl = rwl;
		this.counter = counter;
		this.verbose = verbose;
		this.task_duration = task_duration;
		this.id = id;
	}

	public void run(){
		
		// Announce that thread has been created
		if(this.verbose) System.out.println(this.label + "#READ# Thread " + this.id + " Started!");

		// Lock
		Lock lock = this.rwl.readLock(); // Creates readlock
		lock.lock();

		// Announce that lock has been successfully created
		if(this.verbose) System.out.println(this.label + "#READ# Thread " + this.id + " locked!");

		// Pretend that there's computationally intensive task to do
		ReentrantLockTest.sleep(this.task_duration/2);

		// Count up in middle of the task
		counter.incrementAndGet();

		// Finish this imaginary computationally intensive task
		ReentrantLockTest.sleep(this.task_duration/2);

		// Unlock
		lock.unlock();

		// Announce that lock is released
		if(this.verbose) System.out.println(this.label + "#READ# Thread " + this.id + " unlocks!");
	}
}
